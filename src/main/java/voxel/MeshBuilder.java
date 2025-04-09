package voxel;

import com.jme3.math.Vector3f;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire qui construit un maillage (Mesh) à partir d'un ensemble de faces.
 * Permet d'accumuler des faces, puis de générer le maillage final en une seule fois.
 */
public class MeshBuilder {
    /** Liste des sommets (vertices) du maillage */
    private final List<Vector3f> vertices = new ArrayList<>();
    
    /** Liste des indices du maillage, définissant les triangles */
    private final List<Integer> indices = new ArrayList<>();
    
    /** Liste des normales du maillage, une par sommet */
    private final List<Vector3f> normals = new ArrayList<>();
    
    /** Liste des couleurs du maillage, une par sommet */
    private final List<ColorRGBA> colors = new ArrayList<>();

    /**
     * Ajoute une face au maillage en construction.
     * 
     * @param face La face à ajouter
     */
    public void addFace(Face face) {
        face.addToMesh(vertices, indices, normals, colors);
    }

    /**
     * Construit et retourne le maillage final à partir des faces ajoutées.
     * 
     * @return Le maillage 3D complet
     */
    public Mesh build() {
        Mesh mesh = new Mesh();
        
        // Définition des buffers de vertex
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Color, 4, BufferUtils.createFloatBuffer(colors.toArray(new ColorRGBA[0])));
        
        // Conversion et définition des indices
        int[] indexArray = indices.stream().mapToInt(i -> i).toArray();
        mesh.setBuffer(Type.Index, 3, BufferUtils.createIntBuffer(indexArray));
        
        // Mise à jour des limites (bounds) du maillage
        mesh.updateBound();
        
        return mesh;
    }
} 