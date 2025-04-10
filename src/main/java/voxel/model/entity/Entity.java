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
    }

    public abstract void update(float tpf);

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
     * Obtient la taille de l'entité.
     * @return La taille de l'entité en unités de monde
     */
    public float getSize() {
        return 1.0f; // Taille par défaut d'un bloc
    }

}
