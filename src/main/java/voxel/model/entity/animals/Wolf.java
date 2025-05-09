package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Wolf extends Entity {

    private boolean isPatrolling = false;
    private float patrolRadius = 6.0f;
    private float patrolSpeed = 4.0f;

    private boolean isResting = true;
    private boolean isNight = false;

    private float patrolAngle = 0.0f;

    public Wolf(double x, double y, double z) {
        super(x, y, z);
        setSize(2.2f, 2.2f, 3.5f); // Taille un peu plus grande qu'un mouton
    }

    @Override
    public void update(float tpf) {
        if (isNight) {
            if (!isPatrolling) {
                startPatrolling();
            } else {
                patrol(tpf);
            }
        } else {
            if (!isResting) {
                rest();
            }
        }
    }

    private void startPatrolling() {
        System.out.println("Le loup sort de sa tanière et commence à patrouiller...");
        isResting = false;
        isPatrolling = true;
    }

    private void patrol(float tpf) {
        // Mouvement circulaire autour de son point de départ
        patrolAngle += patrolSpeed * tpf;
        double dx = patrolRadius * Math.cos(patrolAngle);
        double dz = patrolRadius * Math.sin(patrolAngle);

        setX(getX() + dx * tpf);
        setZ(getZ() + dz * tpf);
    }

    private void rest() {
        System.out.println("Le loup retourne se cacher pour dormir.");
        isPatrolling = false;
        isResting = true;
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
