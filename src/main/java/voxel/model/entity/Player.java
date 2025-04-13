package voxel.model.entity;

public class Player extends Entity {
    private boolean onGround = false;

    public Player(double x, double y, double z) {
        super(x, y, z);
        float randomSize = (float) (0.3 + Math.random() * (1.5 - 0.3));
        setSize(randomSize);
    }

    @Override
    public void update(float tpf) {}
    
    /**
     * Fait sauter le joueur s'il est au sol.
     */
    public void jump() {
        if (onGround) {
            setVerticalVelocity(8.0);
            onGround = false;
        }
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
    
    /**
     * Vérifie si le joueur est au sol.
     * @return true si le joueur est au sol, false sinon
     */
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
