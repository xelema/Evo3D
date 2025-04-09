package voxel.model;

/**
 * Représente le monde de voxels complet, composé de plusieurs chunks.
 * Cette classe gère uniquement les données du monde sans le rendu.
 */
public class WorldModel {
    /** Taille du monde en nombre de chunks sur les axes X et Z */
    public static final int WORLD_SIZE = 16;
    
    /** Tableau 3D contenant tous les chunks du monde */
    private ChunkModel[][][] chunks;
    
    /** Taille du monde en nombre de chunks sur l'axe X */
    private final int worldSizeX = WORLD_SIZE;
    
    /** Taille du monde en nombre de chunks sur l'axe Y (un seul niveau vertical pour simplifier) */
    private final int worldSizeY = 1;
    
    /** Taille du monde en nombre de chunks sur l'axe Z */
    private final int worldSizeZ = WORLD_SIZE;
    
    /** Eclairage oui ou non */
    private boolean lightningMode = true;
    
    /** Mode filaire activé ou non */
    private boolean wireframeMode = false;

    /**
     * Crée un nouveau monde de voxels.
     */
    public WorldModel() {
        chunks = new ChunkModel[worldSizeX][worldSizeY][worldSizeZ];
        generateWorld();
    }

    /**
     * Génère le monde complet avec tous ses chunks.
     */
    private void generateWorld() {
        // Créer tous les chunks
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    chunks[cx][cy][cz] = new ChunkModel();
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