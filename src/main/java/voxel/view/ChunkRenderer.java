package voxel.view;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
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
            createTransparentGeometry(transparentMesh, posX, posY, posZ);
        }
    }

    /**
     * Crée ou met à jour la géométrie transparente avec le mesh fourni
     * 
     * @param transparentMesh Le maillage transparent à utiliser
     * @param posX Position X dans le monde
     * @param posY Position Y dans le monde
     * @param posZ Position Z dans le monde
     */
    private void createTransparentGeometry(Mesh transparentMesh, float posX, float posY, float posZ) {
        String chunkNameTransparent = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ + "_transparent";
        transparentGeometry = new Geometry(chunkNameTransparent, transparentMesh);
        
        // Configuration du matériau pour les blocs transparents
        transparentMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        transparentMaterial.setBoolean("VertexColor", true);
        
        // Gestion du rendu alpha des blocs
        transparentMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        transparentMaterial.getAdditionalRenderState().setDepthWrite(false);
        transparentMaterial.setTransparent(true);
        transparentGeometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        transparentMaterial.getAdditionalRenderState().setWireframe(worldModel.getWireframeMode());
        transparentGeometry.setMaterial(transparentMaterial);
        
        // Positionnement dans le monde
        transparentGeometry.setLocalTranslation(posX, posY, posZ);
    }

    /**
     * Génère un maillage pour les parties opaques du chunk en utilisant l'algorithme Greedy Meshing.
     * Fusionne les faces adjacentes identiques pour réduire le nombre de triangles.
     * 
     * @return Le maillage opaque généré
     */
    private Mesh generateOpaqueMesh() {
        return generateGreedyMesh(false);
    }

    /**
     * Génère un maillage pour les parties transparentes du chunk en utilisant l'algorithme Greedy Meshing.
     * 
     * @return Le maillage transparent généré, ou null s'il n'y a pas de blocs transparents
     */
    private Mesh generateTransparentMesh() {
        Mesh mesh = generateGreedyMesh(true);
        // MeshBuilder.build() retourne un mesh même vide, nous devons vérifier s'il contient des sommets
        if (mesh.getVertexCount() == 0) {
            return null;
        }
        return mesh;
    }

    /**
     * Algorithme de Greedy Meshing générique.
     * Fusionne les faces adjacentes identiques en quads plus grands.
     * 
     * @param isTransparent true pour générer le mesh transparent, false pour l'opaque
     * @return Le maillage généré
     */
    private Mesh generateGreedyMesh(boolean isTransparent) {
        MeshBuilder builder = new MeshBuilder();
        final int SIZE = ChunkModel.SIZE;
        final boolean lightningMode = worldModel.getLightningMode();
        
        // Masque indiquant l'ID du bloc visible à une position (u, v) de la tranche courante
        // 0 signifie pas de face à générer
        int[] blockMask = new int[SIZE * SIZE];
        int[] aoMask = new int[SIZE * SIZE];
        
        // Vecteur position mutable pour éviter les allocations
        int[] pos = new int[3];

        // Pour chaque direction (6 faces possibles)
        for (Direction dir : Direction.values()) {
            
            // Déterminer les axes de la tranche (u, v) et l'axe de profondeur (d)
            // alignés avec la logique de Face.createFromDirection
            int uAxis, vAxis, dAxis;
            
            switch (dir) {
                case POS_Z: case NEG_Z: 
                    dAxis=2; uAxis=0; vAxis=1; // Z est depth, X est width (u), Y est height (v)
                    break;
                case POS_X: case NEG_X: 
                    dAxis=0; uAxis=2; vAxis=1; // X est depth, Z est width (u), Y est height (v)
                    break;
                case POS_Y: case NEG_Y: // Y est depth
                    // Pour Face.java POS_Y/NEG_Y: width est sur X, height est sur Z
                    dAxis=1; uAxis=0; vAxis=2; 
                    break; 
                default: continue;
            }
            
            // Parcourir toutes les tranches le long de l'axe de profondeur
            for (int slice = 0; slice < SIZE; slice++) {
                
                // 1. Remplir le masque pour cette tranche
                int n = 0;
                for (int v = 0; v < SIZE; v++) {
                    for (int u = 0; u < SIZE; u++) {
                        // Assigner les coordonnées selon les axes
                        pos[uAxis] = u;
                        pos[vAxis] = v;
                        pos[dAxis] = slice;
                        
                        int x = pos[0];
                        int y = pos[1];
                        int z = pos[2];
                        
                        int blockId = chunkModel.getBlock(x, y, z);
                        boolean visible = false;
                        
                        // Filtrer selon le type (transparent/opaque)
                        if (blockId != BlockType.AIR.getId()) {
                             BlockType type = BlockType.fromId(blockId);
                             boolean typeMatch = isTransparent ? (type.getColor().a < 1.0f) : (type.getColor().a >= 1.0f);
                             
                             if (typeMatch) {
                                 // Vérifier le voisin dans la direction de la face
                                 int nx = x + dir.getOffsetX();
                                 int ny = y + dir.getOffsetY();
                                 int nz = z + dir.getOffsetZ();
                                 
                                 if (shouldGenerateFace(nx, ny, nz, blockId)) {
                                     visible = true;
                                 }
                             }
                        }
                        
                        if (visible) {
                            blockMask[n] = blockId;
                            if (lightningMode) {
                                int[] aoValues = Face.computeAoValues(dir, pos[0], pos[1], pos[2], 1, 1, worldModel, chunkX, chunkY, chunkZ);
                                aoMask[n] = encodeAoKey(aoValues);
                            } else {
                                aoMask[n] = 0;
                            }
                        } else {
                            blockMask[n] = 0;
                            aoMask[n] = 0;
                        }
                        n++;
                    }
                }
                
                // 2. Greedy Meshing sur le masque
                n = 0;
                for (int v = 0; v < SIZE; v++) {
                    for (int u = 0; u < SIZE; u++) {
                        
                        int blockId = blockMask[n];
                        
                        if (blockId != 0) {
                            // Début d'un nouveau quad
                            int width = 1;
                            int height = 1;
                            int aoKey = aoMask[n];
                            
                            // Calculer la largeur (avancer sur u)
                            while (u + width < SIZE 
                                    && blockMask[n + width] == blockId
                                    && (!lightningMode || aoMask[n + width] == aoKey)) {
                                width++;
                            }
                            
                            // Calculer la hauteur (avancer sur v)
                            boolean canExtendHeight = true;
                            while (v + height < SIZE && canExtendHeight) {
                                for (int k = 0; k < width; k++) {
                                    int index = n + k + height * SIZE;
                                    if (blockMask[index] != blockId
                                            || (lightningMode && aoMask[index] != aoKey)) {
                                        canExtendHeight = false;
                                        break;
                                    }
                                }
                                if (canExtendHeight) {
                                    height++;
                                }
                            }
                            
                            // Créer la face
                            pos[uAxis] = u;
                            pos[vAxis] = v;
                            pos[dAxis] = slice;
                            
                            BlockType type = BlockType.fromId(blockId);
                            Face face = Face.createFromDirection(dir, pos[0], pos[1], pos[2], width, height, 
                                                                 type.getColor(), lightningMode, 
                                                                 worldModel, chunkX, chunkY, chunkZ);
                            builder.addFace(face);
                            
                            // Effacer la zone couverte dans le masque
                            for (int h = 0; h < height; h++) {
                                for (int w = 0; w < width; w++) {
                                    int index = n + w + h * SIZE;
                                    blockMask[index] = 0;
                                    aoMask[index] = 0;
                                }
                            }
                            
                            // Optimisation : sauter les blocs traités sur la ligne courante
                            u += width - 1;
                            n += width - 1;
                        }
                        n++;
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
            int globalX = x + (chunkX - worldModel.getWorldSizeX()/2) * ChunkModel.SIZE;
            int globalY = y + chunkY * ChunkModel.SIZE;
            int globalZ = z + (chunkZ - worldModel.getWorldSizeZ()/2) * ChunkModel.SIZE;
            return worldModel.getBlockAt(globalX, globalY, globalZ);
        }
    }

    /**
     * Vérifie si le bloc voisin justifie la génération d'une face
     * (uniquement pour l'air et l'eau).
     * 
     * @param x Coordonnée X du bloc voisin
     * @param y Coordonnée Y du bloc voisin
     * @param z Coordonnée Z du bloc voisin
     * @param blockId L'identifiant du bloc actuel
     * @return True si une face doit être générée, false sinon
     */
    private boolean shouldGenerateFace(int x, int y, int z, int blockId) {
        int neighborId = getBlockNeighbor(x, y, z);
        
        // Toujours générer une face contre l'air, le vide ou l'invisible
        if (neighborId == BlockType.AIR.getId() 
                || neighborId == BlockType.INVISIBLE.getId()
                || neighborId == BlockType.VOID.getId()) {
            return true;
        }
        
        // Générer une face si le bloc voisin est de l'eau et le bloc actuel n'est pas de l'eau
        if (BlockType.isWaterBlock(neighborId) && !BlockType.isWaterBlock(blockId)) {
            return true;
        }
        
        // Générer une face si le bloc voisin est transparent et le bloc actuel est opaque
        if (BlockType.isTransparentBlock(neighborId) && !BlockType.isTransparentBlock(blockId)) {
            return true;
        }
        
        // Générer une face si le bloc actuel est transparent et le voisin est différent
        if (BlockType.isTransparentBlock(blockId) && neighborId != blockId) {
            return true;
        }
        
        return false;
    }

    private static int encodeAoKey(int[] aoValues) {
        int key = 0;
        key |= (aoValues[0] & 0x3);
        key |= (aoValues[1] & 0x3) << 2;
        key |= (aoValues[2] & 0x3) << 4;
        key |= (aoValues[3] & 0x3) << 6;
        return key;
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
                int offsetX = worldModel.getWorldSizeX() * ChunkModel.SIZE / 2;
                int offsetY = 0;
                int offsetZ = worldModel.getWorldSizeZ() * ChunkModel.SIZE / 2;
                
                float posX = (chunkX * ChunkModel.SIZE) - offsetX;
                float posY = chunkY * ChunkModel.SIZE;
                float posZ = (chunkZ * ChunkModel.SIZE) - offsetZ;
                
                createTransparentGeometry(newTransparentMesh, posX, posY, posZ);
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