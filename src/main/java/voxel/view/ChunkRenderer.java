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
    
    /** La géométrie opaque de ce chunk */
    private Geometry opaqueGeometry;
    
    /** La géométrie transparente de ce chunk */
    private Geometry transparentGeometry;
    
    /** Le matériau appliqué à la géométrie opaque */
    private Material opaqueMaterial;
    
    /** Le matériau appliqué à la géométrie transparente */
    private Material transparentMaterial;

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
        
        createGeometries();
    }

    /**
     * Crée les géométries pour ce chunk.
     */
    private void createGeometries() {
        // Génération des maillages séparés
        Mesh opaqueMesh = generateOpaqueMesh();
        Mesh transparentMesh = generateTransparentMesh();
        
        // Création de la géométrie opaque
        String chunkNameOpaque = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ + "_opaque";
        opaqueGeometry = new Geometry(chunkNameOpaque, opaqueMesh);
        
        // Configuration du matériau pour les blocs opaques
        opaqueMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        opaqueMaterial.setBoolean("VertexColor", true);
        opaqueMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
        opaqueGeometry.setMaterial(opaqueMaterial);
        
        // Calcul du décalage pour centrer le monde à (0,0,0)
        int offsetX = worldModel.getWorldSizeX() * ChunkModel.SIZE / 2;
        int offsetY = 0; // Pas de décalage en Y
        int offsetZ = worldModel.getWorldSizeZ() * ChunkModel.SIZE / 2;
        
        // Positionnement dans le monde centré sur (0,0,0)
        float posX = (chunkX * ChunkModel.SIZE) - offsetX;
        float posY = chunkY * ChunkModel.SIZE;
        float posZ = (chunkZ * ChunkModel.SIZE) - offsetZ;
        
        opaqueGeometry.setLocalTranslation(posX, posY, posZ);
        
        // Si nous avons des blocs transparents, créer une géométrie séparée
        if (transparentMesh != null) {
            String chunkNameTransparent = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ + "_transparent";
            transparentGeometry = new Geometry(chunkNameTransparent, transparentMesh);
            
            // Configuration du matériau pour les blocs transparents
            transparentMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            transparentMaterial.setBoolean("VertexColor", true);
            
            // Gestion du rendu alpha des blocs
            transparentMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            transparentMaterial.getAdditionalRenderState().setDepthWrite(false);
            transparentGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
            
            transparentMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
            transparentGeometry.setMaterial(transparentMaterial);
            
            // Même position que la géométrie opaque
            transparentGeometry.setLocalTranslation(posX, posY, posZ);
        }
    }

    /**
     * Génère un maillage pour les parties opaques du chunk.
     * 
     * @return Le maillage opaque généré
     */
    private Mesh generateOpaqueMesh() {
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
                        
                        // Ne traiter que les blocs opaques
                        if (blockColor.a >= 1.0f) {
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
        }

        return builder.build();
    }

    /**
     * Génère un maillage pour les parties transparentes du chunk.
     * 
     * @return Le maillage transparent généré, ou null s'il n'y a pas de blocs transparents
     */
    private Mesh generateTransparentMesh() {
        MeshBuilder builder = new MeshBuilder();
        boolean hasTransparentFaces = false;

        // Parcours de tous les blocs du chunk
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int y = 0; y < ChunkModel.SIZE; y++) {
                for (int z = 0; z < ChunkModel.SIZE; z++) {
                    int blockId = chunkModel.getBlock(x, y, z);
                    
                    // On n'ajoute pas de faces pour les blocs d'air
                    if (blockId != BlockType.AIR.getId()) {
                        BlockType blockType = BlockType.fromId(blockId);
                        ColorRGBA blockColor = blockType.getColor();
                        
                        // Ne traiter que les blocs transparents
                        if (blockColor.a < 1.0f) {
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
                                    hasTransparentFaces = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ne retourner le maillage que s'il y a des faces transparentes
        return hasTransparentFaces ? builder.build() : null;
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
        // Mise à jour du mesh opaque
        Mesh newOpaqueMesh = generateOpaqueMesh();
        opaqueGeometry.setMesh(newOpaqueMesh);
        opaqueMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
        
        // Mise à jour du mesh transparent
        Mesh newTransparentMesh = generateTransparentMesh();
        
        if (newTransparentMesh != null) {
            if (transparentGeometry == null) {
                // Créer une nouvelle géométrie transparente si nécessaire
                String chunkNameTransparent = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ + "_transparent";
                transparentGeometry = new Geometry(chunkNameTransparent, newTransparentMesh);
                
                transparentMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                transparentMaterial.setBoolean("VertexColor", true);
                
                transparentMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                transparentMaterial.getAdditionalRenderState().setDepthWrite(false);
                transparentGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
                
                transparentMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
                transparentGeometry.setMaterial(transparentMaterial);
                
                transparentGeometry.setLocalTranslation(chunkX * ChunkModel.SIZE, chunkY * ChunkModel.SIZE, chunkZ * ChunkModel.SIZE);
            } else {
                // Mettre à jour le mesh transparent existant
                transparentGeometry.setMesh(newTransparentMesh);
                transparentMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
            }
        } else if (transparentGeometry != null) {
            // Si nous n'avons plus de faces transparentes mais que la géométrie existe toujours,
            // nous devons informer le parent pour qu'il la supprime
            // Cela sera géré dans le WorldRenderer
            transparentGeometry = null;
        }
    }

    /**
     * Récupère la géométrie opaque de ce chunk.
     * 
     * @return La géométrie opaque du chunk
     */
    public Geometry getGeometry() {
        return opaqueGeometry;
    }

    /**
     * Récupère la géométrie transparente de ce chunk.
     * 
     * @return La géométrie transparente du chunk, peut être null
     */
    public Geometry getTransparentGeometry() {
        return transparentGeometry;
    }

    /**
     * Récupère le matériau appliqué à la géométrie opaque.
     * 
     * @return Le matériau de la géométrie opaque
     */
    public Material getMaterial() {
        return opaqueMaterial;
    }
} 