package voxel.view;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.asset.AssetManager;

import voxel.model.BlockType;
import voxel.model.ChunkModel;
import voxel.model.WorldModel;
import voxel.utils.Direction;

/**
 * Classe responsable du rendu d'un chunk.
 * Construit et met à jour le maillage 3D à partir des données du chunk.
 */
public class ChunkRenderer {
    /** Le modèle du chunk à rendre */
    private final ChunkModel chunkModel;
    
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Asset manager pour accéder aux ressources */
    private final AssetManager assetManager;
    
    /** Position X du chunk dans le monde */
    private final int chunkX;
    
    /** Position Y du chunk dans le monde */
    private final int chunkY;
    
    /** Position Z du chunk dans le monde */
    private final int chunkZ;
    
    /** La géométrie de ce chunk */
    private Geometry geometry;
    
    /** Le matériau appliqué à ce chunk */
    private Material material;

    /**
     * Crée un nouveau renderer pour un chunk.
     * 
     * @param chunkModel Le modèle du chunk à rendre
     * @param worldModel Référence au modèle du monde
     * @param assetManager Asset manager pour accéder aux ressources
     * @param chunkX Position X du chunk dans le monde
     * @param chunkY Position Y du chunk dans le monde
     * @param chunkZ Position Z du chunk dans le monde
     */
    public ChunkRenderer(ChunkModel chunkModel, WorldModel worldModel, AssetManager assetManager, 
                         int chunkX, int chunkY, int chunkZ) {
        this.chunkModel = chunkModel;
        this.worldModel = worldModel;
        this.assetManager = assetManager;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        
        createGeometry();
    }

    /**
     * Crée la géométrie pour ce chunk.
     */
    private void createGeometry() {
        // Génération du maillage du chunk
        Mesh mesh = generateMesh();
        
        // Création de la géométrie pour le maillage
        String chunkName = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ;
        geometry = new Geometry(chunkName, mesh);
        
        // Configuration du matériau
        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setBoolean("VertexColor", true);

        // Gestion du rendu alpha des blocs
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        // Application de l'état actuel du wireframe
        material.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
        
        geometry.setMaterial(material);
        
        // Positionnement dans le monde
        geometry.setLocalTranslation(chunkX * ChunkModel.SIZE, chunkY * ChunkModel.SIZE, chunkZ * ChunkModel.SIZE);
    }

    /**
     * Génère un maillage pour ce chunk.
     * 
     * @return Le maillage généré
     */
    public Mesh generateMesh() {
        MeshBuilder builder = new MeshBuilder();

        // Parcours de tous les blocs du chunk
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int y = 0; y < ChunkModel.SIZE; y++) {
                for (int z = 0; z < ChunkModel.SIZE; z++) {
                    int blockId = chunkModel.getBlock(x, y, z);
                    
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

                            // Si le bloc voisin est de l'air ou de l'eau, ajouter une face
                            if (getBlockNeighbor(nx, ny, nz) == BlockType.AIR.getId()
                                    || (getBlockNeighbor(nx, ny, nz) == BlockType.WATER.getId()
                                    && chunkModel.getBlock(x, y, z) != BlockType.WATER.getId())) {

                                Face face = Face.createFromDirection(dir, x, y, z, blockColor, worldModel.getLightningMode());
                                builder.addFace(face);
                            }
                        }
                    }
                }
            }
        }

        return builder.build();
    }
    
    /**
     * Récupère le type de bloc voisin, en gérant les blocs à la limite du chunk.
     * 
     * @param x Coordonnée X du bloc voisin
     * @param y Coordonnée Y du bloc voisin
     * @param z Coordonnée Z du bloc voisin
     * @return L'identifiant du type de bloc voisin
     */
    private int getBlockNeighbor(int x, int y, int z) {
        if (x >= 0 && x < ChunkModel.SIZE && y >= 0 && y < ChunkModel.SIZE && z >= 0 && z < ChunkModel.SIZE) {
            // Le bloc voisin est dans ce chunk
            return chunkModel.getBlock(x, y, z);
        } else {
            // Le bloc voisin est dans un autre chunk
            int globalX = chunkX * ChunkModel.SIZE + x;
            int globalY = chunkY * ChunkModel.SIZE + y;
            int globalZ = chunkZ * ChunkModel.SIZE + z;
            return worldModel.getBlockAt(globalX, globalY, globalZ);
        }
    }

    /**
     * Met à jour le maillage du chunk.
     */
    public void updateMesh() {
        Mesh newMesh = generateMesh();
        geometry.setMesh(newMesh);
        material.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
    }

    /**
     * Récupère la géométrie de ce chunk.
     * 
     * @return La géométrie du chunk
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Récupère le matériau appliqué à ce chunk.
     * 
     * @return Le matériau du chunk
     */
    public Material getMaterial(){
        return material;
    }
} 