package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Cow extends Entity {

    public Cow(double x, double y, double z) {
        super(x, y, z);
        setSize(4.0f, 4.0f, 4.0f);
    }

    @Override
    public void update(float tpf) {

    }

    @Override
    public boolean isMarkedForRemoval() {
        return false;
    }
}
