package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Dromedary extends Entity {

    public static String MODEL_PATH = "Models/Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Pudu_Animations.glb";

    private boolean isMoving = false;
    private float targetDistance = 0.0f;
    private final float movementSpeed = 2.0f;  // Lenteur réaliste
    private final float maxDistance = 15.0f;
    
    // Nouveaux attributs pour le système de vélocité
    private float movingTime = 0.0f;
    private float targetMovingTime = 0.0f;

    private boolean isResting = false;

    public Dromedary(double x, double y, double z) {
        super(x, y, z);
        setSize(3.0f, 6f, 5.0f);  // Grande taille
    }

    @Override
    public void update(float tpf) {
        if (!isMoving && !isResting) {
            if (Math.random() < 0.5) {
                startMoving();
            } else {
                startResting();
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

    private void startResting() {
        if (!isResting) {
            isResting = true;
            stopHorizontalMovement(); // Arrêter le mouvement pendant le repos
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

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
