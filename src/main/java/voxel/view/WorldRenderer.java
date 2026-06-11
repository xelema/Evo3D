package voxel.view;

import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.material.Material;
import com.jme3.renderer.ViewPort;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;

import voxel.controller.GameController;
import voxel.controller.PlayerController;
import voxel.model.WorldModel;
import voxel.model.ChunkModel;
import voxel.view.entity.EntityRendererManager;
import voxel.model.BiomeType;
import voxel.model.BlockType;
import voxel.controller.GameStateManager;

/**
 * Classe responsable du rendu du monde entier.
 * Gère tous les renderers de chunks et coordonne l'affichage.
 */
public class WorldRenderer {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Asset manager pour accéder aux ressources */
    private final AssetManager assetManager;
    
    /** Référence au gestionnaire d'états pour la vitesse du temps */
    private GameStateManager gameStateManager;

    /** Référence au contrôleur de jeu pour accéder aux informations sur les animaux */
    private GameController gameController;

    /** Nœud racine contenant tous les chunks du monde */
    private Node worldNode;
    
    /**
     * Renderers des chunks, indexés par clé compactée (cx, cy, cz).
     * Une map permet un monde infini : des renderers sont ajoutés à la volée
     * quand de nouvelles colonnes de chunks sont générées par le streaming.
     * Concurrente car lue par les threads de meshing en arrière-plan.
     */
    private final java.util.concurrent.ConcurrentHashMap<Long, ChunkRenderer> chunkRenderers =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Pipeline asynchrone de reconstruction des maillages de chunks */
    private ChunkMeshingService meshingService;

    /** Rayon de chargement des colonnes de chunks autour du joueur (monde infini) */
    private static final int LOAD_RADIUS = 3;

