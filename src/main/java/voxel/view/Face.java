package voxel.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import voxel.utils.Direction;

/**
 * Représente une face de bloc à ajouter au maillage.
 * Une face est définie par 4 sommets, une normale et une couleur.
 * Cette classe fait partie de la vue car elle concerne uniquement le rendu.
 */
public class Face {
    /** Les 4 sommets de la face (dans l'ordre pour former deux triangles) */
    private final Vector3f[] vertices = new Vector3f[4];
    
    /** La normale de la face (perpendiculaire à la surface) */
    private final Vector3f normal;
    
    /** La couleur de la face */
    private final ColorRGBA color;
    
    /** Facteurs d'éclairage pour chaque direction */
    private static final Map<Direction, Float> LIGHTING_FACTORS = new HashMap<>();
    
    // Initialisation des facteurs d'éclairage
    static {
        // Utilisation de différents facteurs d'éclairage pour chaque direction
        LIGHTING_FACTORS.put(Direction.POS_Y, 1.0f);    // Haut (le plus brillant)
        LIGHTING_FACTORS.put(Direction.POS_X, 0.8f);    // Est
        LIGHTING_FACTORS.put(Direction.POS_Z, 0.7f);    // Sud 
        LIGHTING_FACTORS.put(Direction.NEG_Z, 0.6f);    // Nord
        LIGHTING_FACTORS.put(Direction.NEG_X, 0.5f);    // Ouest
        LIGHTING_FACTORS.put(Direction.NEG_Y, 0.4f);    // Bas (le plus sombre)
    }

    /**
     * Crée une face avec les 4 sommets, la normale et la couleur spécifiés.
     * 
     * @param v0 Premier sommet
     * @param v1 Deuxième sommet
     * @param v2 Troisième sommet
     * @param v3 Quatrième sommet
     * @param normal Vecteur normal à la face
     * @param color Couleur de la face
     */
    public Face(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal, ColorRGBA color) {
        this.vertices[0] = v0;
        this.vertices[1] = v1;
        this.vertices[2] = v2;
        this.vertices[3] = v3;
        this.normal = normal;
        this.color = color;
    }

    /**
     * Crée une face à partir d'une direction, d'une position de bloc et d'une couleur.
     * 
     * @param dir Direction de la face
     * @param x Coordonnée X du bloc
     * @param y Coordonnée Y du bloc
     * @param z Coordonnée Z du bloc
     * @param color Couleur de la face
     * @param lightning Indique si l'éclairage doit être appliqué à la face
     * @return Une nouvelle face orientée dans la direction indiquée
     */
    public static Face createFromDirection(Direction dir, int x, int y, int z, ColorRGBA color, boolean lightning) {
        Vector3f v0, v1, v2, v3;

        switch (dir) {
            case POS_Z: // Face avant (Z+)
                v0 = new Vector3f(x, y, z + 1);
                v1 = new Vector3f(x + 1, y, z + 1);
                v2 = new Vector3f(x + 1, y + 1, z + 1);
                v3 = new Vector3f(x, y + 1, z + 1);
                break;
            case NEG_Z: // Face arrière (Z-)
                v0 = new Vector3f(x + 1, y, z);
                v1 = new Vector3f(x, y, z);
                v2 = new Vector3f(x, y + 1, z);
                v3 = new Vector3f(x + 1, y + 1, z);
                break;
            case POS_X: // Face droite (X+)
                v0 = new Vector3f(x + 1, y, z + 1);
                v1 = new Vector3f(x + 1, y, z);
                v2 = new Vector3f(x + 1, y + 1, z);
                v3 = new Vector3f(x + 1, y + 1, z + 1);
                break;
            case NEG_X: // Face gauche (X-)
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x, y, z + 1);
                v2 = new Vector3f(x, y + 1, z + 1);
                v3 = new Vector3f(x, y + 1, z);
                break;
            case POS_Y: // Face supérieure (Y+)
                v0 = new Vector3f(x, y + 1, z + 1);
                v1 = new Vector3f(x + 1, y + 1, z + 1);
                v2 = new Vector3f(x + 1, y + 1, z);
                v3 = new Vector3f(x, y + 1, z);
                break;
            case NEG_Y: // Face inférieure (Y-)
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x + 1, y, z);
                v2 = new Vector3f(x + 1, y, z + 1);
                v3 = new Vector3f(x, y, z + 1);
                break;
            default:
                throw new IllegalArgumentException("Direction invalide");
        }

        if (lightning){
            // Si l'éclairage est activé, appliquer le facteur d'éclairage
            color = applyLighting(color, dir);
        }

        return new Face(v0, v1, v2, v3, dir.getNormal(), color);
    }
    
    /**
     * Applique un facteur d'éclairage à la couleur en fonction de la direction de la face.
     * 
     * @param baseColor Couleur de base du bloc
     * @param dir Direction de la face
     * @return Couleur modifiée avec le facteur d'éclairage appliqué
     */
    private static ColorRGBA applyLighting(ColorRGBA baseColor, Direction dir) {
        // Récupérer le facteur d'éclairage pour cette direction
        float factor = LIGHTING_FACTORS.getOrDefault(dir, 1.0f);
        
        // Créer une nouvelle couleur avec le facteur d'éclairage appliqué
        return new ColorRGBA(
            baseColor.r * factor,
            baseColor.g * factor,
            baseColor.b * factor,
            baseColor.a
        );
    }

    /**
     * Ajoute cette face au maillage en ajoutant ses sommets, indices, normales et couleurs
     * aux listes correspondantes.
     * 
     * @param vertices Liste des sommets du maillage
     * @param indices Liste des indices du maillage
     * @param normals Liste des normales du maillage
     * @param colors Liste des couleurs du maillage
     */
    public void addToMesh(List<Vector3f> vertices, List<Integer> indices, List<Vector3f> normals, List<ColorRGBA> colors) {
        int vertexIndex = vertices.size();

        // Ajout des sommets
        for (Vector3f vertex : this.vertices) {
            vertices.add(vertex);
        }

        // Ajout des indices pour former 2 triangles
        // Premier triangle
        indices.add(vertexIndex);
        indices.add(vertexIndex + 1);
        indices.add(vertexIndex + 2);
        
        // Second triangle
        indices.add(vertexIndex);
        indices.add(vertexIndex + 2);
        indices.add(vertexIndex + 3);

        // Ajout des normales et couleurs pour chaque sommet
        for (int i = 0; i < 4; i++) {
            normals.add(this.normal);
            colors.add(this.color);
        }
    }
} 