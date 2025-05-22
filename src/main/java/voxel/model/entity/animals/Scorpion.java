package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Scorpion extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Gecko_Animations.glb";

    private boolean isMoving = false;
    private float targetDistance = 0.0f;
    private final float movementSpeed = 5.0f; // Plus rapide que la moyenne
    private final float maxDistance = 6.0f;
    
    // Nouveaux attributs pour le système de vélocité
    private float movingTime = 0.0f;
    private float targetMovingTime = 0.0f;

    private boolean isHiding = false;

    public Scorpion(double x, double y, double z) {
        super(x, y, z);
        setSize(1.0f, 0.5f, 1.0f); // Petit et bas
    }

    @Override
    public void update(float tpf) {
        if (!isMoving && !isHiding) {
            if (Math.random() < 0.7) {
                startMoving();
            } else {
                hideUnderSand();
            }
        }

        if (isMoving) {
            updateMovement(tpf);
        }
        
        // Si l'entité n'est pas en mouvement, arrêter la vélocité
        if (!isMoving) {
            stopHorizontalMovement();
        }
    }

    public void startMoving() {
        // Calculer le temps nécessaire pour parcourir la distance cible
        targetDistance = (float) (Math.random() * maxDistance);
        targetMovingTime = targetDistance / movementSpeed;
        movingTime = 0.0f;
        isMoving = true;
        
        // Définir la vélocité en fonction de la direction actuelle
        double vx = movementSpeed * Math.sin(rotation);
        double vz = movementSpeed * Math.cos(rotation);
        setVelocity(vx, getVy(), vz);
    }

    public void updateMovement(float tpf) {
        movingTime += tpf;
        
        // Vérifier si le mouvement est terminé
        if (movingTime >= targetMovingTime) {
            isMoving = false;
            stopHorizontalMovement();
        } else {
            // Maintenir la direction de mouvement
            double vx = movementSpeed * Math.sin(rotation);
            double vz = movementSpeed * Math.cos(rotation);
            setVelocity(vx, getVy(), vz);
        }
    }

    private void hideUnderSand() {
        if (!isHiding) {
            isHiding = true;
            stopHorizontalMovement(); // Arrêter le mouvement pendant la cachette
            // System.out.println("Le scorpion se cache sous le sable...");
            new Thread(() -> {
                try {
                    Thread.sleep(4000); // 4 secondes de camouflage
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isHiding = false;
                // System.out.println("Le scorpion sort de sa cachette.");
            }).start();
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
