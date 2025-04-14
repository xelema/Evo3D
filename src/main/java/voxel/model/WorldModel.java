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

    /** Valeurs pour definir l'echelle des montagne et des details dans le bruit de Perlin */
    private final float min_mountain = 0.001f;
    private final float max_mountain = 0.03f;  // Réduit pour des montagnes moins abruptes
    private final float min_detail = 0.02f;
    private final float max_detail = 0.06f;    // Réduit pour des détails moins prononcés
    
    /** Bruit de Perlin pour le monde entier */
    private PerlinNoise worldPerlinNoise;

    /** Bruit de Perlin pour les biomes */
    private PerlinNoise biomeNoise;
    
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
                generateTerrainPerlin(cx, cz);
            }
        }
    }
    
    /**
     * Génère un terrain avec un bruit de Perlin pour une colonne de chunks
     * @param chunkX coordonnée X du chunk
     * @param chunkZ coordonnée Z du chunk
     */
    private void generateTerrainPerlin(int chunkX, int chunkZ) {
        // Niveau de l'eau (abaissé par rapport à la version précédente)
        int waterLevel = 40;
        
        // Coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE;
        float worldZStart = chunkZ * ChunkModel.SIZE;
        
        // Hauteur totale disponible
        int totalHeight = worldSizeY * ChunkModel.SIZE;
        // Hauteur maximale pour le terrain (avec marge pour éviter les coupures)
        int maxTerrainHeight = (int)(totalHeight * maxHeightPercent);

        // Générer chaque colonne de blocs
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int z = 0; z < ChunkModel.SIZE; z++) {
                // Coordonnées globales pour ce bloc spécifique
                float worldX = worldXStart + x;
                float worldZ = worldZStart + z;

                // Appliquer un bruit Perlin 2D pour obtenir la hauteur globale des montagnes
                float mountainNoise = worldPerlinNoise.Noise2D(worldX * mountainScale, worldZ * mountainScale);
                
                // Ajouter des détails avec un autre bruit Perlin
                float detailNoise = worldPerlinNoise.Noise2D(worldX * detailScale, worldZ * detailScale);
                
                // Combiner les bruits pour avoir une hauteur réaliste
                float heightFactor = mountainNoise * 0.7f + detailNoise * 0.3f;
                
                // Appliquer des règles spécifiques au biome
                switch (this.activeBiome) {
                    case MOUNTAINS:
                        heightFactor *= 1.5f; // Terrain très accidenté
                        waterLevel = 50;
                        break;
                    case PLAINS:
                        heightFactor *= 0.4f; // Terrain plat
                        waterLevel = 20; // Niveau d'eau plus bas
                        break;
                    case DESERT:
                        heightFactor *= 0.4f; // Terrain plat
                        waterLevel = 10; // Niveau d'eau très bas (presque pas d'eau)
                        break;
                    case JUNGLE:
                        heightFactor *= 0.7f; // Terrain vallonné
                        waterLevel = 20; // Niveau d'eau plus bas
                        break;
                    case SNOWY:
                        heightFactor *= 1.0f; // Terrain normal
                        waterLevel = 30; // Niveau d'eau plus bas
                        break;
                    case SAVANNA:
                        heightFactor *= 0.5f; // Terrain légèrement vallonné
                        waterLevel = 20; // Niveau d'eau plus bas
                        break;
                }

                // Appliquer une courbe d'élévation pour accentuer les différences de hauteur
                heightFactor = (float)Math.pow(heightFactor, 1.2);


                // Calculer la hauteur du terrain pour cette colonne
                // Limiter à maxTerrainHeight pour éviter que le terrain touche le haut du monde
                int baseTerrainHeight = baseOffset + (int)(heightFactor * (maxTerrainHeight - baseOffset));
                
                // Assurer une hauteur minimale
                int terrainHeight = Math.max(waterLevel - 5, baseTerrainHeight);
                
                // Limiter la hauteur maximale
                terrainHeight = Math.min(maxTerrainHeight, terrainHeight);

                // Générer les blocs pour chaque coordonnée Y
                for (int y = 0; y < totalHeight; y++) {
                    // Déterminer le type de bloc en fonction de la hauteur
                    int blockType;
                    
                    if (y <= waterLevel) {
                        if (y < terrainHeight) {
                            // Sous le terrain et sous l'eau
                            switch (this.activeBiome) {
                                case DESERT:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.SAND.getId();
                                    break;
                                case SAVANNA:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1) blockType = BlockType.SAVANNA_GRASS.getId();
                                    break;
                                case JUNGLE:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1) blockType = BlockType.JUNGLE_GRASS.getId();
                                    break;
                                case SNOWY:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1) blockType = BlockType.SNOW.getId(); // Ajouter un type de bloc neige
                                    break;
                                default:
                                    blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                    if (y == terrainHeight - 1) blockType = BlockType.GRASS.getId();
                                    break;
                            }
                    
                            // Si on est près de la surface du terrain et sous l'eau, mettre du sable
                            if (y > terrainHeight - 4 && y == terrainHeight - 1) {
                                blockType = BlockType.SAND.getId();
                            }
                        } else {
                            // Au-dessus du terrain mais sous l'eau -> eau
                            blockType = BlockType.WATER.getId();
                    
                            // Le fond de l'eau est du sable
                            if (y == terrainHeight) {
                                blockType = BlockType.SAND.getId();
                            }
                        }
                    } else if (y < terrainHeight) {
                        // Au-dessus du niveau d'eau, sous la surface du terrain
                        switch (this.activeBiome) {
                            case DESERT:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.SAND.getId();
                                break;
                            case SAVANNA:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1) blockType = BlockType.SAVANNA_GRASS.getId();
                                break;
                            case JUNGLE:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1) blockType = BlockType.JUNGLE_GRASS.getId();
                                break;
                            case SNOWY:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1) blockType = BlockType.SNOW.getId(); // Ajouter un type de bloc neige
                                break;
                            default:
                                blockType = (y < terrainHeight - 3) ? BlockType.STONE.getId() : BlockType.DIRT.getId();
                                if (y == terrainHeight - 1) blockType = BlockType.GRASS.getId();
                                break;
                        }
                    } else {
                        // Au-dessus du terrain, air
                        blockType = BlockType.AIR.getId();
                    }
                    
                    // Placer le bloc au bon endroit
                    setBlockAt(
                        chunkX * ChunkModel.SIZE + x,
                        y,
                        chunkZ * ChunkModel.SIZE + z,
                        blockType
                    );
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
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
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