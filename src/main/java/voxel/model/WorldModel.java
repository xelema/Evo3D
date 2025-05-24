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
    public static final int DEFAULT_WORLD_SIZE = 32;
    
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
    private final float min_mountain = 0.001f;
    private final float max_mountain = 0.03f;
    private final float min_detail = 0.02f;
    private final float max_detail = 0.06f;

    /** Bruit de Perlin pour le monde entier */
    private PerlinNoise worldPerlinNoise;

    /** Échelles pour la génération du terrain */
    private float mountainScale;
    private float detailScale;

    /** Hauteur maximale en pourcentage de la hauteur totale du monde */
    private final float maxHeightPercent = 0.75f;

    /** Décalage vertical de base pour tout le terrain */
    private final int baseOffset = 10;

    /** Biome actif */
    private BiomeType activeBiome;

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
        this.activeBiome = biome;
        this.worldSizeX = worldSize;
        this.worldSizeY = 8; // Hauteur fixe pour l'instant
        this.worldSizeZ = worldSize;

        chunks = new ChunkModel[worldSizeX][worldSizeY][worldSizeZ];

        // Initialisation du bruit de Perlin pour tout le monde
        worldPerlinNoise = new PerlinNoise(worldSeed); // Seed fixe pour reproductibilité

        // Initialisation des échelles (on pourrait les randomiser aussi)
        java.util.Random rand = new java.util.Random(worldSeed);
        mountainScale = min_mountain + rand.nextFloat() * (max_mountain - min_mountain) * 0.5f;
        detailScale = min_detail + rand.nextFloat() * (max_detail - min_detail) * 0.8f;

        generateWorld(false);
        entityManager = new EntityManager(this);
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
                if (activeBiome == BiomeType.FLOATING_ISLAND) {
                    // Créer une île flottante au centre du monde
                    createFloatingIsland();
                } else if(flat){
                    generateTerrainFlat(cx,cz);
                } else {
                    generateTerrainPerlin(cx, cz, 1);
                }
            }
        }

        // Ajout des nuages aléatoires dans le ciel
        addClouds();
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
     * Génère un terrain avec un bruit de Perlin pour une colonne de chunks
     * @param chunkX coordonnée X du chunk
     * @param chunkZ coordonnée Z du chunk
     */
    private void generateTerrainPerlin(int chunkX, int chunkZ, int type) {
        // Niveau de l'eau
        int waterLevel = 50;

        // Coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE - (float) (worldSizeX *
                ChunkModel.SIZE) / 2;
        float worldZStart = chunkZ * ChunkModel.SIZE - (float) (worldSizeZ *
                ChunkModel.SIZE) / 2;

        // Hauteur totale disponible
        int totalHeight = generation_height * ChunkModel.SIZE;
        // Hauteur maximale pour le terrain (avec marge pour éviter les coupures)
        int maxTerrainHeight = (int)(totalHeight * maxHeightPercent);

        // Générer chaque colonne de blocs
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int z = 0; z < ChunkModel.SIZE; z++) {
                // Coordonnées globales pour ce bloc spécifique
                float worldX = worldXStart + x;
                float worldZ = worldZStart + z;

                float heightFactor = 0.0f;

                // Type 0 : méthode 1 (complexe à 3 niveaux de bruit)
                if (type == 0) {
                    float largeScale = 0.01f;
                    float mediumScale = 0.05f;
                    float smallScale = 0.1f;

                    // Appliquer trois niveaux de bruit Perlin
                    float largeNoise = worldPerlinNoise.Noise2D(worldX * largeScale, worldZ * largeScale);
                    float mediumNoise = worldPerlinNoise.Noise2D(worldX * mediumScale, worldZ * mediumScale);
                    float smallNoise = worldPerlinNoise.Noise2D(worldX * smallScale, worldZ * smallScale);

                    // Combiner les différents bruits pour obtenir une hauteur réaliste
                    heightFactor = largeNoise * 0.6f + mediumNoise * 0.3f + smallNoise * 0.1f;

                    // Appliquer un facteur d'atténuation si le bruit large est faible
                    if (largeNoise < 0.1f)
                        heightFactor *= 0.3f;

                    // Appliquer des réglages spécifiques selon le biome actif
                    switch (this.activeBiome) {
                        case MOUNTAINS:
                            heightFactor = largeNoise * 3.8f + mediumNoise * 1.8f;
                            waterLevel = 0;
                            break;
                        case PLAINS:
                            heightFactor = largeNoise * 0.2f + mediumNoise * 0.4f;
                            waterLevel = 25;
                            break;
                        case DESERT:
                            heightFactor = largeNoise * 0.1f + mediumNoise * 0.3f;
                            waterLevel = 0;
                            break;
                        case JUNGLE:
                            heightFactor = largeNoise * 0.6f + mediumNoise * 0.9f;
                            waterLevel = 14;
                            break;
                        case SNOWY:
                            heightFactor = largeNoise * 1.2f + mediumNoise * 0.8f;
                            waterLevel = 8;
                            break;
                        case SAVANNA:
                            heightFactor = largeNoise * 0.5f + mediumNoise * 0.3f;
                            waterLevel = 26;
                            break;
                    }

                    // Type 1 : méthode 2 (plus simple à 2 niveaux de bruit)
                } else if (type == 1) {
                    // Appliquer deux niveaux de bruit Perlin
                    float mountainNoise = worldPerlinNoise.Noise2D(worldX * mountainScale, worldZ * mountainScale);
                    float detailNoise = worldPerlinNoise.Noise2D(worldX * detailScale, worldZ * detailScale);

                    // Combiner les bruits pour une hauteur réaliste
                    heightFactor = mountainNoise * 0.7f + detailNoise * 0.3f;

                    // Appliquer des ajustements spécifiques pour chaque biome
                    switch (this.activeBiome) {
                        case MOUNTAINS:
                            heightFactor *= 1.5f;
                            waterLevel = 50;
                            break;
                        case PLAINS:
                            heightFactor *= 0.4f;
                            waterLevel = 17;
                            break;
                        case DESERT:
                            heightFactor *= 0.4f;
                            waterLevel = 10;
                            break;
                        case JUNGLE:
                            heightFactor *= 0.7f;
                            waterLevel = 20;
                            break;
                        case SNOWY:
                            heightFactor *= 1.0f;
                            waterLevel = 30;
                            break;
                        case SAVANNA:
                            heightFactor *= 0.5f;
                            waterLevel = 26;
                            break;
                    }
                }

                // Appliquer une courbe d'élévation pour accentuer les différences de hauteur
                heightFactor = (float) Math.pow(heightFactor, 1.2);

                // Calculer la hauteur du terrain en fonction de la hauteur de base
                int baseTerrainHeight = baseOffset + (int) (heightFactor * (maxTerrainHeight - baseOffset));

                // Limiter la hauteur du terrain et l'aligner avec le niveau de l'eau
                int terrainHeight = Math.max(waterLevel - 5, baseTerrainHeight);
                terrainHeight = Math.min(maxTerrainHeight, terrainHeight);

                // Générer les blocs pour chaque hauteur de terrain
                for (int y = 0; y < totalHeight; y++) {
                    int blockType;

                    // Si le bloc est sous le niveau de l'eau
                    if (y <= waterLevel) {
                        if (y < terrainHeight) {
                            // Appliquer le type de bloc en fonction du biome
                            switch (this.activeBiome) {
                                case DESERT:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId()
                                            : BlockType.SAND.getId();
                                    break;
                                case SAVANNA:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId()
                                            : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1)
                                        blockType = BlockType.SAVANNA_GRASS.getId();
                                    break;
                                case JUNGLE:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId()
                                            : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1)
                                        blockType = BlockType.JUNGLE_GRASS.getId();
                                    break;
                                case SNOWY:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId()
                                            : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1)
                                        blockType = BlockType.SNOW.getId();
                                    break;
                                default:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId()
                                            : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1)
                                        blockType = BlockType.GRASS.getId();
                                    break;
                            }

                            // Sable près de la surface sous l'eau
                            if (y > terrainHeight - 4 && y == terrainHeight - 1) {
                                blockType = BlockType.SAND.getId();
                            }
                        } else {
                            // Bloc d'eau
                            blockType = BlockType.WATER.getId();
                            if (y == terrainHeight)
                                blockType = BlockType.SAND.getId(); // Sable au fond de l'eau
                        }
                    } else if (y < terrainHeight) {
                        // Si le bloc est au-dessus du niveau d'eau mais sous la surface
                        switch (this.activeBiome) {
                            case DESERT:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.SAND.getId();
                                break;
                            case SAVANNA:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1)
                                    blockType = BlockType.SAVANNA_GRASS.getId();
                                break;
                            case JUNGLE:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1)
                                    blockType = BlockType.JUNGLE_GRASS.getId();
                                break;
                            case SNOWY:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1)
                                    blockType = BlockType.SNOW.getId();
                                break;
                            default:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1)
                                    blockType = BlockType.GRASS.getId();
                                break;
                        }
                    } else {
                        // Bloc d'air au-dessus du terrain
                        blockType = BlockType.AIR.getId();
                    }

                    // Placer le bloc au bon endroit
                    setBlockAt((int) worldX, y, (int) worldZ, blockType);
                    // setBlockAt(chunkX * ChunkModel.SIZE + x, y, chunkZ * ChunkModel.SIZE + z, blockType);
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
        int centerY = 150;
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

    public void update(float tpf) {
        // Mettre à jour toutes les entités
        if (entityManager != null) {
            entityManager.updateAll(tpf);
        }
    }
}