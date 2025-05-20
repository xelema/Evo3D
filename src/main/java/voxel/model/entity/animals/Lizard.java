package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Lizard extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Gecko_Animations.glb";

    private boolean isBasking = false; // En train de se chauffer au soleil
    private boolean isMoving = false;

    private float targetDistance = 0.0f;
    private float movementSpeed = 2.0f; // Lent et saccadé
    private float maxDistance = 3.0f;

    public Lizard(double x, double y, double z) {
        super(x, y, z);
        setSize(1.0f, 0.5f, 2.0f); // Petit corps plat
    }

    @Override
    public void update(float tpf) {
        if (!isMoving && !isBasking) {
            // Choix aléatoire : se déplacer ou se chauffer
            if (Math.random() < 0.5) {
                startMoving();
            } else {
                startBasking();
            }
        }

        if (isMoving) {
            move(tpf);
        }
    }

    private void startMoving() {
        targetDistance = (float) (Math.random() * maxDistance);
        isMoving = true;
        // System.out.println("Le lézard se déplace légèrement...");
    }

    public void move(float tpf) {
        float step = movementSpeed * tpf;

        if (targetDistance <= step) {
            moveForward(targetDistance);
            isMoving = false;
            // System.out.println("Le lézard s'arrête.");
        } else {
            moveForward(step);
            targetDistance -= step;
        }
    }

    private void moveForward(float distance) {
        double dx = distance * Math.sin(rotation);
        double dz = distance * Math.cos(rotation);
        setX(getX() + dx);
        setZ(getZ() + dz);
    }

    private void startBasking() {
        isBasking = true;
        // System.out.println("Le lézard se chauffe au soleil...");
        new Thread(() -> {
            try {
                Thread.sleep(4000); // Il se chauffe pendant 4 secondes
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isBasking = false;
            // System.out.println("Le lézard a terminé sa séance de bronzage.");
        }).start();
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
