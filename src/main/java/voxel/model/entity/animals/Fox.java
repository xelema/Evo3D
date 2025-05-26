package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Fox extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Muskrat_Animations.glb";

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.2f;

    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 6.0f;
    float maxDistance = 10.0f;
    
    // Nouveaux attributs pour le système de vélocité
    float movingTime = 0.0f;
    float targetMovingTime = 0.0f;

    boolean isHunting = false;

    public Fox(double x, double y, double z) {
        super(x, y, z);
        setSize(0.8f, 1f, 2f); // Taille proche du mouton
    }

    @Override
    public void update(float tpf) {
        // Gestion de la rotation
        if (!isRotating && !isHunting) {
            targetRotation = (float) ((Math.random() * 2 * Math.PI) - Math.PI);
            isRotating = true;
        } else if (isRotating) {
            updateRotation(targetRotation, tpf);
        }

        // Gestion du mouvement
        if (!isMoving && !isHunting) {
            if (Math.random() < 0.2) {
                startHunting();
            } else {
                startMoving();
            }
        } else if (isMoving) {
            updateMovement(tpf);
        }
        
        // Si l'entité n'est pas en mouvement, arrêter la vélocité
        if (!isMoving) {
            stopHorizontalMovement();
        }
    }

    public void updateRotation(float targetRot, float tpf) {
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
            // Maintenir la direction de mouvement (au cas où la rotation change)
            double vx = movementSpeed * Math.sin(rotation);
            double vz = movementSpeed * Math.cos(rotation);
            setVelocity(vx, getVy(), vz);
        }
    }

    public void startHunting() {
        if (!isHunting) {
            // System.out.println("Le renard part à la chasse !");
            isHunting = true;
            stopHorizontalMovement(); // Arrêter le mouvement pendant la chasse
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Simuler une chasse rapide
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isHunting = false;
                // System.out.println("Le renard a terminé sa chasse.");
            }).start();
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
