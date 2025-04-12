package voxel.model.entity;

public class Player extends Entity {

    public Player(double x, double y, double z) {
        super(x, y, z);
        setVerticalVelocity(-5f);
        float randomSize = (float) (0.3 + Math.random() * (1.5 - 0.3));
        setSize(randomSize);
    }

    @Override
    public void update(float tpf) {
        // Logique de mise à jour du joueur
        move(tpf);
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 2;
    }
}
