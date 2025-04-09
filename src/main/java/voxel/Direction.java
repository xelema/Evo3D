package voxel;

import com.jme3.math.Vector3f;

/**
 * Énumération représentant les six directions possibles pour les faces d'un bloc.
 * Chaque direction a un vecteur normal associé et permet de calculer les décalages en X, Y et Z.
 */
public enum Direction {

    POS_X(new Vector3f(1, 0, 0)), // Direction positive sur l'axe X (droite)
    NEG_X(new Vector3f(-1, 0, 0)), // Direction négative sur l'axe X (gauche)
    POS_Y(new Vector3f(0, 1, 0)), // Direction positive sur l'axe Y (haut)
    NEG_Y(new Vector3f(0, -1, 0)), // Direction négative sur l'axe Y (bas)
    POS_Z(new Vector3f(0, 0, 1)), // Direction positive sur l'axe Z (avant)
    NEG_Z(new Vector3f(0, 0, -1)); // Direction négative sur l'axe Z (arrière)

    /** Vecteur normal associé à la direction */
    private final Vector3f normal;

    /**
     * Constructeur de l'énumération.
     * 
     * @param normal Vecteur normal représentant la direction
     */
    Direction(Vector3f normal) {
        this.normal = normal;
    }

    /**
     * Récupère le vecteur normal de cette direction.
     * 
     * @return Le vecteur normal unitaire
     */
    public Vector3f getNormal() {
        return normal;
    }

    /**
     * Calcule le décalage sur l'axe X selon cette direction.
     * 
     * @return 1 pour POS_X, -1 pour NEG_X, 0 sinon
     */
    public int getOffsetX() {
        return this == POS_X ? 1 : this == NEG_X ? -1 : 0;
    }

    /**
     * Calcule le décalage sur l'axe Y selon cette direction.
     * 
     * @return 1 pour POS_Y, -1 pour NEG_Y, 0 sinon
     */
    public int getOffsetY() {
        return this == POS_Y ? 1 : this == NEG_Y ? -1 : 0;
    }

    /**
     * Calcule le décalage sur l'axe Z selon cette direction.
     * 
     * @return 1 pour POS_Z, -1 pour NEG_Z, 0 sinon
     */
    public int getOffsetZ() {
        return this == POS_Z ? 1 : this == NEG_Z ? -1 : 0;
    }
} 