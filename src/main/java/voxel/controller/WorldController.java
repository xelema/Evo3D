package voxel.controller;

import com.jme3.math.Vector3f;
import voxel.model.BlockType;
import voxel.model.ChunkModel;
import voxel.model.WorldModel;
import voxel.model.structure.plant.BasicTree;
import voxel.view.WorldRenderer;

import java.util.HashSet;
import java.util.Set;

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


    public void generateTree(int worldX, int worldY, int worldZ, int width, int height){
        BasicTree tree = new BasicTree(width, height);
        int[][][] treeBlocks = tree.getBlocks();

        for (int x = 0; x < tree.getWidth(); x++) {
            for (int y = 0; y < tree.getHeight(); y++) {
                for (int z = 0; z < tree.getWidth(); z++) {
                    int blockType = treeBlocks[x][y][z];
                    if (blockType != -1) {

                        int blockX = worldX-(width/2) + x;
                        int blockY = worldY + y;
                        int blockZ = worldZ-(width/2) + z;

                        // Pose le nouveau bloc pour construire l'arbre
                        boolean modified = worldModel.setBlockAt(blockX, blockY, blockZ, blockType);

                        if (modified){
                            Vector3f chunkCoords = worldModel.getChunkCoordAt(blockX, blockY, blockZ);
                            int cx = (int) chunkCoords.x;
                            int cy = (int) chunkCoords.y;
                            int cz = (int) chunkCoords.z;

                            // Indique que le chunk doit être rechargé
                            worldModel.getChunk(cx, cy, cz).setNeedsUpdate(true);

                            int localX = worldX - (cx - worldModel.getWorldSizeX() / 2) * ChunkModel.SIZE;
                            int localY = worldY - cy * ChunkModel.SIZE;
                            int localZ = worldZ - (cz - worldModel.getWorldSizeZ() / 2) * ChunkModel.SIZE;

                            // Si on est en bordure d'un chunk, mettre à jour les chunks voisins
                            if (localX == 0) worldModel.getChunk(cx-1, cy, cz).setNeedsUpdate(true);
                            if (localX == 15) worldModel.getChunk(cx+1, cy, cz).setNeedsUpdate(true);
                            if (localY == 0) worldModel.getChunk(cx, cy-1, cz).setNeedsUpdate(true);
                            if (localY == 15) worldModel.getChunk(cx, cy+1, cz).setNeedsUpdate(true);
                            if (localZ == 0) worldModel.getChunk(cx, cy, cz-1).setNeedsUpdate(true);
                            if (localZ == 15) worldModel.getChunk(cx, cy, cz+1).setNeedsUpdate(true);
                        }
                    }
                }
            }
        }

        // update les chunks qui ont besoin d'être mis à jour
        updateNeededChunks();

    }

    public void updateNeededChunks() {
        for (int cx = 0; cx < worldModel.getWorldSizeX(); cx++) {
            for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
                for (int cz = 0; cz < worldModel.getWorldSizeZ(); cz++) {
                    ChunkModel chunk = worldModel.getChunk(cx, cy, cz);
                    if (chunk.getNeedsUpdate()) {
                        worldRenderer.updateChunkMesh(cx, cy, cz);
                        chunk.setNeedsUpdate(false);
                    }
                }
            }
        }
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