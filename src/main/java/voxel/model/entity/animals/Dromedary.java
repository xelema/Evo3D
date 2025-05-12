package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Dromedary extends Entity {

    private boolean isMoving = false;
    private float targetDistance = 0.0f;
    private final float movementSpeed = 2.0f;  // Lenteur réaliste
    private final float maxDistance = 15.0f;

    private boolean isResting = false;

    public Dromedary(double x, double y, double z) {
        super(x, y, z);
        setSize(3.0f, 3.5f, 5.0f);  // Grande taille
    }

    @Override
    public void update(float tpf) {
        if (!isMoving && !isResting) {
            if (Math.random() < 0.5) {
                targetDistance = (float) (Math.random() * maxDistance);
                isMoving = true;
            } else {
                startResting();
            }
        }

        if (isMoving) {
            moveForward(tpf);
        }
    }

    private void startResting() {
        if (!isResting) {
            isResting = true;
            // System.out.println("Le dromadaire se repose sous le soleil du désert...");
            new Thread(() -> {
                try {
                    Thread.sleep(7000);  // Long repos de 7 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isResting = false;
                // System.out.println("Le dromadaire reprend sa route.");
            }).start();
        }
    }

    private void moveForward(float tpf) {
        float step = movementSpeed * tpf;
        if (targetDistance <= step) {
            advance(targetDistance);
            isMoving = false;
        } else {
            advance(step);
            targetDistance -= step;
        }
    }

    private void advance(float distance) {
        double dx = distance * Math.sin(rotation);
        double dz = distance * Math.cos(rotation);
        setX(getX() + dx);
        setZ(getZ() + dz);
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
