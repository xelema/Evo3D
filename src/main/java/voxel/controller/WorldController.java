package voxel.controller;

import voxel.model.BlockType;
import voxel.model.ChunkModel;
import voxel.model.WorldModel;
import voxel.view.WorldRenderer;

/**
 * Contrôleur qui gère les interactions entre le modèle de monde et sa vue.
 * Coordonne les modifications et met à jour le rendu en conséquence.
 */
public class WorldController {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Référence au renderer du monde */
    private final WorldRenderer worldRenderer;

    /**
     * Crée un nouveau contrôleur pour le monde.
     * 
     * @param worldModel Le modèle du monde à contrôler
     * @param worldRenderer Le renderer du monde
     */
    public WorldController(WorldModel worldModel, WorldRenderer worldRenderer) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
    }

    /**
     * Active ou désactive le mode filaire pour tous les chunks.
     */
    public void toggleWireframe() {
        boolean newMode = worldModel.toggleWireframe();
        System.out.println("Wireframe: " + (newMode ? "activé" : "désactivé"));
        worldRenderer.applyWireframeModeToMaterials(); 
    }

    /**
     * Active ou désactive l'éclairage pour tous les chunks.
     */
    public void toggleLightning() {
        boolean newMode = worldModel.toggleLightning();
        System.out.println("Lightning: " + (newMode ? "activé" : "désactivé"));
        worldRenderer.updateAllMeshes();
    }

    /**
     * Active ou désactive l'affichage des coordonnées.
     * 
     * @param display true pour afficher les coordonnées, false pour les masquer
     */
    public void toggleCoordinatesDisplay(boolean display) {
        worldRenderer.setDisplayCoordinates(display);
    }

    /**
     * Modifie un bloc à une position donnée.
     * 
     * @param x Coordonnée X du bloc
     * @param y Coordonnée Y du bloc
     * @param z Coordonnée Z du bloc
     * @param blockType Type de bloc à placer
     * @return true si le bloc a été modifié, false sinon
     */
    public boolean modifyBlock(int x, int y, int z, BlockType blockType) {
        boolean modified = worldModel.setBlockAt(x, y, z, blockType.getId());

        if (modified) {
            // Calcul des coordonnées du chunk sans décalage
            int chunkX = Math.floorDiv(x, ChunkModel.SIZE);
            int chunkY = Math.floorDiv(y, ChunkModel.SIZE);
            int chunkZ = Math.floorDiv(z, ChunkModel.SIZE);

            // Calcul des coordonnées locales à l'intérieur du chunk
            int localX = x - chunkX * ChunkModel.SIZE;
            int localY = y - chunkY * ChunkModel.SIZE;
            int localZ = z - chunkZ * ChunkModel.SIZE;

            // Appliquer le décalage pour le stockage dans le tableau de chunks
            int cx = chunkX + worldModel.getWorldSizeX() / 2;
            int cy = chunkY;
            int cz = chunkZ + worldModel.getWorldSizeZ() / 2;

            System.out.println("Chunk modifié: " + cx + ", " + cy + ", " + cz);

            // Mettre à jour le maillage du chunk
            worldRenderer.updateChunkMesh(cx, cy, cz);

            // Si on est en bordure d'un chunk, mettre à jour les chunks voisins
            if (localX == 0) worldRenderer.updateChunkMesh(chunkX - 1, chunkY, chunkZ);
            if (localX == 15) worldRenderer.updateChunkMesh(chunkX + 1, chunkY, chunkZ);
            if (localY == 0) worldRenderer.updateChunkMesh(chunkX, chunkY - 1, chunkZ);
            if (localY == 15) worldRenderer.updateChunkMesh(chunkX, chunkY + 1, chunkZ);
            if (localZ == 0) worldRenderer.updateChunkMesh(chunkX, chunkY, chunkZ - 1);
            if (localZ == 15) worldRenderer.updateChunkMesh(chunkX, chunkY, chunkZ + 1);
        }

        return modified;
    }

    /**
     * Met à jour le contrôleur et le modèle à chaque frame.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf) {
        worldRenderer.update(tpf);
    }

    /**
     * Retourne le modèle du monde (WorldModel).
     */
    public WorldModel getWorldModel() {
        return worldModel;
    }
} 