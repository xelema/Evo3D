package voxel.model.entity;

import voxel.model.physics.BoundingBox;

public abstract class Entity {

    protected double x, y, z;
    protected double vx, vy, vz; // vitesse x, y, z
    protected float height; // hauteur de l'entité
    protected float width; // largeur de l'entité
    protected float depth; // profondeur de l'entité
    protected float rotation = 0.0f; // rotation horizontale de l'entité (en radians)
    protected boolean onGround = false;
    protected BoundingBox boundingBox; // Boîte de collision
    protected boolean markedForRemoval = false; // Indique si l'entité doit être supprimée

    public Entity(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = 0;
        this.vy = 0;
        this.vz = 0;
        this.height = 1.0f; // Taille par défaut
        this.width = 1.0f;
        this.depth = 1.0f;
        this.boundingBox = new BoundingBox(x, y, z, width, height, depth);
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
        
        // Mise à jour de la boîte de collision
        boundingBox.update(x, y, z);
    }
    
    /**
     * Déplace l'entité en prenant en compte les collisions.
     * Cette méthode doit être appelée par le gestionnaire d'entités.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     * @param newX Nouvelle position X après résolution de collision
     * @param newY Nouvelle position Y après résolution de collision
     * @param newZ Nouvelle position Z après résolution de collision
     */
    public void moveWithCollision(float tpf, double newX, double newY, double newZ) {
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        
        // Mise à jour de la boîte de collision
        boundingBox.update(x, y, z);
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
        if (boundingBox != null) {
            boundingBox.update(x, y, z);
        }
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
        if (boundingBox != null) {
            boundingBox.update(x, y, z);
        }
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
        if (boundingBox != null) {
            boundingBox.update(x, y, z);
        }
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
     * Définit la rotation horizontale de l'entité.
     * @param rotation Rotation en radians
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    
    /**
     * Récupère la rotation horizontale de l'entité.
     * @return Rotation en radians
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Détermine si l'entité doit être supprimée du monde.
     * @return true si l'entité doit être supprimée, false sinon.
     */
    public abstract boolean isMarkedForRemoval();

    /**
     * Marque l'entité pour suppression.
     */
    public void markForRemoval() {
        this.markedForRemoval = true;
    }

    /**
     * Vérifie si l'entité a été marquée manuellement pour suppression.
     * @return true si l'entité a été marquée pour suppression, false sinon.
     */
    protected boolean isManuallyMarkedForRemoval() {
        return markedForRemoval;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDepth() {
        return depth;
    }


    public void setSize(float width, float height, float depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        if (boundingBox != null) {
            // Recréer la boîte de collision avec la nouvelle taille
            boundingBox = new BoundingBox(x, y, z, (width+depth)/2, height, (width+depth)/2);
        }
    }
    
    /**
     * Récupère la boîte de collision de l'entité.
     * @return La boîte de collision
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Vérifie si le joueur est au sol.
     * @return true si le joueur est au sol, false sinon
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Définit si le joueur est au sol ou non.
     * @param onGround true si le joueur est au sol, false sinon
     */
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;

        // Réinitialise la vitesse verticale si le joueur touche le sol
        if (onGround && this.getVy() < 0) {
            setVerticalVelocity(0);
        }
    }
}
