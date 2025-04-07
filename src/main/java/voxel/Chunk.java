package voxel;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Mesh;

/**
 * Représente un chunk (section) du monde de voxels.
 * Un chunk est un cube de taille fixe contenant des blocs de différents types.
 */
public class Chunk {
    /** Taille du chunk en nombre de blocs dans chaque dimension */
    public static final int SIZE = 16;
    
    /** Tableau contenant les identifiants des blocs du chunk
     *  De taille SIZE * SIZE * SIZE, représentant chaque bloc dans le chunk.
     *  De dimension 1 et non 3 car il est plus efficace de stocker les blocs dans un tableau 1D.
     * */
    private final int[] blocks;

    /**
     * Crée un nouveau chunk avec une génération de terrain par défaut.
     */
    public Chunk() {
        blocks = new int[SIZE * SIZE * SIZE];
        generateTerrain();
    }

    /**
     * Génère un terrain avec des collines, de la pierre, de la terre et de l'herbe.
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

    /**
     * Récupère le type de bloc voisin, en gérant les blocs à la limite du chunk.
     * 
     * @param x Coordonnée X du bloc voisin
     * @param y Coordonnée Y du bloc voisin
     * @param z Coordonnée Z du bloc voisin
     * @param world Référence au monde voxel
     * @param chunkX Coordonnée X du chunk dans le monde
     * @param chunkY Coordonnée Y du chunk dans le monde
     * @param chunkZ Coordonnée Z du chunk dans le monde
     * @return L'identifiant du type de bloc voisin
     */
    private int getBlockNeighbor(int x, int y, int z, VoxelWorld world, int chunkX, int chunkY, int chunkZ) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE) {
            // Le bloc voisin est dans ce chunk
            return getBlock(x, y, z);
        } else {
            // Le bloc voisin est dans un autre chunk
            int globalX = chunkX * SIZE + x;
            int globalY = chunkY * SIZE + y;
            int globalZ = chunkZ * SIZE + z;
            return world.getBlockAt(globalX, globalY, globalZ);
        }
    }

    /**
     * Génère un maillage pour ce chunk.
     * 
     * @param world Référence au monde voxel
     * @param chunkX Coordonnée X du chunk dans le monde
     * @param chunkY Coordonnée Y du chunk dans le monde
     * @param chunkZ Coordonnée Z du chunk dans le monde
     * @return Le maillage généré pour ce chunk
     */
    public Mesh generateMesh(VoxelWorld world, int chunkX, int chunkY, int chunkZ) {
        MeshBuilder builder = new MeshBuilder();

        // Parcours de tous les blocs du chunk
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    int blockId = getBlock(x, y, z);
                    
                    // On n'ajoute pas de faces pour les blocs d'air
                    if (blockId != BlockType.AIR.getId()) {
                        BlockType blockType = BlockType.fromId(blockId);
                        ColorRGBA blockColor = blockType.getColor();

                        // Vérifier et ajouter les faces visibles pour chaque direction
                        for (Direction dir : Direction.values()) {
                            // Calculer la position du bloc voisin
                            int nx = x + dir.getOffsetX();
                            int ny = y + dir.getOffsetY();
                            int nz = z + dir.getOffsetZ();

                            // Si le bloc voisin est de l'air, ajouter une face
                            if (getBlockNeighbor(nx, ny, nz, world, chunkX, chunkY, chunkZ) == BlockType.AIR.getId()) {
                                Face face = Face.createFromDirection(dir, x, y, z, blockColor, world.getLightningMode());
                                builder.addFace(face);
                            }
                        }
                    }
                }
            }
        }

        return builder.build();
    }
} 