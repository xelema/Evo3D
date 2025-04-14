package voxel.model;

/**
 * Représente un chunk (section) du monde de voxels.
 * Un chunk est un cube de taille fixe contenant des blocs de différents types.
 * Cette classe stocke uniquement les données du chunk sans le rendu.
 */
public class ChunkModel {
    /** Taille du chunk en nombre de blocs dans chaque dimension */
    public static final int SIZE = 32 ;
    
    /** Tableau contenant les identifiants des blocs du chunk */
    private final int[] blocks;

    /**
     * Crée un nouveau chunk avec une génération de terrain par défaut.
     */
    public ChunkModel() {
        this(false);
    }

    /**
     * Crée un nouveau chunk.
     *
     * @param empty Si true, le chunk sera rempli d'air. Sinon, il sera généré avec un terrain par défaut.
     */
    public ChunkModel(boolean empty) {
        blocks = new int[SIZE * SIZE * SIZE];
        if (empty) {
            fillWithAir();
        } else {
            generateTerrain();
        }
    }

    /**
     * Remplit le chunk entièrement avec des blocs d'air.
     */
    private void fillWithAir() {
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = BlockType.AIR.getId();
        }
    }

    /**
     * Génère un environnement de base.
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