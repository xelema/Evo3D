package voxel.model;

import java.util.Random;

/**
 * Cette classe implémente l'algorithme de génération de bruit de Perlin en 2D.
 */
public class PerlinNoise {

    // Tableau de permutation pour les calculs de bruit de Perlin
    private final int[] permutation;

    // Tableau des gradients pour le bruit de Perlin 3D
    private double[][] grad3D = {
        {1,1,0}, {-1,1,0}, {1,-1,0}, {-1,-1,0},
        {1,0,1}, {-1,0,1}, {1,0,-1}, {-1,0,-1},
        {0,1,1}, {0,-1,1}, {0,1,-1}, {0,-1,-1},
        {1,1,1}, {-1,1,1}, {1,-1,1}, {-1,-1,-1}
    };

    /**
     * Constructeur pour la classe PerlinNoise.
     * Initialise la permutation à l'aide d'une seed fournie pour obtenir des résultats reproductibles.
     * @param seed La graine utilisée pour initialiser le générateur de nombres aléatoires pour la permutation.
     */
    public PerlinNoise(int seed){
        permutation = new int[512];
        Random rand = new Random(seed);

        // Initialisation de la permutation de base de 0 à 255
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        // Mélange de la permutation avec des échanges aléatoires
        for (int i = 0; i < 256; i++) {
            int j = rand.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }

        // Doublage de la permutation pour simplifier les calculs de boucle
        for (int i = 256; i < 512; i++) {
            permutation[i] = permutation[i - 256];
        }
    }

    /**
     * Génère un bruit de Perlin en 2D pour des coordonnées données.
     * @param x Coordonnée x dans l'espace 2D.
     * @param y Coordonnée y dans l'espace 2D.
     * @return Un nombre flottant représentant le bruit de Perlin à la position (x, y).
     */
    public float Noise2D(float x, float y){
        // Partie entière des coordonnées
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        
        // Partie décimale des coordonnées
        float xf = x - (int) Math.floor(x);
        float yf = y - (int) Math.floor(y);

        // Lissage des coordonnées
        float u = fade(xf);
        float v = fade(yf);

        // Indices pseudo-aléatoires pour chaque coin 
        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        // Interpolation linéaire des valeurs de gradient
        float x1 = lerp(grad2D(aa, xf, yf), grad2D(ba, xf - 1, yf), u);
        float x2 = lerp(grad2D(ab, xf, yf - 1), grad2D(bb, xf - 1, yf - 1), u);
        
        // Interpolation finale dans la direction verticale
        float result = lerp(x1, x2, v);

        // Retourne une valeur normalisée entre 0 et 1
        return (result + 1f) / 2f;
    }

    /**
     * Fonction de lissage (fade) utilisée pour rendre le bruit de Perlin plus doux.
     * @param t La valeur à lisser.
     * @return La valeur lissée.
     */
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Interpolation linéaire entre deux valeurs.
     * @param a La première valeur.
     * @param b La deuxième valeur.
     * @param t Le facteur d'interpolation entre a et b.
     * @return La valeur interpolée entre a et b.
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    /**
     * Calcule le gradient en 2D à partir de l'indice du tableau de permutation.
     * @param hash L'indice pour obtenir le gradient.
     * @param x La coordonnée x du point.
     * @param y La coordonnée y du point.
     * @return La valeur du gradient calculée.
     */
    private float grad2D(int hash, float x, float y) {
        int h = hash & 15;
        double[] g = grad3D[h % grad3D.length];
        return (float)(g[0] * x + g[1] * y);
    }
}
