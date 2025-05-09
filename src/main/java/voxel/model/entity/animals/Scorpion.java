package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Scorpion extends Entity {

    private boolean isMoving = false;
    private float targetDistance = 0.0f;
    private final float movementSpeed = 5.0f; // Plus rapide que la moyenne
    private final float maxDistance = 6.0f;

    private boolean isHiding = false;

    public Scorpion(double x, double y, double z) {
        super(x, y, z);
        setSize(1.0f, 0.5f, 1.0f); // Petit et bas
    }

    @Override
    public void update(float tpf) {
        if (!isMoving && !isHiding) {
            if (Math.random() < 0.7) {
                targetDistance = (float) (Math.random() * maxDistance);
                isMoving = true;
            } else {
                hideUnderSand();
            }
        }

        if (isMoving) {
            moveForward(tpf);
        }
    }

    private void hideUnderSand() {
        if (!isHiding) {
            isHiding = true;
            System.out.println("Le scorpion se cache sous le sable...");
            new Thread(() -> {
                try {
                    Thread.sleep(4000); // 4 secondes de camouflage
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isHiding = false;
                System.out.println("Le scorpion sort de sa cachette.");
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
