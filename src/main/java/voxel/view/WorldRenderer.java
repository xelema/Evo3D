package voxel.view;

import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;

import voxel.model.WorldModel;

/**
 * Classe responsable du rendu du monde entier.
 * Gère tous les renderers de chunks et coordonne l'affichage.
 */
public class WorldRenderer {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Asset manager pour accéder aux ressources */
    private final AssetManager assetManager;
    
    /** Nœud racine contenant tous les chunks du monde */
    private Node worldNode;
    
    /** Tableau des renderers pour chaque chunk */
    private ChunkRenderer[][][] chunkRenderers;
    
    /** Indique si une mise à jour du maillage est nécessaire */
    private boolean needsMeshUpdate = false;

    /**
     * Crée un nouveau renderer pour le monde entier.
     * 
     * @param worldModel Le modèle du monde à rendre
     * @param assetManager Asset manager pour accéder aux ressources
     */
    public WorldRenderer(WorldModel worldModel, AssetManager assetManager) {
        this.worldModel = worldModel;
        this.assetManager = assetManager;
        this.worldNode = new Node("world");
        
        initializeChunkRenderers();
    }

    /**
     * Récupère le tableau des renderers de chunks.
     * 
     * @return Tableau 3D contenant tous les renderers de chunks
     */
    public ChunkRenderer[][][] getChunkRenderers() {
        return chunkRenderers;
    }

    /**
     * Initialise tous les renderers de chunks.
     */
    private void initializeChunkRenderers() {
        int sizeX = worldModel.getWorldSizeX();
        int sizeY = worldModel.getWorldSizeY();
        int sizeZ = worldModel.getWorldSizeZ();
        
        chunkRenderers = new ChunkRenderer[sizeX][sizeY][sizeZ];
        
        // Création des renderers pour tous les chunks
        for (int cx = 0; cx < sizeX; cx++) {
            for (int cy = 0; cy < sizeY; cy++) {
                for (int cz = 0; cz < sizeZ; cz++) {
                    createChunkRenderer(cx, cy, cz);
                }
            }
        }
    }

    /**
     * Crée un renderer pour un chunk spécifique.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    private void createChunkRenderer(int chunkX, int chunkY, int chunkZ) {
        // Récupérer le modèle du chunk
        if (worldModel.getChunk(chunkX, chunkY, chunkZ) != null) {
            // Créer le renderer pour ce chunk
            ChunkRenderer renderer = new ChunkRenderer(
                worldModel.getChunk(chunkX, chunkY, chunkZ),
                worldModel,
                assetManager,
                chunkX, chunkY, chunkZ
            );
            
            // Stocker le renderer
            chunkRenderers[chunkX][chunkY][chunkZ] = renderer;
            
            // Attacher la géométrie au nœud monde
            worldNode.attachChild(renderer.getGeometry());
        }
    }

    /**
     * Met à jour tous les maillages des chunks.
     * À appeler quand le mode d'éclairage ou le wireframe change.
     */
    public void updateAllMeshes() {
        int sizeX = worldModel.getWorldSizeX();
        int sizeY = worldModel.getWorldSizeY();
        int sizeZ = worldModel.getWorldSizeZ();
        
        for (int cx = 0; cx < sizeX; cx++) {
            for (int cy = 0; cy < sizeY; cy++) {
                for (int cz = 0; cz < sizeZ; cz++) {
                    if (chunkRenderers[cx][cy][cz] != null) {
                        chunkRenderers[cx][cy][cz].updateMesh();
                    }
                }
            }
        }
        
        needsMeshUpdate = false;
    }

    /**
     * Applique l'état actuel du mode filaire (défini dans WorldModel) 
     * aux matériaux de tous les chunks sans reconstruire les maillages.
     */
    public void applyWireframeModeToMaterials() {
        boolean wireframeEnabled = worldModel.getWireframeMode();
        for (int cx = 0; cx < worldModel.getWorldSizeX(); cx++) {
            for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
                for (int cz = 0; cz < worldModel.getWorldSizeZ(); cz++) {
                    if (chunkRenderers[cx][cy][cz] != null && chunkRenderers[cx][cy][cz].getMaterial() != null) {
                        chunkRenderers[cx][cy][cz].getMaterial()
                                .getAdditionalRenderState()
                                .setWireframe(wireframeEnabled);
                    }
                }
            }
        }
    }
    
    /**
     * Met à jour le maillage d'un chunk spécifique.
     * À appeler quand un bloc est modifié.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     */
    public void updateChunkMesh(int chunkX, int chunkY, int chunkZ) {
        if (chunkX >= 0 && chunkX < worldModel.getWorldSizeX() &&
            chunkY >= 0 && chunkY < worldModel.getWorldSizeY() &&
            chunkZ >= 0 && chunkZ < worldModel.getWorldSizeZ() &&
            chunkRenderers[chunkX][chunkY][chunkZ] != null) {
            
            chunkRenderers[chunkX][chunkY][chunkZ].updateMesh();
        }
    }

    /**
     * Retourne le nœud contenant l'ensemble du monde voxel.
     * 
     * @return Le nœud racine du monde
     */
    public Node getNode() {
        return worldNode;
    }
    
    /**
     * Signale qu'une mise à jour des maillages est nécessaire.
     */
    public void setNeedsMeshUpdate() {
        needsMeshUpdate = true;
    }
    
    /**
     * Méthode appelée à chaque frame pour mettre à jour le rendu.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf) {
        if (needsMeshUpdate) {
            updateAllMeshes();
        }
    }
} 