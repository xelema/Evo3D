package voxel;

import com.jme3.material.Material;
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
    public static final int WORLD_SIZE = 10;
    
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
                    // Génération du maillage du chunk
                    Mesh mesh = chunks[cx][cy][cz].generateMesh(this, cx, cy, cz);
                    
                    // Création de la géométrie pour le maillage
                    Geometry geo = new Geometry("chunk_" + cx + "_" + cy + "_" + cz, mesh);
                    
                    // Configuration du matériau
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setBoolean("VertexColor", true);
                    geo.setMaterial(mat);
                    
                    // Stockage du matériau pour modifications ultérieures
                    materials[cx][cy][cz] = mat;
                    
                    // Positionnement dans le monde
                    geo.setLocalTranslation(cx * Chunk.SIZE, cy * Chunk.SIZE, cz * Chunk.SIZE);
                    
                    // Ajout au nœud racine
                    worldNode.attachChild(geo);
                }
            }
        }
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
                    // Génération du nouveau maillage
                    Mesh mesh = chunks[cx][cy][cz].generateMesh(this, cx, cy, cz);
                    
                    // Création de la géométrie et configuration
                    Geometry geo = new Geometry("chunk_" + cx + "_" + cy + "_" + cz, mesh);
                    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    mat.setBoolean("VertexColor", true);
                    
                    // Application de l'état actuel du wireframe
                    mat.getAdditionalRenderState().setWireframe(wireframeMode);
                    
                    geo.setMaterial(mat);
                    materials[cx][cy][cz] = mat;
                    
                    // Positionnement et ajout au nœud monde
                    geo.setLocalTranslation(cx * Chunk.SIZE, cy * Chunk.SIZE, cz * Chunk.SIZE);
                    worldNode.attachChild(geo);
                }
            }
        }
    }

    /**
     * Méthode appelée depuis simpleUpdate pour mettre à jour le monde si nécessaire.
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void update(float tpf) {
        // Pour l'instant, pas de mise à jour particulière
    }
} 