package voxel.model;
import java.util.Random;

/**
 * Représente un chunk (section) du monde de voxels.
 * Un chunk est un cube de taille fixe contenant des blocs de différents types.
 * Cette classe stocke uniquement les données du chunk sans le rendu.
 */
public class ChunkModel {
    /** Taille du chunk en nombre de blocs dans chaque dimension */
    public static final int SIZE = 128 ;
    
    /** Tableau contenant les identifiants des blocs du chunk */
    private final int[] blocks;

    /** Valeurs pour definir l'echelle des montagne et des details dans le bruit de Perlin */
    private final float min_mountain = 0.0005f;
    private final float max_mountain = 0.02f;
    private final float min_detail = 0.02f;
    private final float max_detail = 0.1f;
    
    /**
     * Crée un nouveau chunk avec une génération de terrain par défaut.
     */
    public ChunkModel() {
        blocks = new int[SIZE * SIZE * SIZE];
        generateTerrain();
    }

    /**
     * Crée un nouveau chunk avec une génération de terrain par défaut.
     */
    public ChunkModel(int chunkX, int chunkZ, PerlinNoise perlinNoise) {
        blocks = new int[SIZE * SIZE * SIZE];
        Random rand = new Random();
        float mountainScale = min_mountain + rand.nextFloat() * (max_mountain - min_mountain);
        float detailScale = min_detail + rand.nextFloat() * (max_detail - min_detail);
        generateTerrainPerlin(chunkX, chunkZ, perlinNoise, mountainScale, detailScale);
    }

    /**
    * Génère un terrain sans bruit de perlin avec des collines, de la pierre, de la terre et de l'herbe.
    */
    private void generateTerrain() {
        // Génération de terrain avec collines et vallées
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                // Calcul de la hauteur avec une fonction sinusoïdale pour créer des collines
                int maxHeight = (int)(SIZE / 2 + Math.sin(x/2.0) * 2 + Math.cos(z/2.0) * 2);

                // Remplissage de bas en haut avec différents types de blocs
                for (int y = 0; y < SIZE; y++) {
                    if (y < maxHeight - 3) {
                        // Sous-sol en pierre
                        setBlock(x, y, z, BlockType.STONE.getId());
                    } else if (y < maxHeight - 1) {
                        // Couche de terre
                        setBlock(x, y, z, BlockType.DIRT.getId());
                    } else if (y < maxHeight) {
                        // Surface en herbe
                        setBlock(x, y, z, BlockType.GRASS.getId());
                    } else if (y == maxHeight && y < SIZE / 2 - 2) {
                        // Eau dans les dépressions
                        setBlock(x, y, z, BlockType.WATER.getId());
                    } else {
                        // Air au-dessus
                        setBlock(x, y, z, BlockType.AIR.getId());
                    }
                }
            }
        }
    }

    /**
     * Generer un terrain avec un bruit de perlin
     * @param chunkX  coordonnée X du chunk 
     * @param chunkZ  coordonnée Z du chunk 
     * @param perlinNoise bruit de perlin utilisé
     */
    private void generateTerrainPerlin(int chunkX, int chunkZ, PerlinNoise perlinNoise , float mountainScale , float detailScale) {
        
        // Niveau de l'eau  
        int waterLevel = 9;
        
        // Coordonnées globales du chunk
        float worldXStart = chunkX * SIZE;
        float worldZStart = chunkZ * SIZE;

        // Générer chaque bloc dans le chunk
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                
                // Coordonnées globales pour ce bloc spécifique
                float worldX = worldXStart + x;
                float worldZ = worldZStart + z;

                // Appliquer un bruit Perlin 2D pour obtenir la hauteur globale des montagnes
                float mountainNoise = perlinNoise.Noise2D(worldX * mountainScale, worldZ * mountainScale);
                
                // Ajouter des détails avec un autre bruit Perlin
                float detailNoise = perlinNoise.Noise2D(worldX * detailScale, worldZ * detailScale);
                
                // Combiner les bruits pour avoir une hauteur réaliste
                float height = mountainNoise + detailNoise * 0.8f;  
                
                // Hauteur finale du terrain dans ce bloc
                int terrainHeight = Math.min(SIZE, Math.max(1, (int)(height * 16)));

                // Générer les blocs pour chaque coordonnée Y
                for (int y = 0; y < SIZE; y++) {
                    // Si on est sous le seuil de la mer mettre de l'eauu
                    if (y <= waterLevel){
                        setBlock(x, y, z, BlockType.WATER.getId());
                    } else if (y < terrainHeight) {
                        // Si on est sous la surface, on place de la roche
                        if (y < terrainHeight - 2) {
                            setBlock(x, y, z, BlockType.STONE.getId());
                        // Si on est sous la surface mais au-dessus, on met de la terre
                        } else if (y < terrainHeight - 1) {
                            setBlock(x, y, z, BlockType.DIRT.getId());
                        } else {
                            //Sinon mettre de l'herbe
                            setBlock(x, y, z, BlockType.GRASS.getId());
                        }
                    } else {
                        // Au-dessus du terrain, on met de l'air
                        setBlock(x, y, z, BlockType.AIR.getId());
                    }
                }
            }
        }
    }

    /**
     * Calcule l'index dans le tableau 1D à partir des coordonnées 3D.
     * 
     * @param x Coordonnée X dans le chunk (0-15)
     * @param y Coordonnée Y dans le chunk (0-15)
     * @param z Coordonnée Z dans le chunk (0-15)
     * @return L'index correspondant dans le tableau 1D
     */
    private int getIndex(int x, int y, int z) {
        return x + SIZE * (y + SIZE * z);
    }

    /**
     * Récupère le type de bloc à une position donnée dans le chunk.
     * 
     * @param x Coordonnée X dans le chunk (0-15)
     * @param y Coordonnée Y dans le chunk (0-15)
     * @param z Coordonnée Z dans le chunk (0-15)
     * @return L'identifiant du type de bloc, ou AIR si hors limites
     */
    public int getBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE) {
            return BlockType.AIR.getId();
        }
        return blocks[getIndex(x, y, z)];
    }

    /**
     * Modifie le type de bloc à une position donnée dans le chunk.
     * 
     * @param x Coordonnée X dans le chunk (0-15)
     * @param y Coordonnée Y dans le chunk (0-15)
     * @param z Coordonnée Z dans le chunk (0-15)
     * @param value Identifiant du type de bloc à placer
     */
    public void setBlock(int x, int y, int z, int value) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE) {
            blocks[getIndex(x, y, z)] = value;
        }
    }
} 