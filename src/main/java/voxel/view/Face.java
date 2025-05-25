package voxel.view;

import java.util.ArrayList;
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

    private static final int[] aoId = new int[4];
    
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
     * 
     * @param dir Direction de la face
     * @param x Coordonnée X du bloc
     * @param y Coordonnée Y du bloc
     * @param z Coordonnée Z du bloc
     * @param color Couleur de la face
     * @param lightning Indique si l'éclairage doit être appliqué à la face
     * @return Une nouvelle face orientée dans la direction indiquée
     */
    public static Face createFromDirection(Direction dir, int x, int y, int z, ColorRGBA color, boolean lightning, WorldModel worldModel, int cx, int cy, int cz) {
        Vector3f v0, v1, v2, v3;
        ColorRGBA[] vertexColors = new ColorRGBA[4];

        switch (dir) {
            case POS_Z: // Face avant (Z+)
                getAmbientOcclusion(worldModel, cx, cy, cz, x, y, z+1, dir);
                v0 = new Vector3f(x, y, z + 1);
                v1 = new Vector3f(x + 1, y, z + 1);
                v2 = new Vector3f(x + 1, y + 1, z + 1);
                v3 = new Vector3f(x, y + 1, z + 1);
                break;
            case NEG_Z: // Face arrière (Z-)
                getAmbientOcclusion(worldModel, cx, cy, cz, x, y, z-1, dir);
                v0 = new Vector3f(x + 1, y, z);
                v1 = new Vector3f(x, y, z);
                v2 = new Vector3f(x, y + 1, z);
                v3 = new Vector3f(x + 1, y + 1, z);
                break;
            case POS_X: // Face droite (X+)
                getAmbientOcclusion(worldModel, cx, cy, cz, x+1, y, z, dir);
                v0 = new Vector3f(x + 1, y, z + 1);
                v1 = new Vector3f(x + 1, y, z);
                v2 = new Vector3f(x + 1, y + 1, z);
                v3 = new Vector3f(x + 1, y + 1, z + 1);
                break;
            case NEG_X: // Face gauche (X-)
                getAmbientOcclusion(worldModel, cx, cy, cz, x-1, y, z, dir);
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x, y, z + 1);
                v2 = new Vector3f(x, y + 1, z + 1);
                v3 = new Vector3f(x, y + 1, z);
                break;
            case POS_Y: // Face supérieure (Y+)
                getAmbientOcclusion(worldModel, cx, cy, cz, x, y + 1, z, dir);
                v0 = new Vector3f(x, y + 1, z + 1);
                v1 = new Vector3f(x + 1, y + 1, z + 1);
                v2 = new Vector3f(x + 1, y + 1, z);
                v3 = new Vector3f(x, y + 1, z);
                break;
            case NEG_Y: // Face inférieure (Y-)
                getAmbientOcclusion(worldModel, cx, cy, cz, x, y - 1, z, dir);
                v0 = new Vector3f(x, y, z);
                v1 = new Vector3f(x + 1, y, z);
                v2 = new Vector3f(x + 1, y, z + 1);
                v3 = new Vector3f(x, y, z + 1);
                break;
            default:
                throw new IllegalArgumentException("Direction invalide");
        }

        for (int i = 0; i < 4; i++) {
            int ao_source_index;
            if (dir == Direction.POS_Y) {
                // Pour POS_Y, les sommets v0,v1,v2,v3 de la face correspondent respectivement
                // aux valeurs d'occlusion stockées dans aoId[3], aoId[2], aoId[1], et aoId[0].
                if (i == 0) ao_source_index = 3;      // vertexColors[0] (pour v0) utilise aoId[3]
                else if (i == 1) ao_source_index = 2; // vertexColors[1] (pour v1) utilise aoId[2]
                else if (i == 2) ao_source_index = 1; // vertexColors[2] (pour v2) utilise aoId[1]
                else ao_source_index = 0;             // vertexColors[3] (pour v3) utilise aoId[0] (i doit être 3)

            } else if (dir == Direction.POS_Z) {
                // Mapping for POS_Z: v0->aoId[0], v1->aoId[3], v2->aoId[2], v3->aoId[1]
                if (i == 0) ao_source_index = 0;
                else if (i == 1) ao_source_index = 3;
                else if (i == 2) ao_source_index = 2;
                else ao_source_index = 1; // i == 3

            } else if (dir == Direction.NEG_Z) {
                // Corrected mapping for NEG_Z: v0->aoId[3], v1->aoId[0], v2->aoId[1], v3->aoId[2]
                if (i == 0) ao_source_index = 3;      // v0
                else if (i == 1) ao_source_index = 0; // v1
                else if (i == 2) ao_source_index = 1; // v2
                else ao_source_index = 2;             // v3 (i must be 3)

            } else if (dir == Direction.POS_X) {
                // Corrected mapping for POS_X: v0->aoId[3], v1->aoId[0], v2->aoId[1], v3->aoId[2]
                if (i == 0) ao_source_index = 3;      // v0
                else if (i == 1) ao_source_index = 0; // v1
                else if (i == 2) ao_source_index = 1; // v2
                else ao_source_index = 2;             // v3 (i must be 3)

            } else if (dir == Direction.NEG_X) {
                // Mapping for NEG_X: v0->aoId[0], v1->aoId[3], v2->aoId[2], v3->aoId[1]
                if (i == 0) ao_source_index = 0;
                else if (i == 1) ao_source_index = 3;
                else if (i == 2) ao_source_index = 2;
                else ao_source_index = 1; // i == 3

            } else { // Implicitly NEG_Y
                // For NEG_Y, mapping is v0->aoId[0], v1->aoId[1], v2->aoId[2], v3->aoId[3]
                ao_source_index = i;
            }

            int ao_sum = aoId[ao_source_index]; // ao_sum est 0, 1, 2, ou 3 (nombre de blocs AIR)

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
        boolean flipId = aoId[1] + aoId[3] > aoId[0] + aoId[2];
        
        return new Face(v0, v1, v2, v3, dir.getNormal(), vertexColors, flipId);
    }

    private static void getAmbientOcclusion(WorldModel world, int cx, int cy, int cz,
                                            int x, int y, int z, Direction dir) {

        float worldXStart = cx * ChunkModel.SIZE - (float) (world.getWorldSizeX() * ChunkModel.SIZE) / 2;
        float worldZStart = cz * ChunkModel.SIZE - (float) (world.getWorldSizeZ() * ChunkModel.SIZE) / 2;

        int globalX = (int) (worldXStart + x);
        int globalY = (cy * ChunkModel.SIZE) + y;
        int globalZ = (int) (worldZStart + z);

        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        int f = 0;
        int g = 0;
        int h = 0;

        if (dir == Direction.POS_Y || dir == Direction.NEG_Y) {
            a = (world.getBlockAt(globalX, globalY, globalZ - 1) <= (BlockType.AIR.getId())) ? 1 : 0;
            b = (world.getBlockAt(globalX - 1, globalY, globalZ - 1) <= (BlockType.AIR.getId())) ? 1 : 0;
            c = (world.getBlockAt(globalX - 1, globalY, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            d = (world.getBlockAt(globalX - 1, globalY, globalZ + 1) <= (BlockType.AIR.getId())) ? 1 : 0;
            e = (world.getBlockAt(globalX, globalY, globalZ + 1) <= (BlockType.AIR.getId())) ? 1 : 0;
            f = (world.getBlockAt(globalX + 1, globalY, globalZ + 1) <= (BlockType.AIR.getId())) ? 1 : 0;
            g = (world.getBlockAt(globalX + 1, globalY, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            h = (world.getBlockAt(globalX + 1, globalY, globalZ - 1) <= (BlockType.AIR.getId())) ? 1 : 0;
        } else if (dir == Direction.POS_X || dir == Direction.NEG_X) {
            a = (world.getBlockAt(globalX, globalY, globalZ - 1) == (BlockType.AIR.getId())) ? 1 : 0;
            b = (world.getBlockAt(globalX, globalY-1, globalZ - 1) == (BlockType.AIR.getId())) ? 1 : 0;
            c = (world.getBlockAt(globalX, globalY - 1, globalZ) == (BlockType.AIR.getId())) ? 1 : 0;
            d = (world.getBlockAt(globalX, globalY - 1, globalZ + 1) == (BlockType.AIR.getId())) ? 1 : 0;
            e = (world.getBlockAt(globalX, globalY, globalZ + 1) == (BlockType.AIR.getId())) ? 1 : 0;
            f = (world.getBlockAt(globalX, globalY + 1, globalZ + 1) == (BlockType.AIR.getId())) ? 1 : 0;
            g = (world.getBlockAt(globalX, globalY + 1, globalZ) == (BlockType.AIR.getId())) ? 1 : 0;
            h = (world.getBlockAt(globalX, globalY + 1, globalZ - 1) == (BlockType.AIR.getId())) ? 1 : 0;
        } else {
            a = (world.getBlockAt(globalX - 1, globalY, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            b = (world.getBlockAt(globalX - 1, globalY - 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            c = (world.getBlockAt(globalX, globalY - 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            d = (world.getBlockAt(globalX + 1, globalY - 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            e = (world.getBlockAt(globalX + 1, globalY, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            f = (world.getBlockAt(globalX + 1, globalY + 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            g = (world.getBlockAt(globalX, globalY + 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
            h = (world.getBlockAt(globalX - 1, globalY + 1, globalZ) <= (BlockType.AIR.getId())) ? 1 : 0;
        }

        aoId[0] = (a + b + c);
        aoId[1] = (g + h + a);
        aoId[2] = (e + f + g);
        aoId[3] = (c + d + e);
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