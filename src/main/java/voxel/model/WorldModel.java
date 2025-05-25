package voxel.model;

import com.jme3.math.Vector3f;
import voxel.model.entity.EntityManager;
import voxel.model.structure.plant.BasicTree;

import java.util.Random;

/**
 * Représente le monde de voxels complet, composé de plusieurs chunks.
 * Cette classe gère uniquement les données du monde sans le rendu.
 */
public class WorldModel {
    /** Taille par défaut du monde en nombre de chunks sur les axes X et Z */
    public static final int DEFAULT_WORLD_SIZE = 16;
    
    /** Tableau 3D contenant tous les chunks du monde */
    private ChunkModel[][][] chunks;
    
    /** Taille du monde en nombre de chunks sur l'axe X */
    private int worldSizeX;
    
    /** Taille du monde en nombre de chunks sur l'axe Y */
    private int worldSizeY;
    
    /** Taille du monde en nombre de chunks sur l'axe Z */
    private int worldSizeZ;
    
    /** Eclairage oui ou non */
    private boolean lightningMode = true;
    
    /** Mode filaire activé ou non */
    private boolean wireframeMode = false;

    private EntityManager entityManager;

    /** Générateur de nombres aléatoires */
    private final Random random = new Random();

    /** Valeurs pour definir l'echelle des montagne et des details dans le bruit de Perlin */
    private final int worldSeed = 424242;
    private final int generation_height = 6; // Hauteur max de la génération avec Perlin

    /** Bruit de Perlin pour le monde entier */
    private PerlinNoise worldPerlinNoise;
    private PerlinNoise detailPerlinNoise;
    private PerlinNoise ridgePerlinNoise;
    private PerlinNoise zonePerlinNoise; // Nouvelle couche pour les zones

    /** Biome actif */
    private BiomeType activeBiome;

    /** Paramètres de configuration du monde */
    // Température : 0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum
    private int temperature = 2; // Valeur par défaut : Modéré
    
    // Humidité : 0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum  
    private int humidity = 2; // Valeur par défaut : Modéré
    
    // Complexité du relief : 0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum
    private int reliefComplexity = 2; // Valeur par défaut : Modéré

    /** Hauteurs fixes pour les différents niveaux de relief (en blocs) */
    private static final int[] RELIEF_HEIGHTS = {
        60,   // Relief 0 : Variations importantes (0-60 blocs) - JAMAIS plat
        90,   // Relief 1 : Reliefs moyens (0-90 blocs) 
        140,  // Relief 2 : Reliefs importants (0-140 blocs) 
        200,  // Relief 3 : Reliefs marqués (0-220 blocs)
        320   // Relief 4 : Très hautes montagnes (0-320 blocs)
    };

    private int waterLevel;

    /** Niveau de base du terrain */
    private static final int BASE_TERRAIN_LEVEL = 40;

    /**
     * Crée un nouveau monde de voxels avec la taille par défaut.
     */
    public WorldModel(BiomeType biome) {
        this(biome, DEFAULT_WORLD_SIZE);
    }

    /**
     * Crée un nouveau monde de voxels avec une taille spécifiée.
     * 
     * @param biome Le type de biome du monde
     * @param worldSize La taille du monde en nombre de chunks (axes X et Z)
     */
    public WorldModel(BiomeType biome, int worldSize) {
        this(biome, worldSize, 8, 2, 2, 2); // Valeurs par défaut pour les paramètres environnementaux
    }

    public WorldModel(BiomeType biome, int worldSize, int worldSizeY) {
        this(biome, worldSize, worldSizeY, 2, 2, 2); // Valeurs par défaut pour les paramètres environnementaux
    }

    /**
     * Crée un nouveau monde de voxels avec une taille et des paramètres environnementaux spécifiés.
     * 
     * @param biome Le type de biome du monde
     * @param worldSize La taille du monde en nombre de chunks (axes X et Z)
     * @param temperature Niveau de température (0-4)
     * @param humidity Niveau d'humidité (0-4)
     * @param reliefComplexity Niveau de complexité du relief (0-4)
     */
    public WorldModel(BiomeType biome, int worldSize, int worldSizeY, int temperature, int humidity, int reliefComplexity) {
        this.worldSizeX = worldSize;
        this.worldSizeY = worldSizeY;
        this.worldSizeZ = worldSize;
        
        // Définir les paramètres environnementaux
        this.temperature = Math.max(0, Math.min(4, temperature));
        this.humidity = Math.max(0, Math.min(4, humidity));
        this.reliefComplexity = Math.max(0, Math.min(4, reliefComplexity));

        // Créer le biome basé sur les paramètres (sauf pour île flottante)
        if (biome != null && biome.isFloatingIsland()) {
            this.activeBiome = biome;
        } else {
            this.activeBiome = BiomeType.createBiome(this.temperature, this.humidity, this.reliefComplexity);
        }

        chunks = new ChunkModel[worldSizeX][worldSizeY][worldSizeZ];

        // Initialisation des bruits de Perlin pour tout le monde
        worldPerlinNoise = new PerlinNoise(worldSeed); // Bruit principal
        detailPerlinNoise = new PerlinNoise(worldSeed + 1000); // Bruit de détail
        ridgePerlinNoise = new PerlinNoise(worldSeed + 2000); // Bruit pour les crêtes
        zonePerlinNoise = new PerlinNoise(worldSeed + 3000); // Nouvelle couche pour les zones

        generateWorld(false);
        entityManager = new EntityManager(this);

        System.out.println("Création du monde de taille " + worldSizeX + "x" + worldSizeY + "x" + worldSizeZ);
        System.out.println("Biome : " + activeBiome.toString());
    }

