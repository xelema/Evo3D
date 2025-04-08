package voxel;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;

/**
 * Représente le monde de voxels complet, composé de plusieurs chunks.
 * Cette classe gère la création, l'affichage et la mise à jour du monde.
 */
public class VoxelWorld {
    /** Taille du monde en nombre de chunks sur les axes X et Z */
    public static final int WORLD_SIZE = 16;
    
    /** Nœud racine contenant tous les chunks du monde */
    private Node worldNode;
    
    /** Tableau 3D contenant tous les chunks du monde */
    private Chunk[][][] chunks;
    
    /** Taille du monde en nombre de chunks sur l'axe X */
    private final int worldSizeX = WORLD_SIZE;
    
    /** Taille du monde en nombre de chunks sur l'axe Y (un seul niveau vertical pour simplifier) */
    private final int worldSizeY = 1;
    
    /** Taille du monde en nombre de chunks sur l'axe Z */
    private final int worldSizeZ = WORLD_SIZE;
    
    /** Tableau pour stocker les matériaux des chunks */
    private Material[][][] materials;
    
    /** Mode filaire activé ou non */
    private boolean wireframeMode = false;

    /** Eclairage oui ou non */
    private boolean lightningMode = true;

    /** Indique si une mise à jour du maillage est nécessaire */
    private boolean needsMeshUpdate = false;
    
    /** Référence à l'AssetManager pour accéder aux ressources */
    private final AssetManager assetManager;

    /**
     * Crée un nouveau monde de voxels.
     * 
     * @param app L'application principale
     */
    public VoxelWorld(Main app) {
        this.assetManager = app.getAssetManager();
        worldNode = new Node("world");
        chunks = new Chunk[worldSizeX][worldSizeY][worldSizeZ];
        generateWorld();
    }

    /**
     * Retourne l'état actuel du mode d'éclairage.
     * 
     * @return true si l'éclairage est activé, false sinon
     */
    boolean getLightningMode() {
        return lightningMode;
    }

