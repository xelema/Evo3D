package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Eagle extends Entity {

    private boolean isCircling = true;
    private float circleRadius = 12.0f;
    private float angle = 0.0f;
    private float flightSpeed = 1.5f;

    public Eagle(double x, double y, double z) {
        super(x, y + 20, z); // Commence en altitude
        setSize(2.5f, 1.0f, 3.0f); // Envergure
    }

    @Override
    public void update(float tpf) {
        if (isCircling) {
            angle += flightSpeed * tpf;
            double dx = circleRadius * Math.cos(angle);
            double dz = circleRadius * Math.sin(angle);

            setX(getX() + dx * tpf);
            setZ(getZ() + dz * tpf);
            setY(25 + 2 * Math.sin(angle * 0.5)); // Oscillation douce
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
