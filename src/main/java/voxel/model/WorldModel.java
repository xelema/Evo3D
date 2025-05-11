package voxel.model;

/**
 * Représente le monde de voxels complet, composé de plusieurs chunks.
 * Cette classe gère uniquement les données du monde sans le rendu.
 */
public class WorldModel {
    /** Taille du monde en nombre de chunks sur les axes X et Z */
    public static final int WORLD_SIZE = 32;

    /** Tableau 3D contenant tous les chunks du monde */
    private ChunkModel[][][] chunks;

    /** Taille du monde en nombre de chunks sur l'axe X */
    private final int worldSizeX = WORLD_SIZE;

    /** Taille du monde en nombre de chunks sur l'axe Y */
    private final int worldSizeY = 4;

    /** Taille du monde en nombre de chunks sur l'axe Z */
    private final int worldSizeZ = WORLD_SIZE;

    /** Eclairage oui ou non */
    private boolean lightningMode = true;

    /** Mode filaire activé ou non */
    private boolean wireframeMode = false;

    /**
     * Valeurs pour definir l'echelle des montagne et des details dans le bruit de
     * Perlin
     */
    private final float min_mountain = 0.001f;
    private final float max_mountain = 0.03f; // Réduit pour des montagnes moins abruptes
    private final float min_detail = 0.02f;
    private final float max_detail = 0.06f; // Réduit pour des détails moins prononcés

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
     * Crée un nouveau monde de voxels.
     */
    public WorldModel(BiomeType biome) {
        this.activeBiome = biome;

        chunks = new ChunkModel[worldSizeX][worldSizeY][worldSizeZ];

        // Initialisation du bruit de Perlin pour tout le monde
        worldPerlinNoise = new PerlinNoise(42); // Seed fixe pour reproductibilité

        // Initialisation des échelles (on pourrait les randomiser aussi)
        java.util.Random rand = new java.util.Random();
        mountainScale = min_mountain + rand.nextFloat() * (max_mountain - min_mountain) * 0.5f;
        detailScale = min_detail + rand.nextFloat() * (max_detail - min_detail) * 0.8f;

        generateWorld();
    }

    /**
     * Génère le monde complet avec tous ses chunks.
     */
    private void generateWorld() {
        // Initialisation de tous les chunks avec de l'air
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    chunks[cx][cy][cz] = new ChunkModel();
                }
            }
        }

        // Génération du terrain uniquement sur le plan X-Z
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cz = 0; cz < worldSizeZ; cz++) {
                generateTerrainPerlin(cx, cz, 0);
            }
        }

        // Ajout des nuages aléatoires dans le ciel
        addClouds();
    }

    /**
     * Génère un terrain avec un bruit de Perlin pour une colonne de chunks
     * 
     * @param chunkX coordonnée X du chunk
     * @param chunkZ coordonnée Z du chunk
     */
    private void generateTerrainPerlin(int chunkX, int chunkZ, int type) {
        int waterLevel = 40;

        // Calcul des coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE;
        float worldZStart = chunkZ * ChunkModel.SIZE;

        // Hauteur totale et maximale du terrain
        int totalHeight = worldSizeY * ChunkModel.SIZE;
        int maxTerrainHeight = (int) (totalHeight * maxHeightPercent);

        // Parcourir chaque colonne du chunk
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int z = 0; z < ChunkModel.SIZE; z++) {
                // Coordonnées globales du bloc
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
                            waterLevel = 20;
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
                            waterLevel = 20;
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

                    // Placer le bloc à la position correspondante
                    setBlockAt(chunkX * ChunkModel.SIZE + x, y, chunkZ * ChunkModel.SIZE + z, blockType);
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
            int cloudX = random.nextInt(worldSizeX * ChunkModel.SIZE);
            int cloudZ = random.nextInt(worldSizeZ * ChunkModel.SIZE);

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
     * Récupère le type de bloc à partir de coordonnées globales.
     * Convertit les coordonnées globales en coordonnées de chunk et locales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @return L'identifiant du type de bloc, ou 0 (AIR) si en dehors du monde
     */
    public int getBlockAt(int globalX, int globalY, int globalZ) {
        // Calcul des coordonnées du chunk qui contient cette position
        int cx = Math.floorDiv(globalX, ChunkModel.SIZE);
        int cy = Math.floorDiv(globalY, ChunkModel.SIZE);
        int cz = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return BlockType.AIR.getId(); // AIR pour tout ce qui est en dehors du monde
        }

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - cx * ChunkModel.SIZE;
        int localY = globalY - cy * ChunkModel.SIZE;
        int localZ = globalZ - cz * ChunkModel.SIZE;

        // Récupération du type de bloc dans le chunk
        return chunks[cx][cy][cz].getBlock(localX, localY, localZ);
    }

    /**
     * Modifie le type de bloc à partir de coordonnées globales.
     * 
     * @param globalX   Coordonnée globale X
     * @param globalY   Coordonnée globale Y
     * @param globalZ   Coordonnée globale Z
     * @param blockType Identifiant du type de bloc
     * @return true si le bloc a été modifié, false si hors des limites
     */
    public boolean setBlockAt(int globalX, int globalY, int globalZ, int blockType) {
        // Calcul des coordonnées du chunk qui contient cette position
        int cx = Math.floorDiv(globalX, ChunkModel.SIZE);
        int cy = Math.floorDiv(globalY, ChunkModel.SIZE);
        int cz = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return false;
        }

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - cx * ChunkModel.SIZE;
        int localY = globalY - cy * ChunkModel.SIZE;
        int localZ = globalZ - cz * ChunkModel.SIZE;

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
}