    /**
     * Génère le monde complet avec tous ses chunks.
     */
    private void generateWorld() {
        materials = new Material[worldSizeX][worldSizeY][worldSizeZ];

        // Créer tous les chunks
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    chunks[cx][cy][cz] = new Chunk();
                }
            }
        }

        // Générer les maillages pour tous les chunks
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    Geometry geo = createChunkGeometry(cx, cy, cz);
                    worldNode.attachChild(geo);
                }
            }
        }
    }

    /**
     * Crée et configure la géométrie d'un chunk.
     * Méthode factorisée utilisée par les différentes fonctions de génération.
     *
     * @param chunkX Position X du chunk dans le monde
     * @param chunkY Position Y du chunk dans le monde
     * @param chunkZ Position Z du chunk dans le monde
     * @return La géométrie configurée pour ce chunk
     */
    private Geometry createChunkGeometry(int chunkX, int chunkY, int chunkZ) {
        // Génération du maillage du chunk
        Mesh mesh = chunks[chunkX][chunkY][chunkZ].generateMesh(this, chunkX, chunkY, chunkZ);
        
        // Création de la géométrie pour le maillage
        String chunkName = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ;
        Geometry geo = new Geometry(chunkName, mesh);
        
        // Configuration du matériau
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);

        // Gestion du rendu alpha des blocs
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geo.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        // Application de l'état actuel du wireframe
        mat.getAdditionalRenderState().setWireframe(wireframeMode);
        
        // Stockage du matériau pour modifications ultérieures
        materials[chunkX][chunkY][chunkZ] = mat;
        geo.setMaterial(mat);
        
        // Positionnement dans le monde
        geo.setLocalTranslation(chunkX * Chunk.SIZE, chunkY * Chunk.SIZE, chunkZ * Chunk.SIZE);
        
        return geo;
    }

    /**
     * Active ou désactive le mode filaire (wireframe) pour tous les chunks.
     */
    public void toggleWireframe() {
        wireframeMode = !wireframeMode;
        System.out.println("Wireframe: " + (wireframeMode ? "activé" : "désactivé"));

        // Application du changement à tous les chunks
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    if (materials[cx][cy][cz] != null) {
                        materials[cx][cy][cz].getAdditionalRenderState().setWireframe(wireframeMode);
                    }
                }
            }
        }
    }

    /**
     * Active ou désactive l'éclairage pour tous les chunks.
     */
    public void toggleLightning() {
        lightningMode = !lightningMode;
        System.out.println("Lightning: " + (lightningMode ? "activé" : "désactivé"));

        needsMeshUpdate = true;
    }

    /**
     * Renvoie le nœud contenant l'ensemble du monde voxel.
     * 
     * @return Le nœud racine du monde
     */
    public Node getNode() {
        return worldNode;
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
        int cx = Math.floorDiv(globalX, Chunk.SIZE);
        int cy = Math.floorDiv(globalY, Chunk.SIZE);
        int cz = Math.floorDiv(globalZ, Chunk.SIZE);

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return 0; // AIR pour tout ce qui est en dehors du monde
        }
        
        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - cx * Chunk.SIZE;
        int localY = globalY - cy * Chunk.SIZE;
        int localZ = globalZ - cz * Chunk.SIZE;

        // Récupération du type de bloc dans le chunk
        return chunks[cx][cy][cz].getBlock(localX, localY, localZ);
    }

    /**
     * Régénère tous les maillages des chunks.
     * Utile après avoir modifié le contenu de plusieurs chunks.
     */
    private void regenerateMeshes() {
        // Supprime tous les enfants du nœud monde
        worldNode.detachAllChildren();

        // Régénère tous les maillages
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    Geometry geo = createChunkGeometry(cx, cy, cz);
                    worldNode.attachChild(geo);
                }
            }
        }
    }

    /**
     * Régénère le maillage d'un chunk spécifique.
     *
     * @param chunkX Position X du chunk dans le monde
     * @param chunkY Position Y du chunk dans le monde
     * @param chunkZ Position Z du chunk dans le monde
     */
    public void regenerateChunkMesh(int chunkX, int chunkY, int chunkZ) {
        // Vérifier que les coordonnées sont valides
        if (chunkX < 0 || chunkX >= worldSizeX ||
                chunkY < 0 || chunkY >= worldSizeY ||
                chunkZ < 0 || chunkZ >= worldSizeZ) {
            System.out.println("Chunk hors limites: " + chunkX + ", " + chunkY + ", " + chunkZ);
            return;
        }

        // Trouver et détacher l'ancienne géométrie
        String chunkName = "chunk_" + chunkX + "_" + chunkY + "_" + chunkZ;
        Geometry oldGeometry = (Geometry) worldNode.getChild(chunkName);
        if (oldGeometry != null) {
            worldNode.detachChild(oldGeometry);
        }

        // Créer et attacher la nouvelle géométrie
        Geometry geo = createChunkGeometry(chunkX, chunkY, chunkZ);
        worldNode.attachChild(geo);
    }

    /**
     * Méthode appelée depuis simpleUpdate pour mettre à jour le monde si nécessaire.
     * 
     * @param tpf Temps écoulé depuis la dernière image
     * @param posX Position X de la caméra
     * @param posY Position Y de la caméra
     * @param posZ Position Z de la caméra
     */
    public void update(float tpf, float posX, float posY, float posZ) {
        // Si une mise à jour est nécessaire, régénérer les maillages
        if (needsMeshUpdate) {
            System.out.println("Position du joueur: " + posX + ", " + posY + ", " + posZ);
            System.out.println("Position du chunk: " + (int)posX/16 + ", " + (int)posY/16 + ", " + (int)posZ/16);
            regenerateMeshes();
            needsMeshUpdate = false;
        }
    }
}
