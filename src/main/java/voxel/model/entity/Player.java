package voxel.model.entity;

public class Player extends Entity {

    public Player(double x, double y, double z) {
        super(x, y, z);
        vy= -5f;
    }

    @Override
    public void update(float tpf) {
        // Logique de mise à jour du joueur
        // Par exemple, mettre à jour la position en fonction de la vitesse
        x += vx * tpf;
        y += vy * tpf;
        z += vz * tpf;

        // Limiter la position pour qu'elle reste dans le monde
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (z < 0) z = 0;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 2;
    }
}
