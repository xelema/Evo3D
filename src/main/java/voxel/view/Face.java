package voxel.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import voxel.model.BlockType;
import voxel.model.ChunkModel;
import voxel.model.WorldModel;
import voxel.utils.Direction;

/**
 * Représente une face de bloc à ajouter au maillage.
 * Une face est définie par 4 sommets, une normale et une couleur.
 * Cette classe fait partie de la vue car elle concerne uniquement le rendu.
 */
public class Face {
    /** Les 4 sommets de la face (dans l'ordre pour former deux triangles) */
    private final Vector3f[] vertices = new Vector3f[4];

    // Suppression du champ statique aoId pour thread-safety
    
    /** La normale de la face (perpendiculaire à la surface) */
    private final Vector3f normal;
    
    /** La couleur de la face */
    private final ColorRGBA[] vertexColors = new ColorRGBA[4];
    
    /** Indique si la face doit utiliser l'ordre de triangulation alternatif pour l'ambient occlusion */
    private final boolean flipId;
    
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
     * @param vertexColors Couleurs des sommets
     * @param flipId Indique si la face doit utiliser l'ordre de triangulation alternatif pour l'ambient occlusion
     */
    public Face(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal, ColorRGBA[] vertexColors, boolean flipId) {
        this.vertices[0] = v0;
        this.vertices[1] = v1;
        this.vertices[2] = v2;
        this.vertices[3] = v3;
        this.normal = normal;
        this.vertexColors[0] = vertexColors[0];
        this.vertexColors[1] = vertexColors[1];
        this.vertexColors[2] = vertexColors[2];
        this.vertexColors[3] = vertexColors[3];
        this.flipId = flipId;
    }

    /**
     * Crée une face à partir d'une direction, d'une position de bloc et d'une couleur.
     * Version standard (1x1).
     */
    public static Face createFromDirection(Direction dir, int x, int y, int z, ColorRGBA color, boolean lightning, WorldModel worldModel, int cx, int cy, int cz) {
        return createFromDirection(dir, x, y, z, 1, 1, color, lightning, worldModel, cx, cy, cz);
    }

