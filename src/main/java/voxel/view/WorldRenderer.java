package voxel.view;

import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Geometry;
import com.jme3.ui.Picture;
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

import voxel.model.WorldModel;
import voxel.model.ChunkModel;

/**
 * Classe responsable du rendu du monde entier.
 * Gère tous les renderers de chunks et coordonne l'affichage.
 */
public class WorldRenderer {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Asset manager pour accéder aux ressources */
    private final AssetManager assetManager;
    
    /** Nœud racine contenant tous les chunks du monde */
    private Node worldNode;
    
    /** Tableau des renderers pour chaque chunk */
    private ChunkRenderer[][][] chunkRenderers;

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
        initSkyColors();
        initSun(assetManager);
        this.startTime = System.currentTimeMillis() - 30_000; // Décale de 30 secondes en arrière

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
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Console.fnt");
        coordinatesText = new BitmapText(font);
        coordinatesText.setSize(font.getCharSet().getRenderedSize());
        coordinatesText.setColor(ColorRGBA.White);
        coordinatesText.setText("");
        coordinatesText.setLocalTranslation(10, camera.getHeight() - 10, 0);

        // Ajoute le texte au nœud GUI
        guiNode.attachChild(coordinatesText);
    }

    /**
     * Récupère le tableau des renderers de chunks.
     * 
     * @return Tableau 3D contenant tous les renderers de chunks
     */
    public ChunkRenderer[][][] getChunkRenderers() {
        return chunkRenderers;
    }

    /**
     * Initialise tous les renderers de chunks.
     */
    private void initializeChunkRenderers() {
        int sizeX = worldModel.getWorldSizeX();
        int sizeY = worldModel.getWorldSizeY();
        int sizeZ = worldModel.getWorldSizeZ();
        
        chunkRenderers = new ChunkRenderer[sizeX][sizeY][sizeZ];
        
        // Création des renderers pour tous les chunks
        for (int cx = 0; cx < sizeX; cx++) {
            for (int cy = 0; cy < sizeY; cy++) {
                for (int cz = 0; cz < sizeZ; cz++) {
                    createChunkRenderer(cx, cy, cz);
                }
            }
        }
    }

    /**
     * Crée un renderer pour un chunk spécifique.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    private void createChunkRenderer(int chunkX, int chunkY, int chunkZ) {
        // Récupérer le modèle du chunk
        if (worldModel.getChunk(chunkX, chunkY, chunkZ) != null) {
            // Créer le renderer pour ce chunk
            ChunkRenderer renderer = new ChunkRenderer(
                worldModel.getChunk(chunkX, chunkY, chunkZ),
                worldModel,
                assetManager,
                chunkX, chunkY, chunkZ
            );
            
            // Stocker le renderer
            chunkRenderers[chunkX][chunkY][chunkZ] = renderer;
            
            // Attacher la géométrie opaque au nœud monde
            worldNode.attachChild(renderer.getGeometry());

            // Attacher la géométrie transparente si elle existe
            if (renderer.getTransparentGeometry() != null) {
                worldNode.attachChild(renderer.getTransparentGeometry());
            }
        }
    }

    /**
     * Met à jour tous les maillages des chunks.
     * À appeler quand le mode d'éclairage ou le wireframe change.
     */
    public void updateAllMeshes() {
        int sizeX = worldModel.getWorldSizeX();
        int sizeY = worldModel.getWorldSizeY();
        int sizeZ = worldModel.getWorldSizeZ();
        
        for (int cx = 0; cx < sizeX; cx++) {
            for (int cy = 0; cy < sizeY; cy++) {
                for (int cz = 0; cz < sizeZ; cz++) {
                    if (chunkRenderers[cx][cy][cz] != null) {
                        ChunkRenderer renderer = chunkRenderers[cx][cy][cz];

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
                }
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
        for (int cx = 0; cx < worldModel.getWorldSizeX(); cx++) {
            for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
                for (int cz = 0; cz < worldModel.getWorldSizeZ(); cz++) {
                    if (chunkRenderers[cx][cy][cz] != null) {
                        // Appliquer le mode filaire au maillage opaque
                        if (chunkRenderers[cx][cy][cz].getMaterial() != null) {
                            chunkRenderers[cx][cy][cz].getMaterial()
                                    .getAdditionalRenderState()
                                    .setWireframe(wireframeEnabled);
                        }

                        // Appliquer le mode filaire au maillage transparent s'il existe
                        Geometry transparentGeometry = chunkRenderers[cx][cy][cz].getTransparentGeometry();
                        if (transparentGeometry != null && transparentGeometry.getMaterial() != null) {
                            transparentGeometry.getMaterial()
                                    .getAdditionalRenderState()
                                    .setWireframe(wireframeEnabled);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Met à jour le maillage d'un chunk spécifique.
     * À appeler quand un bloc est modifié.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    public void updateChunkMesh(int chunkX, int chunkY, int chunkZ) {
        if (chunkX >= 0 && chunkX < worldModel.getWorldSizeX() &&
            chunkY >= 0 && chunkY < worldModel.getWorldSizeY() &&
            chunkZ >= 0 && chunkZ < worldModel.getWorldSizeZ() &&
            chunkRenderers[chunkX][chunkY][chunkZ] != null) {
            
            ChunkRenderer renderer = chunkRenderers[chunkX][chunkY][chunkZ];

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

            // Formater le texte avec les coordonnées du joueur et du chunk
            String text = String.format(
                "Position: %.2f, %.2f, %.2f\nChunk: %d, %d, %d",
                location.x, location.y, location.z,
                cx, cy, cz
            );

            coordinatesText.setText(text);
        }
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
        // Gestion du cycle jour/nuit
        long elapsed = (System.currentTimeMillis() - startTime) / 1000; // secondes écoulées
        int step = (int)((elapsed * 64 / 60) % 192); // 192 étapes sur le cycle
        ColorRGBA skyColor = skyColors[step];
        mainViewport.setBackgroundColor(skyColor);

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

        // Met à jour le texte des coordonnées si nécessaire
        if (displayCoordinates) {
            updateCoordinatesText();
        }

        entityRendererManager.update();
    }

    public EntityRendererManager getEntityRendererManager() {
        return entityRendererManager;
    }
} 