    /** Thread d'arrière-plan qui génère les nouvelles colonnes de terrain */
    private final java.util.concurrent.ExecutorService columnGenExecutor =
            java.util.concurrent.Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "column-generator");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            });

    /** Colonnes en cours de génération ou en attente d'attache (clés compactées) */
    private final java.util.Set<Long> columnsInFlight = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** Colonnes générées, en attente d'attache au scene graph par le thread de rendu */
    private final java.util.concurrent.ConcurrentLinkedQueue<int[]> completedColumns =
            new java.util.concurrent.ConcurrentLinkedQueue<>();

    private EntityRendererManager entityRendererManager;

    /** Caméra pour obtenir la position du joueur */
    private Camera camera;

    /** Text pour afficher les coordonnées */
    private BitmapText coordinatesText;

    /** Flag pour indiquer si les coordonnées doivent être affichées */
    private boolean displayCoordinates = false;

    /** Indique si une mise à jour du maillage est nécessaire */
    private boolean needsMeshUpdate = false;

    private ColorRGBA[] skyColors = new ColorRGBA[192];
    private long startTime;
    private Geometry sunGeometry;
    private Node skyNode = new Node("sky");

    /** Temps virtuel accumulé pour le cycle jour/nuit, affecté par la vitesse de l'environnement */
    private float virtualTime = 0.0f;

    /** Processeur de filtres pour les effets post-traitement */
    private FilterPostProcessor fpp;

    /** Filtre de bloom pour l'effet lumineux du soleil */
    private BloomFilter bloomFilter;

    /**
     * Crée un nouveau renderer pour le monde entier.
     * 
     * @param worldModel Le modèle du monde à rendre
     * @param assetManager Asset manager pour accéder aux ressources
     */
    public WorldRenderer(WorldModel worldModel, AssetManager assetManager) {
        this.worldModel = worldModel;
        this.assetManager = assetManager;
        this.worldNode = new Node("world");
        this.skyNode = new Node("sky");
        initializeChunkRenderers();
        this.meshingService = new ChunkMeshingService(worldModel, this);
        initSkyColors();
        initSun(assetManager);
        this.startTime = System.currentTimeMillis() - 30_000; // Décale de 30 secondes en arrière
        this.virtualTime = (48f * 60f) / 64f; // démarrage à 48 steps

        this.entityRendererManager = new EntityRendererManager(this.worldModel.getEntityManager(), assetManager);
        worldNode.attachChild(entityRendererManager.getNode());
    }

    /**
     * Initialise les éléments d'interface utilisateur.
     *
     * @param guiNode Nœud GUI auquel attacher les éléments
     * @param camera Caméra du joueur
     */
    public void initializeUI(Node guiNode, Camera camera) {
        this.camera = camera;

        // Crée le texte pour afficher les coordonnées
        BitmapFont font = assetManager.loadFont("Fonts/IBMDOSVGA9x16.fnt");
        coordinatesText = new BitmapText(font);
        coordinatesText.setSize(font.getCharSet().getRenderedSize());
        coordinatesText.setColor(ColorRGBA.White);
        coordinatesText.setText("");
        coordinatesText.setLocalTranslation(10, camera.getHeight() - 10, 0);

        // Ajoute le texte au nœud GUI
        guiNode.attachChild(coordinatesText);
    }

    /** Compacte des coordonnées de chunk en une clé unique (21 bits par axe) */
    private static long packChunk(int chunkX, int chunkY, int chunkZ) {
        return ((long) (chunkX & 0x1FFFFF) << 42)
             | ((long) (chunkY & 0x1FFFFF) << 21)
             | (chunkZ & 0x1FFFFF);
    }

    /** Compacte des coordonnées de colonne (cx, cz) en une clé unique */
    private static long packColumn(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Initialise les renderers des chunks de la zone initiale du monde.
     */
    private void initializeChunkRenderers() {
        int sizeX = worldModel.getWorldSizeX();
        int sizeY = worldModel.getWorldSizeY();
        int sizeZ = worldModel.getWorldSizeZ();

        // Création des renderers pour tous les chunks de la zone initiale
        for (int cx = 0; cx < sizeX; cx++) {
            for (int cy = 0; cy < sizeY; cy++) {
                for (int cz = 0; cz < sizeZ; cz++) {
                    createChunkRenderer(cx, cy, cz, false);
                }
            }
        }
    }

    /**
     * Crée un renderer pour un chunk spécifique et l'attache au scene graph.
     * Doit être appelé depuis le thread de rendu (ou avant le démarrage du rendu).
     *
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     * @param deferMeshing Si true, le renderer est créé avec des maillages vides
     *                     qui seront construits en arrière-plan ensuite
     */
    private void createChunkRenderer(int chunkX, int chunkY, int chunkZ, boolean deferMeshing) {
        long key = packChunk(chunkX, chunkY, chunkZ);

        // Récupérer le modèle du chunk (et éviter les doublons)
        if (worldModel.getChunk(chunkX, chunkY, chunkZ) != null && !chunkRenderers.containsKey(key)) {
            // Créer le renderer pour ce chunk
            ChunkRenderer renderer = new ChunkRenderer(
                worldModel.getChunk(chunkX, chunkY, chunkZ),
                worldModel,
                assetManager,
                chunkX, chunkY, chunkZ,
                deferMeshing
            );

            // Stocker le renderer
            chunkRenderers.put(key, renderer);

            // Attacher la géométrie opaque au nœud monde
            worldNode.attachChild(renderer.getGeometry());

            // Attacher la géométrie transparente si elle existe
            if (renderer.getTransparentGeometry() != null) {
                worldNode.attachChild(renderer.getTransparentGeometry());
            }
        }
    }

    /**
     * Récupère le renderer d'un chunk spécifique.
     *
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     * @return Le renderer du chunk, ou null s'il n'existe pas
     */
    public ChunkRenderer getChunkRenderer(int chunkX, int chunkY, int chunkZ) {
        return chunkRenderers.get(packChunk(chunkX, chunkY, chunkZ));
    }

    /**
     * Streaming de chunks pour le monde infini : génère en arrière-plan les
     * colonnes de terrain manquantes autour du joueur, puis les attache au
     * scene graph (au plus une colonne par frame) et déclenche la construction
     * de leurs maillages via le pipeline asynchrone.
     */
    private void updateChunkStreaming() {
        // Pas de streaming sans caméra ni pour l'île flottante (monde fini spécial)
        if (camera == null || worldModel.getActiveBiome().isFloatingIsland()) {
            return;
        }

        // Colonne occupée par la caméra, en espace d'index
        Vector3f location = camera.getLocation();
        int camCx = Math.floorDiv((int) Math.floor(location.x), ChunkModel.SIZE) + worldModel.getWorldSizeX() / 2;
        int camCz = Math.floorDiv((int) Math.floor(location.z), ChunkModel.SIZE) + worldModel.getWorldSizeZ() / 2;

        // Demander la génération des colonnes manquantes autour du joueur
        for (int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++) {
            for (int dz = -LOAD_RADIUS; dz <= LOAD_RADIUS; dz++) {
                final int cx = camCx + dx;
                final int cz = camCz + dz;

                if (!worldModel.hasColumn(cx, cz)) {
                    final long key = packColumn(cx, cz);
                    if (columnsInFlight.add(key)) {
                        columnGenExecutor.submit(() -> {
                            try {
                                worldModel.generateColumn(cx, cz);
                                completedColumns.add(new int[]{cx, cz});
                            } catch (Exception e) {
                                columnsInFlight.remove(key);
                                System.err.println("Erreur de génération de la colonne (" + cx + ", " + cz + ") : " + e);
                            }
                        });
                    }
                }
            }
        }

        // Attacher au plus une colonne générée par frame (budget) :
        // renderers créés avec des maillages vides, remplis en arrière-plan
        int[] column = completedColumns.poll();
        if (column != null) {
            int cx = column[0];
            int cz = column[1];

            for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
                createChunkRenderer(cx, cy, cz, true);
            }

            // Mesher la nouvelle colonne, et remesher les 8 colonnes voisines
            // existantes : leurs faces de bordure contre l'ancien vide, et
            // l'occlusion ambiante qui échantillonne aussi en diagonale
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    remeshColumn(cx + dx, cz + dz);
                }
            }

            columnsInFlight.remove(packColumn(cx, cz));
        }
    }

    /**
     * Demande la reconstruction asynchrone des maillages de tous les chunks
     * existants d'une colonne.
     */
    private void remeshColumn(int chunkX, int chunkZ) {
        for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
            ChunkModel chunk = worldModel.getChunk(chunkX, cy, chunkZ);
            if (chunk != null) {
                chunk.markDirty();
                meshingService.requestRemesh(chunkX, cy, chunkZ);
            }
        }
    }

    /**
     * Demande la reconstruction asynchrone du maillage d'un chunk.
     * Le maillage sera construit en arrière-plan puis appliqué lors d'une
     * prochaine frame, sans bloquer le thread de rendu.
     *
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    public void requestChunkRemesh(int chunkX, int chunkY, int chunkZ) {
        meshingService.requestRemesh(chunkX, chunkY, chunkZ);
    }

    /**
     * Applique des maillages construits en arrière-plan à la géométrie d'un chunk.
     * Doit être appelé depuis le thread de rendu.
     *
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     * @param opaqueMesh Le nouveau maillage opaque
     * @param transparentMesh Le nouveau maillage transparent, ou null
     */
    public void applyChunkMeshes(int chunkX, int chunkY, int chunkZ, Mesh opaqueMesh, Mesh transparentMesh) {
        ChunkRenderer renderer = getChunkRenderer(chunkX, chunkY, chunkZ);
        if (renderer == null) {
            return;
        }

        // Conserver la référence à l'ancienne géométrie transparente
        Geometry oldTransparentGeometry = renderer.getTransparentGeometry();

        renderer.applyMeshes(opaqueMesh, transparentMesh);

        // Gérer la nouvelle géométrie transparente
        Geometry newTransparentGeometry = renderer.getTransparentGeometry();

        if (oldTransparentGeometry == null && newTransparentGeometry != null) {
            worldNode.attachChild(newTransparentGeometry);
        } else if (oldTransparentGeometry != null && newTransparentGeometry == null) {
            worldNode.detachChild(oldTransparentGeometry);
        }
    }

    /**
     * Arrête les threads de meshing et de génération en arrière-plan.
     * À appeler quand le monde est détruit.
     */
    public void shutdownMeshing() {
        if (meshingService != null) {
            meshingService.shutdown();
        }
        columnGenExecutor.shutdownNow();
        completedColumns.clear();
        columnsInFlight.clear();
    }

    /**
     * Met à jour tous les maillages des chunks.
     * À appeler quand le mode d'éclairage ou le wireframe change.
     */
    public void updateAllMeshes() {
        for (ChunkRenderer renderer : chunkRenderers.values()) {
            // Invalider les éventuels maillages en cours de construction
            // en arrière-plan, construits avec l'ancien mode de rendu
            renderer.getChunkModel().markDirty();

            // Conserver la référence à l'ancienne géométrie transparente
            Geometry oldTransparentGeometry = renderer.getTransparentGeometry();

            // Mettre à jour le mesh
            renderer.updateMesh();

            // Gérer la nouvelle géométrie transparente
            Geometry newTransparentGeometry = renderer.getTransparentGeometry();

            // Si une nouvelle géométrie transparente a été créée
            if (oldTransparentGeometry == null && newTransparentGeometry != null) {
                worldNode.attachChild(newTransparentGeometry);
            }
            // Si la géométrie transparente a été supprimée
            else if (oldTransparentGeometry != null && newTransparentGeometry == null) {
                worldNode.detachChild(oldTransparentGeometry);
            }
        }

        needsMeshUpdate = false;
    }

    /**
     * Applique l'état actuel du mode filaire (défini dans WorldModel)
     * aux matériaux de tous les chunks sans reconstruire les maillages.
     */
    public void applyWireframeModeToMaterials() {
        boolean wireframeEnabled = worldModel.getWireframeMode();
        for (ChunkRenderer renderer : chunkRenderers.values()) {
            // Appliquer le mode filaire au maillage opaque
            if (renderer.getMaterial() != null) {
                renderer.getMaterial()
                        .getAdditionalRenderState()
                        .setWireframe(wireframeEnabled);
            }

            // Appliquer le mode filaire au maillage transparent s'il existe
            Geometry transparentGeometry = renderer.getTransparentGeometry();
            if (transparentGeometry != null && transparentGeometry.getMaterial() != null) {
                transparentGeometry.getMaterial()
                        .getAdditionalRenderState()
                        .setWireframe(wireframeEnabled);
            }
        }
    }

    /**
     * Retourne le nœud contenant l'ensemble du monde voxel.
     * 
     * @return Le nœud racine du monde
     */
    public Node getNode() {
        return worldNode;
    }
    
    /**
     * Signale qu'une mise à jour des maillages est nécessaire.
     */
    public void setNeedsMeshUpdate() {
        needsMeshUpdate = true;
    }
    
    /**
     * Active ou désactive l'affichage des coordonnées.
     *
     * @param display true pour afficher, false pour masquer
     */
    public void setDisplayCoordinates(boolean display) {
        this.displayCoordinates = display;

        // Efface le texte si on désactive l'affichage
        if (!display && coordinatesText != null) {
            coordinatesText.setText("");
        }
    }

    /**
     * Met à jour le texte des coordonnées.
     */
    private void updateCoordinatesText() {
        if (displayCoordinates && coordinatesText != null && camera != null) {
            Vector3f location = camera.getLocation();

            // Calcul des coordonnées du chunk comme dans WorldModel
            // On ajoute le décalage pour convertir les coordonnées centrées en coordonnées d'index
            int offsetX = worldModel.getWorldSizeX() * ChunkModel.SIZE / 2;
            int offsetZ = worldModel.getWorldSizeZ() * ChunkModel.SIZE / 2;

            // Conversion des coordonnées de la caméra en coordonnées globales des index
            int globalX = (int)location.x + offsetX;
            int globalY = (int)location.y;
            int globalZ = (int)location.z + offsetZ;

            // Calcul des indices de chunk utilisés dans les tableaux
            int cx = Math.floorDiv(globalX, ChunkModel.SIZE);
            int cy = Math.floorDiv(globalY, ChunkModel.SIZE);
            int cz = Math.floorDiv(globalZ, ChunkModel.SIZE);

            // Récupération des informations du biome
            BiomeType activeBiome = worldModel.getActiveBiome();

            // Récupération du bloc sous les pieds du joueur
            int blockUnderFeet = worldModel.getBlockAt(globalX, globalY - 1, globalZ);
            BlockType blockTypeUnderFeet = BlockType.fromId(blockUnderFeet);

            // Récupération du bloc au niveau des pieds (position actuelle)
            int blockAtFeet = worldModel.getBlockAt(globalX, globalY, globalZ);
            BlockType blockTypeAtFeet = BlockType.fromId(blockAtFeet);

            // Récupération de la hauteur du sol
            int groundHeight = worldModel.getGroundHeightAt(globalX, globalZ);

            // Calcul de l'altitude relative par rapport au sol
            int altitudeAboveGround = globalY - groundHeight;

            // Paramètres environnementaux du monde
            int temperature = worldModel.getTemperature();
            int humidity = worldModel.getHumidity();
            int reliefComplexity = worldModel.getReliefComplexity();

            // Noms des paramètres
            String[] tempNames = {"Glacial", "Froid", "Tempéré", "Chaud", "Torride"};
            String[] humidityNames = {"Aride", "Sec", "Modéré", "Humide", "Saturé"};
            String[] reliefNames = {"Plat", "Doux", "Vallonné", "Accidenté", "Montagneux"};

            // Composition du biome
            String surfaceBlock = activeBiome.getSurfaceBlock().name();
            String subSurfaceBlock = activeBiome.getSubSurfaceBlock().name();
            String deepBlock = activeBiome.getDeepBlock().name();
            String waterBlock = activeBiome.getWaterBlock().name();

            // Informations sur les animaux (si le gameController est disponible)
            String animalInfo = "";
            if (gameController != null) {
                int currentAnimals = gameController.getAnimalCount();
                int maxAnimals = gameController.getMaxAnimals();
                animalInfo = String.format(
                    "\n=== ÉCOSYSTEME ===\n" +
                    "Animaux: %d/%d\n" +
                    "Densité: %.2f animaux/chunk²\n",
                    currentAnimals, maxAnimals,
                    (float)currentAnimals / (worldModel.getWorldSizeX() * worldModel.getWorldSizeZ())
                );
            }

            // Formater le texte avec toutes les informations
            String text = String.format(
                "=== INFORMATIONS DU MONDE ===\n" +
                "Seed : %d\n" +
                "Taille : %dx%dx%d chunks\n" +
                "%s" +
                "\n=== POSITION JOUEUR ===\n" +
                "Position: %.1f, %.1f, %.1f\n" +
                "Chunk: %d, %d, %d\n" +
                "Bloc local: %d, %d, %d\n" +
                "Altitude: %d\n" +
                "\n=== BIOME ACTUEL ===\n" +
                "Nom: %s\n" +
                "Temperature: %s (%d/4)\n" +
                "Humidité: %s (%d/4)\n" +
                "Relief: %s (%d/4)\n" +
                "\n=== COMPOSITION BIOME ===\n" +
                "Surface: %s\n" +
                "Sous-surface: %s\n" +
                "Profondeur: %s\n" +
                "Eau: %s",

                // Informations du monde
                worldModel.getWorldSeed(),
                worldModel.getWorldSizeX(), worldModel.getWorldSizeY(), worldModel.getWorldSizeZ(),

                // Informations sur les animaux
                animalInfo,

                // Position du joueur
                location.x, location.y, location.z,
                cx, cy, cz,
                globalX % ChunkModel.SIZE, globalY % ChunkModel.SIZE, globalZ % ChunkModel.SIZE,
                (int) (location.y - 3 - worldModel.getWaterLevel()),

                // Biome actuel
                activeBiome.getName(),
                tempNames[temperature], temperature,
                humidityNames[humidity], humidity,
                reliefNames[reliefComplexity], reliefComplexity,

                // Composition du biome
                surfaceBlock, subSurfaceBlock, deepBlock, waterBlock
            );

            coordinatesText.setText(text);
        }
    }

    /**
     * Définit le gestionnaire d'états du jeu pour accéder à la vitesse du temps de l'environnement
     *
     * @param gameStateManager Le gestionnaire d'états du jeu
     */
    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Définit le contrôleur de jeu pour accéder aux informations sur les animaux
     *
     * @param gameController Le contrôleur de jeu
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Méthode appelée à chaque frame pour mettre à jour le rendu.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf, ViewPort mainViewport) {
        if (needsMeshUpdate) {
            updateAllMeshes();
        }

        // Monde infini : générer et attacher les colonnes de chunks autour du joueur
        updateChunkStreaming();

        // Appliquer les maillages de chunks construits en arrière-plan (budget par frame)
        meshingService.applyCompletedMeshes();

        // Met à jour le texte des coordonnées si nécessaire
        if (displayCoordinates) {
            updateCoordinatesText();
        }

        entityRendererManager.update();

        // Gestion du cycle jour/nuit avec la vitesse de l'environnement
        float environmentSpeed = (gameStateManager != null) ? gameStateManager.getEnvironmentTimeSpeed() : 1.0f;

        // Accumuler le temps virtuel progressivement en appliquant la vitesse de l'environnement
        virtualTime += tpf * environmentSpeed;

        // Calculer l'étape du cycle basée sur le temps virtuel accumulé
        int step = (int)((virtualTime * 64 / 60) % 192); // 192 étapes sur le cycle
        ColorRGBA skyColor = skyColors[step];
        mainViewport.setBackgroundColor(skyColor);

        // Mettre à jour l'intensité du bloom en fonction de l'heure du jour
        updateBloomIntensity(step);

        // Calcule le centre de la map
        float mapCenterX = (worldModel.getWorldSizeX() * ChunkModel.SIZE) / 2f;
        float mapCenterZ = (worldModel.getWorldSizeZ() * ChunkModel.SIZE) / 2f;

        // Décalage d'angle pour choisir la direction du lever
        float angleOffset = (float)(-Math.PI / 2); // Ajuste pour le lever
        float angle = (float)(2 * Math.PI * (step / 192.0)) + angleOffset;
        float radius = 600;
        float inclination = (float)(Math.PI / 4);

        // Centre du cercle = centre de la map
        float centerX = mapCenterX;
        float centerZ = mapCenterZ;

        float sunX = (float)(Math.cos(angle) * radius) + centerX;
        float sunY = (float)(Math.sin(angle) * Math.cos(inclination) * 400);
        float sunZ = (float)(Math.sin(angle) * Math.sin(inclination) * radius) + centerZ;

        sunGeometry.setLocalTranslation(sunX, sunY, sunZ);
    }

    private void initSkyColors() {
        // Bleu ciel (jour)
        float rDay = 0.5f, gDay = 0.8f, bDay = 1.0f;
        // Bleu marine très foncé (nuit)
        float rNight = 0.01f, gNight = 0.03f, bNight = 0.08f;

        float amplitude = 400f; // Doit être identique à l'amplitude utilisée pour sunY dans update()

        for (int i = 0; i < 192; i++) {
            float t = i / 192.0f;
            // Angle du soleil sur le cercle
            float angle = (float)(2 * Math.PI * t - Math.PI / 2);
            float sunY = (float)(Math.sin(angle) * Math.cos(Math.PI / 4) * amplitude);

            // Seuils pour le dégradé autour de l'horizon (augmenté pour un dégradé plus long)
            float seuil = 0.40f * amplitude; // 40% de l'amplitude autour de l'horizon

            float r, g, b;
            if (sunY >= seuil) {
                // Plein jour
                r = rDay; g = gDay; b = bDay;
            } else if (sunY <= -seuil) {
                // Pleine nuit
                r = rNight; g = gNight; b = bNight;
            } else {
                // Dégradé autour de l'horizon
                float alpha = (sunY + seuil) / (2 * seuil); // alpha varie de 0 (nuit) à 1 (jour)
                r = rNight * (1 - alpha) + rDay * alpha;
                g = gNight * (1 - alpha) + gDay * alpha;
                b = bNight * (1 - alpha) + bDay * alpha;
            }
            skyColors[i] = new ColorRGBA(r, g, b, 1.0f);
        }
    }

    private void initSun(AssetManager assetManager) {
        // Crée une sphère pour le soleil, segments élevés pour un rendu lisse
        int zSamples = 32; // plus de segments = plus rond
        int radialSamples = 32;
        float radius = 14f; // plus grand rayon = soleil plus gros

        Sphere sunSphere = new Sphere(zSamples, radialSamples, radius);
        sunGeometry = new Geometry("Sun", sunSphere);
        Material sunMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sunMat.setColor("Color", ColorRGBA.Yellow);
        sunMat.setColor("GlowColor", ColorRGBA.White); // Pour qu'il brille
        sunGeometry.setMaterial(sunMat);
        skyNode.attachChild(sunGeometry);
    }

    public Node getSkyNode() {
        return skyNode;
    }

    public EntityRendererManager getEntityRendererManager() {
        return entityRendererManager;
    }

    /**
     * Initialise l'effet de bloom pour le soleil.
     * Cette méthode doit être appelée après que le ViewPort soit disponible.
     *
     * @param viewPort Le ViewPort principal de l'application
     */
    public void initializeBloomEffect(ViewPort viewPort) {
        // Créer le FilterPostProcessor avec antialiasing
        fpp = new FilterPostProcessor(assetManager);

        // Configurer l'antialiasing sur le FilterPostProcessor
        // Cela compense la perte d'antialiasing MSAA due au post-traitement
        fpp.setNumSamples(4); // Même valeur que dans Main.java

        // Créer le BloomFilter avec le mode Objects pour utiliser les GlowColor
        bloomFilter = new BloomFilter(BloomFilter.GlowMode.Objects);

        // Configurer les paramètres du bloom selon la documentation
        bloomFilter.setBlurScale(3.5f);        // Échelle du flou (défaut: 1.5f)
        bloomFilter.setExposurePower(7.0f);    // Puissance d'exposition (défaut: 5.0f)
        bloomFilter.setExposureCutOff(0.0f);   // Seuil d'exposition (défaut: 0.0f)
        bloomFilter.setBloomIntensity(2.5f);   // Intensité du bloom (défaut: 2.0f)

        // Ajouter le filtre bloom au processeur
        fpp.addFilter(bloomFilter);

        // Ajouter le processeur au ViewPort
        viewPort.addProcessor(fpp);
    }

    /**
     * Met à jour l'intensité du bloom en fonction de l'heure du jour.
     * Le bloom est plus intense quand le soleil est visible.
     *
     * @param step L'étape actuelle du cycle jour/nuit (0-191)
     */
    public void updateBloomIntensity(int step) {
        if (bloomFilter != null) {
            // Calculer l'intensité basée sur la position du soleil
            float angle = (float)(2 * Math.PI * (step / 192.0) - Math.PI / 2);
            float sunY = (float)(Math.sin(angle) * Math.cos(Math.PI / 4) * 400);

            // Le bloom est plus intense quand le soleil est au-dessus de l'horizon
            float intensity;
            if (sunY > 0) {
                // Jour : intensité normale à forte
                intensity = 2.0f + (sunY / 400f) * 1.5f; // Entre 2.0 et 3.5
            } else {
                // Nuit : bloom très faible ou désactivé
                intensity = 0.2f;
            }

            bloomFilter.setBloomIntensity(intensity);
        }
    }
} 