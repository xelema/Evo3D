package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Owl extends Entity {

    boolean isFlying = false;
    float targetAltitude = 10.0f;   // Altitude de vol par défaut
    float flightSpeed = 3.5f;       // Vitesse de montée/descente
    float glideSpeed = 5.0f;        // Vitesse en vol

    boolean isResting = true;
    boolean isNight = false;

    public Owl(double x, double y, double z) {
        super(x, y, z);
        setSize(1.5f, 1.5f, 1.5f); // Plus petit qu'un mouton
    }

    @Override
    public void update(float tpf) {
        if (isNight) {
            if (isResting) {
                takeOff();
            } else {
                glide(tpf);
            }
        } else {
            if (!isResting) {
                land();
            }
        }
    }

    public void takeOff() {
        System.out.println("Le hibou déploie ses ailes et décolle...");
        isFlying = true;
        isResting = false;
        new Thread(() -> {
            try {
                while (this.y < targetAltitude) {
                    this.y += 0.5f;
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void glide(float tpf) {
        float dx = (float) (glideSpeed * Math.sin(rotation) * tpf);
        float dz = (float) (glideSpeed * Math.cos(rotation) * tpf);
        setX(getX() + dx);
        setZ(getZ() + dz);

        // Tourne doucement
        this.rotation += 0.1f * tpf;
    }

    public void land() {
        System.out.println("Le hibou retourne se percher.");
        isFlying = false;
        isResting = true;
        new Thread(() -> {
            try {
                while (this.y > 2.0f) {
                    this.y -= 0.5f;
                    Thread.sleep(200);
                }
                this.y = 2.0f;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
