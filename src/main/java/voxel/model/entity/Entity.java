package voxel.model.entity;

public abstract class Entity {

    protected double x, y, z;
    protected double vx, vy, vz; // vitesse x, y, z
    protected float size; // taille de l'entité

    public Entity(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
        this.size = 1.0f; // Taille par défaut
    }

    public abstract void update(float tpf);
    
    /**
     * Déplace l'entité en fonction de sa vitesse et du temps écoulé.
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void move(float tpf) {
        x += vx * tpf;
        y += vy * tpf;
        z += vz * tpf;
    }

    /**
     * Définit la vitesse de l'entité dans les trois directions.
     * @param vx Vitesse en X
     * @param vy Vitesse en Y
     * @param vz Vitesse en Z
     */
    public void setVelocity(double vx, double vy, double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }
    
    /**
     * Ajoute une valeur à la vitesse actuelle (accélération).
     * @param dvx Changement de vitesse en X
     * @param dvy Changement de vitesse en Y
     * @param dvz Changement de vitesse en Z
     */
    public void addVelocity(double dvx, double dvy, double dvz) {
        this.vx += dvx;
        this.vy += dvy;
        this.vz += dvz;
    }
    
    /**
     * Arrête tout mouvement de l'entité.
     */
    public void stopMovement() {
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
    }
    
    /**
     * Arrête le mouvement horizontal (X et Z).
     */
    public void stopHorizontalMovement() {
        this.vx = 0;
        this.vz = 0;
    }
    
    /**
     * Modifie la vitesse verticale (Y).
     * @param vy Nouvelle vitesse verticale
     */
    public void setVerticalVelocity(double vy) {
        this.vy = vy;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    /**
     * Détermine si l'entité doit être supprimée du monde.
     * @return true si l'entité doit être supprimée, false sinon.
     */
    public abstract boolean isMarkedForRemoval();

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

}
