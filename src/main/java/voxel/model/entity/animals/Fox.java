package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Fox extends Entity {

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.2f;

    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 6.0f;
    float maxDistance = 10.0f;

    boolean isHunting = false;

    public Fox(double x, double y, double z) {
        super(x, y, z);
        setSize(2.0f, 2.0f, 3.0f); // Taille proche du mouton
    }

    @Override
    public void update(float tpf) {
        if (!isRotating && !isHunting) {
            targetRotation = (float) ((Math.random() * 2 * Math.PI) - Math.PI);
            isRotating = true;
        } else if (isRotating) {
            setRotation(targetRotation, tpf);
        }

        if (!isMoving && !isHunting) {
            if (Math.random() < 0.2) {
                startHunting();
            } else {
                targetDistance = (float) (Math.random() * maxDistance);
                isMoving = true;
            }
        } else if (isMoving) {
            setMoving(targetDistance, tpf);
        }
    }

    public void setRotation(float targetRot, float tpf) {
        float step = rotationSpeed * tpf;
        if (Math.abs(targetRot) <= step) {
            this.rotation += targetRot;
            isRotating = false;
        } else {
            if (targetRot > 0) {
                this.rotation += step;
                targetRotation -= step;
            } else {
                this.rotation -= step;
                targetRotation += step;
            }
        }
    }

    public void setMoving(float distance, float tpf) {
        float step = movementSpeed * tpf;
        if (distance <= step) {
            moveForward(distance);
            isMoving = false;
        } else {
            moveForward(step);
            targetDistance -= step;
        }
    }

    public void startHunting() {
        if (!isHunting) {
            System.out.println("Le renard part à la chasse !");
            isHunting = true;
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Simuler une chasse rapide
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isHunting = false;
                System.out.println("Le renard a terminé sa chasse.");
            }).start();
        }
    }

    private void moveForward(float distance) {
        double dx = distance * Math.sin(rotation);
        double dz = distance * Math.cos(rotation);
        setX(getX() + dx);
        setZ(getZ() + dz);
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