    /**
     * Génère le monde complet avec tous ses chunks.
     */
    private void generateWorld(Boolean flat) {

        // Créer tous les chunks vides
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    chunks[cx][cy][cz] = new ChunkModel(true, cx, cy, cz); // Créer des chunks vides
                }
            }
        }

        // Génération du terrain uniquement sur le plan X-Z
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cz = 0; cz < worldSizeZ; cz++) {
                if (activeBiome.isFloatingIsland()) {
                    // Créer une île flottante au centre du monde
                    createFloatingIsland();
                } else if(flat){
                    generateTerrainFlat(cx,cz);
                } else {
                    generateTerrainWithBiome(cx, cz);
                }
            }
        }

        // Ajout des nuages aléatoires dans le ciel
        addClouds();
    }

    /**
     * Génère un terrain basé sur le biome et les paramètres environnementaux.
     * @param chunkX coordonnée X du chunk
     * @param chunkZ coordonnée Z du chunk
     */
    private void generateTerrainWithBiome(int chunkX, int chunkZ) {
        // Coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE - (float) (worldSizeX * ChunkModel.SIZE) / 2;
        float worldZStart = chunkZ * ChunkModel.SIZE - (float) (worldSizeZ * ChunkModel.SIZE) / 2;

        // Hauteur totale disponible
        int totalHeight = generation_height * ChunkModel.SIZE;
        
        // Hauteur max de terrain basée sur le relief (hauteur fixe)
        int maxReliefVariation = RELIEF_HEIGHTS[reliefComplexity];
        
        // Niveau d'eau basé sur la température et l'humidité
        waterLevel = calculateWaterLevel();

        // Générer chaque colonne de blocs
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int z = 0; z < ChunkModel.SIZE; z++) {
                // Coordonnées globales pour ce bloc spécifique
                float worldX = worldXStart + x;
                float worldZ = worldZStart + z;

                // Calcul de la hauteur du terrain
                int terrainHeight = calculateTerrainHeight(worldX, worldZ, maxReliefVariation);
                
                // Limiter la hauteur pour éviter de dépasser le monde
                terrainHeight = Math.min(terrainHeight, totalHeight - 10);
                terrainHeight = Math.max(terrainHeight, waterLevel - 5);

                // Générer les blocs pour chaque hauteur
                for (int y = 0; y < totalHeight; y++) {
                    BlockType blockType = determineBlockType(y, terrainHeight, waterLevel);
                    setBlockAt((int) worldX, y, (int) worldZ, blockType.getId());
                }
            }
        }
    }

    /**
     * Calcule la hauteur du terrain en fonction des bruits de Perlin et du relief.
     */
    private int calculateTerrainHeight(float worldX, float worldZ, int maxReliefVariation) {
        // Couche de zonage à échelle intermédiaire pour créer des zones variées
        // Augmentation de la fréquence pour avoir plus de zones dans un monde 32x32
        float zoneScale = 0.004f; // Échelle augmentée pour plus de variété
        float zoneNoise = zonePerlinNoise.Noise2D(worldX * zoneScale, worldZ * zoneScale);
        
        // Déterminer le facteur de zone avec des transitions fluides
        float zoneFactor = calculateZoneFactor(zoneNoise);

        // Échelles différentes selon le relief
        float primaryScale = getPrimaryScale();
        float detailScale = getDetailScale();
        float ridgeScale = getRidgeScale();

        // Bruit principal (grandes structures)
        float primaryNoise = worldPerlinNoise.Noise2D(worldX * primaryScale, worldZ * primaryScale);
        
        // Bruit de détail (petites variations)
        float detailNoise = detailPerlinNoise.Noise2D(worldX * detailScale, worldZ * detailScale);
        
        // Bruit pour les crêtes (relief accidenté)
        float ridgeNoise = 0;
        if (reliefComplexity >= 3) {
            ridgeNoise = ridgePerlinNoise.Noise2D(worldX * ridgeScale, worldZ * ridgeScale);
            // Transformer en crêtes (valeurs proches de 0.5 donnent des pics)
            ridgeNoise = 1.0f - 2.0f * Math.abs(ridgeNoise - 0.5f);
        }

        // Combiner les bruits selon le type de relief
        float baseHeightFactor = combineNoise(primaryNoise, detailNoise, ridgeNoise);
        
        // Appliquer le facteur de zone avec une transition fluide
        float heightFactor = baseHeightFactor * zoneFactor;
        
        // Appliquer des courbes selon la température (volcans, glaciers, etc.)
        heightFactor = applyTemperatureModification(heightFactor, worldX, worldZ);
        
        // Calculer la hauteur finale
        int heightVariation = (int) (heightFactor * maxReliefVariation);
        return BASE_TERRAIN_LEVEL + heightVariation;
    }

    /**
     * Calcule le facteur de zone basé sur le bruit de zonage avec des transitions fluides.
     * Retourne une valeur entre 0 (zone plate) et 1 (zone montagneuse).
     * Utilise des fonctions de lissage au lieu de seuils durs pour éviter les "murs".
     * Tous les niveaux de relief ont maintenant des zones, mais à différentes intensités.
     */
    private float calculateZoneFactor(float zoneNoise) {
        switch (reliefComplexity) {
            case 0:
                // Relief minimal : FORCE un minimum élevé pour JAMAIS être plat
                // Garantit toujours entre 40% et 90% du relief maximum
                float factor0 = smoothStep(0.2f, 0.8f, zoneNoise);
                return 0.4f + factor0 * 0.5f; // Entre 40% et 90% de relief - JAMAIS plat

            case 1:
                // Relief doux : bon contraste avec minimum élevé
                float factor1 = smoothStep(0.2f, 0.75f, zoneNoise);
                return 0.3f + factor1 * 0.6f; // Entre 30% et 90% de relief

            case 2:
                // Relief modéré : contrastes nets entre vallées et montagnes moyennes
                float factor2 = smoothStep(0.25f, 0.75f, zoneNoise);
                return 0.2f + factor2 * 0.7f; // Entre 20% et 90% de relief

            case 3:
                // Relief accidenté : alternance marquée entre plaines et hautes collines
                float factor3 = smoothStep(0.2f, 0.8f, zoneNoise);
                return 0.25f + factor3 * 0.65f; // Entre 25% et 90% de relief

            case 4:
                // Utilise plusieurs courbes de lissage superposées au lieu de seuils nets
                
                // Facteur pour les vallées plates (zones très basses)
                float valleyFactor = 1.0f - smoothStep(0.0f, 0.3f, zoneNoise);
                
                // Facteur pour les hautes montagnes (zones très hautes) 
                float mountainFactor = smoothStep(0.7f, 1.0f, zoneNoise);
                
                // Facteur pour les collines intermédiaires
                float hillFactor = smoothStep(0.25f, 0.75f, zoneNoise) * (1.0f - valleyFactor) * (1.0f - mountainFactor);
                
                // Combinaison fluide des trois zones
                float baseLevel = 0.05f;  // Niveau minimal pour vallées
                float hillLevel = 0.4f;   // Niveau moyen pour collines
                float mountainLevel = 0.9f; // Niveau élevé pour montagnes
                
                return baseLevel * valleyFactor + 
                       hillLevel * hillFactor + 
                       mountainLevel * mountainFactor +
                       0.1f * (1.0f - valleyFactor - hillFactor - mountainFactor); // Niveau de base pour zones non définies

            default:
                return 0.5f;
        }
    }

    /**
     * Fonction de lissage (smoothstep) pour créer des transitions fluides.
     * Équivalent à la fonction smoothstep de GLSL.
     * 
     * @param edge0 Début de la transition
     * @param edge1 Fin de la transition  
     * @param x Valeur d'entrée
     * @return Valeur lissée entre 0 et 1
     */
    private float smoothStep(float edge0, float edge1, float x) {
        // Clamper x entre edge0 et edge1
        float t = Math.max(0.0f, Math.min(1.0f, (x - edge0) / (edge1 - edge0)));
        // Appliquer la courbe de lissage : 3t² - 2t³
        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Combine les différents bruits selon le relief.
     */
    private float combineNoise(float primaryNoise, float detailNoise, float ridgeNoise) {
        switch (reliefComplexity) {
            case 0: // Relief minimal : force un minimum plus élevé pour toujours avoir du relief
                float baseNoise0 = primaryNoise * 0.8f + detailNoise * 0.3f;
                return Math.max(0.2f, baseNoise0); // Force un minimum de 20% du bruit
            case 1: // Doux : seulement le bruit principal atténué
                return Math.max(0.1f, primaryNoise * 0.7f + detailNoise * 0.2f);
            case 2: // Modéré : bruit principal + un peu de détail
                return primaryNoise * 0.7f + detailNoise * 0.3f;
            case 3: // Accidenté : bruit principal + détail + début de crêtes
                return primaryNoise * 0.5f + detailNoise * 0.3f + ridgeNoise * 0.2f;
            case 4: // Montagneux : tous les bruits avec emphase sur les crêtes
                return primaryNoise * 0.4f + detailNoise * 0.2f + ridgeNoise * 0.4f;
            default:
                return primaryNoise;
        }
    }

    /**
     * Applique des modifications basées sur la température.
     */
    private float applyTemperatureModification(float heightFactor, float worldX, float worldZ) {
        if (temperature >= 4) {
            // Température torride : créer des volcans occasionnels
            float volcanoNoise = worldPerlinNoise.Noise2D(worldX * 0.005f, worldZ * 0.005f);
            if (volcanoNoise > 0.85f) {
                heightFactor += (volcanoNoise - 0.85f) * 6.0f; // Pics volcaniques
            }
        } else if (temperature <= 1) {
            // Température froide : adoucir les reliefs (érosion glaciaire)
            heightFactor *= 0.8f;
        }
        
        return Math.max(0, Math.min(1, heightFactor));
    }

    /**
     * Détermine le type de bloc à placer selon la position et les paramètres du biome.
     */
    private BlockType determineBlockType(int y, int terrainHeight, int waterLevel) {
        // Au-dessus du terrain : air ou eau
        if (y >= terrainHeight) {
            if (y <= waterLevel) {
                return activeBiome.getWaterBlock();
            } else {
                return BlockType.AIR;
            }
        }

        // Dans le terrain
        if (y == terrainHeight - 1) {
            // Surface
            if (y <= waterLevel) {
                // Sous l'eau : utiliser le bon bloc selon le biome
                return getUnderwaterSurfaceBlock();
            } else {
                return activeBiome.getSurfaceBlock();
            }
        } else if (y >= terrainHeight - 4) {
            // Sous-surface (3 blocs sous la surface)
            return activeBiome.getSubSurfaceBlock();
        } else {
            // Profondeur
            return activeBiome.getDeepBlock();
        }
    }
    
    /**
     * Détermine le type de bloc de surface à utiliser sous l'eau selon le biome.
     */
    private BlockType getUnderwaterSurfaceBlock() {
        // Marécage : toujours de la boue sous l'eau
        if (activeBiome.isSwamp()) {
            return BlockType.MUD;
        }
        
        // Jungle : sol riche sous l'eau
        if (activeBiome.isJungle()) {
            return BlockType.RICH_SOIL;
        }
        
        // Désert brûlant : sable rouge
        if (activeBiome.isHotDesert()) {
            return BlockType.RED_SAND;
        }
        
        // Volcanique : cendres ou sol volcanique
        if (activeBiome.isVolcanic()) {
            return BlockType.ASH;
        }
        
        // Arctique : neige sous l'eau gelée
        if (activeBiome.isArctic()) {
            return BlockType.SNOW;
        }
        
        // Toundra : permafrost
        if (activeBiome.isTundra()) {
            return BlockType.PERMAFROST;
        }
        
        // Autres zones très froides : permafrost
        if (temperature <= 0) {
            return BlockType.PERMAFROST;
        }
        
        // Oasis et zones tropicales : sable corallien ou sable normal
        if (temperature >= 3 && humidity >= 3) {
            return BlockType.CORAL_SAND;
        }
        
        // Logique par défaut selon la température
        if (temperature >= 4) {
            return BlockType.RED_SAND; // Déserts chauds
        } else if (temperature >= 3) {
            return BlockType.SAND; // Zones chaudes normales
        } else if (temperature <= 1) {
            return BlockType.CLAY; // Zones froides
        } else {
            return BlockType.SAND; // Défaut tempéré
        }
    }

    /**
     * Calcule le niveau d'eau selon la température et l'humidité.
     */
    private int calculateWaterLevel() {
        // Niveau de base plus élevé pour avoir vraiment de l'eau
        int baseWaterLevel = BASE_TERRAIN_LEVEL + 5;

        // Ajustements majeurs selon l'humidité
        if (humidity >= 4) baseWaterLevel += 20;      // Très humide : beaucoup d'eau
        else if (humidity >= 3) baseWaterLevel += 10; // Humide : pas mal d'eau
        else if (humidity >= 2) baseWaterLevel += 5;  // Modéré : un peu d'eau
        else if (humidity <= 1) baseWaterLevel -= 20; // Très sec : peu d'eau
        else baseWaterLevel -= 10; // Sec : moins d'eau

        // Ajustements selon la température
        if (temperature <= 1) baseWaterLevel += 0;   // Froid : eau gelée mais même niveau
        else if (temperature >= 4) baseWaterLevel -= 5; // Chaud : évaporation légère

        return Math.max(BASE_TERRAIN_LEVEL - 30, baseWaterLevel);
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    /**
     * Obtient l'échelle primaire selon le relief.
     */
    private float getPrimaryScale() {
        switch (reliefComplexity) {
            case 0: case 1: return 0.002f;  // Très grandes structures
            case 2: return 0.005f;          // Grandes structures
            case 3: return 0.008f;          // Structures moyennes
            case 4: return 0.012f;          // Structures plus petites mais plus marquées
            default: return 0.005f;
        }
    }

    /**
     * Obtient l'échelle de détail selon le relief.
     */
    private float getDetailScale() {
        return getPrimaryScale() * 4.0f; // Toujours 4x plus fin que le primaire
    }

    /**
     * Obtient l'échelle des crêtes selon le relief.
     */
    private float getRidgeScale() {
        return getPrimaryScale() * 2.0f; // 2x plus fin que le primaire
    }

    /**
     * Récupère les coordonnées du chunk à partir des globales.
     *
     * @param worldX Coordonnée mondiale X
     * @param worldY Coordonnée mondiale Y
     * @param worldZ Coordonnée mondiale Z
     * @return Les coordonnées du chunk sous forme de Vector3f
     */
    public Vector3f getChunkCoordAt(int worldX, int worldY, int worldZ) {
        // Calcul des coordonnées du chunk sans décalage
        int chunkX = Math.floorDiv(worldX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(worldY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(worldZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = worldX - chunkX * ChunkModel.SIZE;
        int localY = worldY - chunkY * ChunkModel.SIZE;
        int localZ = worldZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        return new Vector3f(cx, cy, cz);
    }

    private void generateTerrainFlat(int chunkX, int chunkZ){

        // Coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE - (float) (worldSizeX *
                ChunkModel.SIZE) / 2;
        float worldZStart = chunkZ * ChunkModel.SIZE - (float) (worldSizeZ *
                ChunkModel.SIZE) / 2;

        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int y = 0; y < ChunkModel.SIZE; y++) {
                for (int z = 0; z < ChunkModel.SIZE; z++) {

                    float worldX = worldXStart + x;
                    float worldZ = worldZStart + z;

                    if (y <= ChunkModel.SIZE/2) {
                        if (y == ChunkModel.SIZE/2){
                            setBlockAt((int) worldX, y, (int) worldZ, BlockType.GRASS.getId());
                        }
                        else {
                            setBlockAt((int) worldX, y, (int) worldZ, BlockType.STONE.getId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Ajoute des nuages aléatoires dans le ciel du monde.
     */
    private void addClouds() {
        int cloudY = 100; // Altitude moyenne des nuages
        int numClouds = 80; // Nombre total de nuages à générer
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < numClouds; i++) {
            // Générer des positions aléatoires pour les nuages
            int cloudX = random.nextInt(worldSizeX*ChunkModel.SIZE);
            int cloudZ = random.nextInt(worldSizeZ*ChunkModel.SIZE);

            // Générer une taille aléatoire pour le nuage
            int cloudSizeX = 50 + random.nextInt(80); // Taille entre 50 et 130 blocs
            int cloudSizeY = 2; // Hauteur de 2 blocs
            int cloudSizeZ = 50 + random.nextInt(80); // Taille entre 50 et 130 blocs

            // Créer le nuage
            createCloud(cloudX, cloudY, cloudZ, cloudSizeX, cloudSizeY, cloudSizeZ);
        }
    }

    /**
     * Crée un nuage à la position et aux dimensions spécifiées.
     */
    private void createCloud(int x, int y, int z, int size, int height, int depth) {
        java.util.Random random = new java.util.Random();
        int numPlaques = 2 + random.nextInt(4); // Entre 2 et 5 plaques par nuage
        java.util.List<int[]> bords = new java.util.ArrayList<>();

        // Première plaque, placée aléatoirement
        int plaqueSizeX = 4 + random.nextInt(size / 2);
        int plaqueSizeZ = 4 + random.nextInt(depth / 2);
        int plaqueY = y + random.nextInt(height);
        int offsetX = x + random.nextInt(size - plaqueSizeX + 1);
        int offsetZ = z + random.nextInt(depth - plaqueSizeZ + 1);
        for (int dx = 0; dx < plaqueSizeX; dx++) {
            for (int dz = 0; dz < plaqueSizeZ; dz++) {
                setBlockAt(offsetX + dx, plaqueY, offsetZ + dz, BlockType.CLOUD.getId());
                bords.add(new int[] { offsetX + dx, plaqueY, offsetZ + dz });
            }
        }

        // Plaques suivantes, toujours collées à une position déjà occupée
        for (int i = 1; i < numPlaques; i++) {
            // Choisir un point de départ parmi les bords existants
            int[] base = bords.get(random.nextInt(bords.size()));
            plaqueSizeX = 4 + random.nextInt(size / 2);
            plaqueSizeZ = 4 + random.nextInt(depth / 2);
            plaqueY = y + random.nextInt(height);
            // Décalage aléatoire autour du point de base (pour coller la plaque)
            int decalX = base[0] - random.nextInt(plaqueSizeX);
            int decalZ = base[2] - random.nextInt(plaqueSizeZ);
            for (int dx = 0; dx < plaqueSizeX; dx++) {
                for (int dz = 0; dz < plaqueSizeZ; dz++) {
                    int bx = decalX + dx;
                    int by = plaqueY;
                    int bz = decalZ + dz;
                    setBlockAt(bx, by, bz, BlockType.CLOUD.getId());
                    bords.add(new int[] { bx, by, bz });
                }
            }
        }
    }

    /**
     * Crée une île flottante au centre du monde (0,0,0).
     */
    private void createFloatingIsland() {

        int centerX = 0;
        int centerY = 30;
        int centerZ = 0;

        // Rayon de l'île
        int radiusXZ = 30; // Rayon horizontal
        int radiusY = 16;   // Hauteur/épaisseur

        // Générer la base de l'île
        for (int x = centerX - radiusXZ; x <= centerX + radiusXZ; x++) {
            for (int z = centerZ - radiusXZ; z <= centerZ + radiusXZ; z++) {

                double distanceSquared = Math.pow((x - centerX) / (radiusXZ * 0.8), 2)
                                      + Math.pow((z - centerZ) / (radiusXZ * 0.8), 2);

                // Si dans le rayon de l'île
                if (distanceSquared >= 1.0 + 10e-6 && distanceSquared <= 1.1) {
                    addInvisiblePillar(x, centerY, z);
                }

                if (distanceSquared <= 1.0) {
                    // Déterminer la hauteur à cette position
                    double heightFactor = 1 - Math.sqrt(distanceSquared);
                    int height = (int) (radiusY * heightFactor);

                    // Ajouter une variation aléatoire aux bords
                    if (distanceSquared > 0.6) {
                        height += random.nextInt(3) - 1;
                    }

                    for (int y = centerY - height; y <= centerY; y++) {

                        if (y == centerY) {
                            setBlockAt(x, y, z, BlockType.GRASS.getId());
                        } else if (y > centerY - 3) {
                            setBlockAt(x, y, z, BlockType.DIRT.getId());
                        } else {
                            setBlockAt(x, y, z, BlockType.STONE.getId());
                        }
                    }
                }
            }
        }



        // Ajouter quelques arbres
        addTrees(centerX, centerY, centerZ, radiusXZ);

        // Ajouter des nuages dans le ciel
        addClouds(centerX, centerY + 30, centerZ);
    }


    /**
     * Ajoute un pilier invisible à la position spécifiée pour empêcher le joueur de tomber.
     *
     * @param x Coordonnée X du pilier
     * @param y Coordonnée Y de référence (surface de l'île)
     * @param z Coordonnée Z du pilier
     */
    private void addInvisiblePillar(int x, int y, int z) {
        int pillarHeight = 30; // Hauteur du pilier vers le bas

        // Créer le pilier invisible du niveau de l'île vers le bas
        for (int dy = -10; dy <= pillarHeight; dy++) {
            setBlockAt(x,y + dy, z, BlockType.INVISIBLE.getId());
        }
    }


    /**
     * Ajoute des nuages dans le ciel du monde.
     *
     * @param centerX Le centre du monde en X
     * @param cloudY L'altitude des nuages
     * @param centerZ Le centre du monde en Z
     */
    private void addClouds(int centerX, int cloudY, int centerZ) {
        // Positions fixes des nuages (X, Z, altitude relative, taille)
        int[][] cloudPositions = {
            {0, 0, 0, 12},
            {30, 30, -5, 10},
            {-40, 25, 8, 8},
            {40, -35, -3, 9},
            {-30, -40, 5, 7},
            {50, 0, 10, 11},
            {-60, 0, -6, 10},
            {0, 50, 4, 9}
        };

        // Créer des nuages aux positions fixes
        for (int[] position : cloudPositions) {
            int cloudX = centerX + position[0];
            int cloudZ = centerZ + position[1];
            int thisCloudY = cloudY + position[2];  // Ajout de la variation d'altitude
            int cloudSize = position[3];            // Taille du nuage

            // Dimensions du nuage basées sur sa taille
            int cloudSizeX = cloudSize;
            int cloudSizeY = 2 + (cloudSize / 5);
            int cloudSizeZ = cloudSize + random.nextInt(3) - 1; // Légère variation

            createCloudIsland(cloudX, thisCloudY, cloudZ, cloudSizeX, cloudSizeY, cloudSizeZ);
        }
    }

    /**
     * Crée un nuage à la position et aux dimensions spécifiées.
     */
    private void createCloudIsland(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {

        // Créer le nuage avec une forme arrondie
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    // Calculer la distance normalisée au centre (forme ellipsoïdale)
                    double distanceSquared =
                        Math.pow((dx - sizeX/2.0) / (sizeX * 0.5), 2) +
                        Math.pow((dy - sizeY/2.0) / (sizeY * 0.7), 2) +
                        Math.pow((dz - sizeZ/2.0) / (sizeZ * 0.5), 2);

                    double noise = random.nextDouble() * 0.3;

                    // Si le point est dans l'ellipsoïde du nuage
                    if (distanceSquared + noise <= 1.0) {
                        if (distanceSquared > 0.7 && random.nextDouble() > 0.7) {
                            continue;
                        }

                        // Convertir les coordonnées centrées en coordonnées de grille
                        int gridX = x + dx;
                        int gridY = y + dy;
                        int gridZ = z + dz;

                        // Placer le bloc de nuage
                        setBlockAt(gridX, gridY, gridZ, BlockType.CLOUD.getId());
                    }
                }
            }
        }
    }

    /**
     * Ajoute quelques arbres sur l'île.
     */
    private void addTrees(int centerX, int centerY, int centerZ, int radiusXZ) {

        // Positions fixes des arbres (angles en degrés, distance en pourcentage du radius)
        int[][] treePositions = {
            {45, 40, 4},
            {225, 45, 6},
            {315, 55, 5},
            {0, 0, 5},
            {90, 58, 7},
            {180, 55, 8},
            {270, 47, 8}
        };

        // Créer les arbres aux positions fixes
        for (int[] position : treePositions) {
            int angleDegrees = position[0];
            int distancePercent = position[1];
            int height = position[2];

            // Convertir l'angle en radians
            double angleRadians = Math.toRadians(angleDegrees);
            double distance = (distancePercent / 100.0) * radiusXZ;

            int treeX = centerX + (int)(Math.cos(angleRadians) * distance);
            int treeZ = centerZ + (int)(Math.sin(angleRadians) * distance);

            // Vérifier que l'emplacement est de l'herbe (utilise maintenant les coordonnées de grille)
            if (getBlockAt(treeX, centerY, treeZ) == BlockType.GRASS.getId()) {
                createTree(treeX, centerY + 1, treeZ, height);
            }
        }
    }

     /**
     * Crée un arbre à la position spécifiée.
     */
    private void createTree(int x, int y, int z, int height) {

        // Créer le tronc
        for (int i = 0; i < height; i++) {
            // Convertir en coordonnées de grille
            setBlockAt(x, y + i, z, BlockType.LOG.getId());
        }

        // Créer le feuillage - centré au sommet du tronc
        int leavesRadius = 3;
        int foliageCenter = y + height; // Position du centre du feuillage
        
        for (int dx = -leavesRadius; dx <= leavesRadius; dx++) {
            for (int dy = -leavesRadius; dy <= leavesRadius; dy++) { // Symétrique maintenant
                for (int dz = -leavesRadius; dz <= leavesRadius; dz++) {
                    // Distance au centre du feuillage
                    double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

                    // Si dans le rayon des feuilles
                    if (distance <= leavesRadius + 0.5) {
                        int blockX = x + dx;
                        int blockY = foliageCenter + dy;
                        int blockZ = z + dz;

                        // Ne pas remplacer le tronc (partie qui dépasse dans le feuillage)
                        if (!(dx == 0 && dz == 0 && blockY <= foliageCenter)) {
                            if (distance < leavesRadius || random.nextDouble() < 0.6) {
                                setBlockAt(blockX, blockY, blockZ, BlockType.LEAVES.getId());
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Récupère le type de bloc à partir de coordonnées globales.
     * Convertit les coordonnées globales en coordonnées de chunk et locales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @return L'identifiant du type de bloc, ou 0 (AIR) si en dehors du monde
     */
    public int getBlockAt(int globalX, int globalY, int globalZ) {
        // Calcul des coordonnées du chunk
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return BlockType.AIR.getId(); // AIR pour tout ce qui est en dehors du monde
        }

        // Récupération du type de bloc dans le chunk
        return chunks[cx][cy][cz].getBlock(localX, localY, localZ);
    }
    
    /**
     * Modifie le type de bloc à partir de coordonnées globales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @param blockType Identifiant du type de bloc
     * @return true si le bloc a été modifié, false si hors des limites
     */
    public boolean setBlockAt(int globalX, int globalY, int globalZ, int blockType) {
        // Calcul des coordonnées du chunk sans décalage
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            // System.out.println("Bloc : " + BlockType.fromId(blockType) + " hors des limites du monde, globalX: " + globalX + ", globalY: " + globalY + ", globalZ: " + globalZ);
            return false;
        }

        // Modification du bloc dans le chunk
        chunks[cx][cy][cz].setBlock(localX, localY, localZ, blockType);
        return true;
    }

    /**
     * Active ou désactive le mode filaire.
     * 
     * @return Le nouvel état du mode filaire
     */
    public boolean toggleWireframe() {
        wireframeMode = !wireframeMode;
        return wireframeMode;
    }

    /**
     * Active ou désactive l'éclairage.
     * 
     * @return Le nouvel état de l'éclairage
     */
    public boolean toggleLightning() {
        lightningMode = !lightningMode;
        return lightningMode;
    }

    /**
     * Récupère l'état actuel du mode d'éclairage.
     * 
     * @return true si l'éclairage est activé, false sinon
     */
    public boolean getLightningMode() {
        return lightningMode;
    }

    /**
     * Récupère l'état actuel du mode filaire.
     * 
     * @return true si le mode filaire est activé, false sinon
     */
    public boolean getWireframeMode() {
        return wireframeMode;
    }

    /**
     * Récupère le chunk aux coordonnées spécifiées.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     * @return Le chunk à cette position, ou null si hors limites
     */
    public ChunkModel getChunk(int chunkX, int chunkY, int chunkZ) {
        if (chunkX >= 0 && chunkX < worldSizeX && 
            chunkY >= 0 && chunkY < worldSizeY && 
            chunkZ >= 0 && chunkZ < worldSizeZ) {
            return chunks[chunkX][chunkY][chunkZ];
        }
        return null;
    }

    /**
     * Récupère la taille du monde en X.
     */
    public int getWorldSizeX() {
        return worldSizeX;
    }

    /**
     * Récupère la taille du monde en Y.
     */
    public int getWorldSizeY() {
        return worldSizeY;
    }

    /**
     * Récupère la taille du monde en Z.
     */
    public int getWorldSizeZ() {
        return worldSizeZ;
    }

    public int getWorldSeed(){
        return worldSeed;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Définit une nouvelle taille pour le monde (axes X et Z).
     * Note: Cette méthode ne redimensionne pas le monde existant.
     * 
     * @param worldSize La nouvelle taille du monde
     */
    public void setWorldSize(int worldSize) {
        this.worldSizeX = worldSize;
        this.worldSizeZ = worldSize;
    }

    /**
     * Définit une nouvelle taille pour l'axe X du monde.
     * Note: Cette méthode ne redimensionne pas le monde existant.
     * 
     * @param worldSizeX La nouvelle taille en X
     */
    public void setWorldSizeX(int worldSizeX) {
        this.worldSizeX = worldSizeX;
    }

    /**
     * Définit une nouvelle taille pour l'axe Y du monde.
     * Note: Cette méthode ne redimensionne pas le monde existant.
     * 
     * @param worldSizeY La nouvelle taille en Y
     */
    public void setWorldSizeY(int worldSizeY) {
        this.worldSizeY = worldSizeY;
    }

    /**
     * Définit une nouvelle taille pour l'axe Z du monde.
     * Note: Cette méthode ne redimensionne pas le monde existant.
     * 
     * @param worldSizeZ La nouvelle taille en Z
     */
    public void setWorldSizeZ(int worldSizeZ) {
        this.worldSizeZ = worldSizeZ;
    }

    /**
     * Récupère le niveau de température du monde.
     * @return La température (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * Définit le niveau de température du monde.
     * @param temperature La température (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public void setTemperature(int temperature) {
        this.temperature = Math.max(0, Math.min(4, temperature)); // Clamp entre 0 et 4
    }

    /**
     * Récupère le niveau d'humidité du monde.
     * @return L'humidité (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * Définit le niveau d'humidité du monde.
     * @param humidity L'humidité (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public void setHumidity(int humidity) {
        this.humidity = Math.max(0, Math.min(4, humidity)); // Clamp entre 0 et 4
    }

    /**
     * Récupère le niveau de complexité du relief du monde.
     * @return La complexité du relief (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public int getReliefComplexity() {
        return reliefComplexity;
    }

    /**
     * Définit le niveau de complexité du relief du monde.
     * @param reliefComplexity La complexité du relief (0 = Minimal, 1 = Faible, 2 = Modéré, 3 = Élevé, 4 = Maximum)
     */
    public void setReliefComplexity(int reliefComplexity) {
        this.reliefComplexity = Math.max(0, Math.min(4, reliefComplexity)); // Clamp entre 0 et 4
    }

    /**
     * Configure tous les paramètres environnementaux en une seule fois.
     * @param temperature Niveau de température (0-4)
     * @param humidity Niveau d'humidité (0-4)
     * @param reliefComplexity Niveau de complexité du relief (0-4)
     */
    public void setEnvironmentalParameters(int temperature, int humidity, int reliefComplexity) {
        this.setTemperature(temperature);
        this.setHumidity(humidity);
        this.setReliefComplexity(reliefComplexity);
    }

    public void update(float tpf) {
        // Mettre à jour toutes les entités
        if (entityManager != null) {
            entityManager.updateAll(tpf);
        }
    }

    /**
     * Retourne le label correspondant au niveau de température
     */
    private String getTemperatureLabel(int temperature) {
        String[] labels = {"Minimal", "Faible", "Modéré", "Élevé", "Maximum"};
        return labels[Math.max(0, Math.min(4, temperature))];
    }
    
    /**
     * Retourne le label correspondant au niveau d'humidité
     */
    private String getHumidityLabel(int humidity) {
        String[] labels = {"Minimal", "Faible", "Modéré", "Élevé", "Maximum"};
        return labels[Math.max(0, Math.min(4, humidity))];
    }
    
    /**
     * Retourne le label correspondant au niveau de relief
     */
    private String getReliefLabel(int relief) {
        String[] labels = {"Minimal", "Faible", "Modéré", "Élevé", "Maximum"};
        return labels[Math.max(0, Math.min(4, relief))];
    }

    /**
     * Trouve la hauteur du sol à une position donnée (X, Z).
     * Recherche depuis le haut du monde jusqu'à trouver le premier bloc solide (non-AIR).
     * 
     * @param globalX Coordonnée globale X
     * @param globalZ Coordonnée globale Z
     * @return La hauteur Y du sol, ou -1 si aucun bloc solide n'est trouvé
     */
    public int getGroundHeightAt(int globalX, int globalZ) {
        // Calculer la hauteur maximale du monde en voxels
        int maxWorldHeight = worldSizeY * ChunkModel.SIZE;
        
        // Rechercher depuis le haut du monde vers le bas
        for (int y = maxWorldHeight - 1; y >= 0; y--) {
            int blockType = getBlockAt(globalX, y, globalZ);
            
            // Si ce n'est pas de l'air, c'est le sol
            // > à AIR pour inclure VOID et INVISIBLE
            if (blockType > BlockType.AIR.getId() && blockType != BlockType.CLOUD.getId()) {
                return y + 1; // Retourner la position juste au-dessus du bloc solide
            }
        }
        
        // Si aucun bloc solide n'est trouvé, retourner -1
        return -1;
    }

    /**
     * Récupère le biome actif du monde.
     * @return Le biome actuellement configuré pour ce monde
     */
    public BiomeType getActiveBiome() {
        return activeBiome;
    }

    /**
     * Modifie le type de bloc et l'ID de structure à partir de coordonnées globales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @param blockType Identifiant du type de bloc
     * @param structureId ID de la structure propriétaire (0 si aucune)
     * @return true si le bloc a été modifié, false si hors des limites
     */
    public boolean setBlockAt(int globalX, int globalY, int globalZ, int blockType, int structureId) {
        // Calcul des coordonnées du chunk sans décalage
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return false;
        }

        // Modification du bloc et de l'ID de structure dans le chunk
        chunks[cx][cy][cz].setBlock(localX, localY, localZ, blockType);
        chunks[cx][cy][cz].setStructureId(localX, localY, localZ, structureId);
        return true;
    }
    
    /**
     * Récupère l'ID de la structure propriétaire d'un bloc.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @return L'ID de la structure (0 si aucune structure)
     */
    public int getStructureIdAt(int globalX, int globalY, int globalZ) {
        // Calcul des coordonnées du chunk
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return 0; // Aucune structure pour tout ce qui est en dehors du monde
        }

        // Récupération de l'ID de structure dans le chunk
        return chunks[cx][cy][cz].getStructureId(localX, localY, localZ);
    }
}