    /**
     * Crée une face à partir d'une direction, d'une position de bloc, d'une couleur et de dimensions personnalisées.
     * Utilisé pour le Greedy Meshing.
     * 
     * @param width Largeur de la face (sur l'axe U)
     * @param height Hauteur de la face (sur l'axe V)
     */
    public static Face createFromDirection(Direction dir, int x, int y, int z, int width, int height, ColorRGBA color, boolean lightning, WorldModel worldModel, int cx, int cy, int cz) {
        Vector3f v0, v1, v2, v3;
        ColorRGBA[] vertexColors = new ColorRGBA[4];
        int[] aoId = computeAoValues(dir, x, y, z, width, height, worldModel, cx, cy, cz); // Tableau local pour thread-safety

        switch (dir) {
            case POS_Z: // Face avant (Z+) -> Plan XY, Z fixe à gz+1
                v0 = new Vector3f(x, y, z + 1);
                v1 = new Vector3f(x + width, y, z + 1);
                v2 = new Vector3f(x + width, y + height, z + 1);
                v3 = new Vector3f(x, y + height, z + 1);
                break;
                
            case NEG_Z: // Face arrière (Z-) -> Plan XY, Z fixe à gz-1
                v0 = new Vector3f(x + width, y, z);
                v1 = new Vector3f(x, y, z);
                v2 = new Vector3f(x, y + height, z);
                v3 = new Vector3f(x + width, y + height, z);
                break;
                
            case POS_X: // Face droite (X+) -> Plan ZY, X fixe à gx+1
                v0 = new Vector3f(x + 1, y, z + width);
                v1 = new Vector3f(x + 1, y, z);
                v2 = new Vector3f(x + 1, y + height, z);
                v3 = new Vector3f(x + 1, y + height, z + width);
                break;
                
            case NEG_X: // Face gauche (X-) -> Plan ZY, X fixe à gx-1
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x, y, z + width);
                v2 = new Vector3f(x, y + height, z + width);
                v3 = new Vector3f(x, y + height, z);
                break;
                
            case POS_Y: // Face supérieure (Y+) -> Plan XZ, Y fixe à gy+1
                v0 = new Vector3f(x, y + 1, z + height);
                v1 = new Vector3f(x + width, y + 1, z + height);
                v2 = new Vector3f(x + width, y + 1, z);
                v3 = new Vector3f(x, y + 1, z);
                break;
                
            case NEG_Y: // Face inférieure (Y-) -> Plan XZ, Y fixe à gy-1
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x + width, y, z);
                v2 = new Vector3f(x + width, y, z + height);
                v3 = new Vector3f(x, y, z + height);
                break;
            default:
                throw new IllegalArgumentException("Direction invalide");
        }

        for (int i = 0; i < 4; i++) {
            // Nous utilisons directement les valeurs calculées pour chaque sommet
            // aoId[i] contient déjà l'AO pour vertices[i] grâce à l'assignation explicite dans le switch
            int ao_sum = aoId[i]; 

            // Facteurs d'occlusion ambiante plus doux pour un rendu plus smooth
            float[] aoFactors = new float[4];
            aoFactors[0] = 0.25f;  // 0 blocs AIR (occlusion maximale, mais moins sombre)
            aoFactors[1] = 0.5f;  // 1 bloc AIR
            aoFactors[2] = 0.75f;  // 2 blocs AIR
            aoFactors[3] = 1.0f;  // 3 blocs AIR (pas d'occlusion)

            ColorRGBA baseColor = color;
            if (lightning) {
                baseColor = applyLighting(color, dir);
                vertexColors[i] = new ColorRGBA(
                        baseColor.r * aoFactors[ao_sum],
                        baseColor.g * aoFactors[ao_sum],
                        baseColor.b * aoFactors[ao_sum],
                        baseColor.a);
            }
            else {
                baseColor = applyLighting(color, dir);
                vertexColors[i] = new ColorRGBA(baseColor.r, baseColor.g, baseColor.b, baseColor.a);
            }
        }
        
        // Calcul du flip_id basé sur les valeurs d'ambient occlusion
        // La technique du flip améliore le rendu en choisissant la diagonale optimale pour diviser la face en triangles
        // Pour POS_Z/NEG_Z: v0=BG, v1=BD, v2=HD, v3=HG
        // Diagonales: v0-v2 vs v1-v3.
        // On veut couper l'AO là où elle diffère le moins.
        boolean flipId = Math.abs(aoId[0] - aoId[2]) > Math.abs(aoId[1] - aoId[3]);
        
        return new Face(v0, v1, v2, v3, dir.getNormal(), vertexColors, flipId);
    }

    static int[] computeAoValues(Direction dir, int x, int y, int z, int width, int height, WorldModel worldModel, int cx, int cy, int cz) {
        int[] aoId = new int[4];

        float worldXStart = cx * ChunkModel.SIZE - (float) (worldModel.getWorldSizeX() * ChunkModel.SIZE) / 2;
        float worldZStart = cz * ChunkModel.SIZE - (float) (worldModel.getWorldSizeZ() * ChunkModel.SIZE) / 2;

        int gx = (int) (worldXStart + x);
        int gy = (cy * ChunkModel.SIZE) + y;
        int gz = (int) (worldZStart + z);

        switch (dir) {
            case POS_Z:
                aoId[0] = getAOAt(worldModel, gx, gy, gz + 1, dir, 0);
                aoId[1] = getAOAt(worldModel, gx + width - 1, gy, gz + 1, dir, 1);
                aoId[2] = getAOAt(worldModel, gx + width - 1, gy + height - 1, gz + 1, dir, 2);
                aoId[3] = getAOAt(worldModel, gx, gy + height - 1, gz + 1, dir, 3);
                break;
            case NEG_Z:
                aoId[0] = getAOAt(worldModel, gx + width - 1, gy, gz - 1, dir, 1);
                aoId[1] = getAOAt(worldModel, gx, gy, gz - 1, dir, 0);
                aoId[2] = getAOAt(worldModel, gx, gy + height - 1, gz - 1, dir, 3);
                aoId[3] = getAOAt(worldModel, gx + width - 1, gy + height - 1, gz - 1, dir, 2);
                break;
            case POS_X:
                aoId[0] = getAOAt(worldModel, gx + 1, gy, gz + width - 1, dir, 1);
                aoId[1] = getAOAt(worldModel, gx + 1, gy, gz, dir, 0);
                aoId[2] = getAOAt(worldModel, gx + 1, gy + height - 1, gz, dir, 3);
                aoId[3] = getAOAt(worldModel, gx + 1, gy + height - 1, gz + width - 1, dir, 2);
                break;
            case NEG_X:
                aoId[0] = getAOAt(worldModel, gx - 1, gy, gz, dir, 0);
                aoId[1] = getAOAt(worldModel, gx - 1, gy, gz + width - 1, dir, 1);
                aoId[2] = getAOAt(worldModel, gx - 1, gy + height - 1, gz + width - 1, dir, 2);
                aoId[3] = getAOAt(worldModel, gx - 1, gy + height - 1, gz, dir, 3);
                break;
            case POS_Y:
                aoId[0] = getAOAt(worldModel, gx, gy + 1, gz + height - 1, dir, 3);
                aoId[1] = getAOAt(worldModel, gx + width - 1, gy + 1, gz + height - 1, dir, 2);
                aoId[2] = getAOAt(worldModel, gx + width - 1, gy + 1, gz, dir, 1);
                aoId[3] = getAOAt(worldModel, gx, gy + 1, gz, dir, 0);
                break;
            case NEG_Y:
                aoId[0] = getAOAt(worldModel, gx, gy - 1, gz, dir, 0);
                aoId[1] = getAOAt(worldModel, gx + width - 1, gy - 1, gz, dir, 1);
                aoId[2] = getAOAt(worldModel, gx + width - 1, gy - 1, gz + height - 1, dir, 2);
                aoId[3] = getAOAt(worldModel, gx, gy - 1, gz + height - 1, dir, 3);
                break;
            default:
                throw new IllegalArgumentException("Direction invalide");
        }

        return aoId;
    }
    
    private static boolean isTransparentForAO(WorldModel world, int x, int y, int z) {
         return world.getBlockAt(x, y, z) <= BlockType.AIR.getId();
    }

    private static int getAOAt(WorldModel world, int globalX, int globalY, int globalZ, Direction dir, int corner) {
        int s1x=0, s1y=0, s1z=0;
        int s2x=0, s2y=0, s2z=0;
        int cx=0, cy=0, cz=0;
        
        // corner: 0=BG, 1=BD, 2=HD, 3=HG (dans le système de coordonnées locales de la face U,V)
        
        switch(dir) {
            case POS_Z: 
            case NEG_Z:
                // U=X, V=Y
                if (corner == 0) { s1x=-1; s2y=-1; cx=-1; cy=-1; }
                else if (corner == 1) { s1x=1; s2y=-1; cx=1; cy=-1; }
                else if (corner == 2) { s1x=1; s2y=1; cx=1; cy=1; }
                else { s1x=-1; s2y=1; cx=-1; cy=1; }
                break;
            case POS_X: 
            case NEG_X:
                // U=Z, V=Y
                if (corner == 0) { s1z=-1; s2y=-1; cz=-1; cy=-1; }
                else if (corner == 1) { s1z=1; s2y=-1; cz=1; cy=-1; }
                else if (corner == 2) { s1z=1; s2y=1; cz=1; cy=1; }
                else { s1z=-1; s2y=1; cz=-1; cy=1; }
                break;
             case POS_Y:
             case NEG_Y:
                // U=X, V=Z
                if (corner == 0) { s1x=-1; s2z=-1; cx=-1; cz=-1; }
                else if (corner == 1) { s1x=1; s2z=-1; cx=1; cz=-1; }
                else if (corner == 2) { s1x=1; s2z=1; cx=1; cz=1; }
                else { s1x=-1; s2z=1; cx=-1; cz=1; }
                break;
        }

        boolean side1 = isTransparentForAO(world, globalX + s1x, globalY + s1y, globalZ + s1z);
        boolean side2 = isTransparentForAO(world, globalX + s2x, globalY + s2y, globalZ + s2z);
        boolean c = isTransparentForAO(world, globalX + cx, globalY + cy, globalZ + cz);
        
        if (!side1 && !side2) return 0; 
        return (side1?1:0) + (side2?1:0) + (c?1:0);
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
        // L'ordre dépend du flipId pour améliorer le rendu de l'ambient occlusion
        if (flipId) {
            // Triangulation alternative : utilise la diagonale v1-v3
            // Premier triangle : v1, v3, v0 (ordre corrigé pour éviter le face culling)
            indices.add(vertexIndex + 1);
            indices.add(vertexIndex + 3);
            indices.add(vertexIndex);
            
            // Second triangle : v1, v2, v3 (ordre corrigé pour éviter le face culling)
            indices.add(vertexIndex + 1);
            indices.add(vertexIndex + 2);
            indices.add(vertexIndex + 3);
        } else {
            // Triangulation normale : utilise la diagonale v0-v2
            // Premier triangle : v0, v1, v2
            indices.add(vertexIndex);
            indices.add(vertexIndex + 1);
            indices.add(vertexIndex + 2);

            // Second triangle : v0, v2, v3
            indices.add(vertexIndex);
            indices.add(vertexIndex + 2);
            indices.add(vertexIndex + 3);
        }

        // Ajout des normales et couleurs pour chaque sommet
        for (int i = 0; i < 4; i++) {
            normals.add(this.normal);
            colors.add(this.vertexColors[i]);
        }
    }
} 