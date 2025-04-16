package voxel.controller;

import voxel.model.BlockType;
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
            // Calculer les coordonnées du chunk contenant ce bloc
            int chunkX = Math.floorDiv(x, 16);
            int chunkY = Math.floorDiv(y, 16);
            int chunkZ = Math.floorDiv(z, 16);
            
            // Mettre à jour le maillage du chunk
            worldRenderer.updateChunkMesh(chunkX, chunkY, chunkZ);
            
            // Vérifier les chunks voisins qui pourraient être affectés
            int localX = x - chunkX * 16;
            int localY = y - chunkY * 16;
            int localZ = z - chunkZ * 16;
            
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