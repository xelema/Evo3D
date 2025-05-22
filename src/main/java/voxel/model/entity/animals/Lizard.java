package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Lizard extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Gecko_Animations.glb";

    private boolean isBasking = false; // En train de se chauffer au soleil
    private boolean isMoving = false;

    private float targetDistance = 0.0f;
    private float movementSpeed = 2.0f; // Lent et saccadé
    private float maxDistance = 3.0f;
    
    // Nouveaux attributs pour le système de vélocité
    private float movingTime = 0.0f;
    private float targetMovingTime = 0.0f;

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
            updateMovement(tpf);
        }
        
        // Si l'entité n'est pas en mouvement, arrêter la vélocité
        if (!isMoving) {
            stopHorizontalMovement();
        }
    }

    private void startMoving() {
        // Calculer le temps nécessaire pour parcourir la distance cible
        targetDistance = (float) (Math.random() * maxDistance);
        targetMovingTime = targetDistance / movementSpeed;
        movingTime = 0.0f;
        isMoving = true;
        
        // Définir la vélocité en fonction de la direction actuelle
        double vx = movementSpeed * Math.sin(rotation);
        double vz = movementSpeed * Math.cos(rotation);
        setVelocity(vx, getVy(), vz);
        
        // System.out.println("Le lézard se déplace légèrement...");
    }

    public void updateMovement(float tpf) {
        movingTime += tpf;
        
        // Vérifier si le mouvement est terminé
        if (movingTime >= targetMovingTime) {
            isMoving = false;
            stopHorizontalMovement();
            // System.out.println("Le lézard s'arrête.");
        } else {
            // Maintenir la direction de mouvement
            double vx = movementSpeed * Math.sin(rotation);
            double vz = movementSpeed * Math.cos(rotation);
            setVelocity(vx, getVy(), vz);
        }
    }

    private void startBasking() {
        isBasking = true;
        stopHorizontalMovement(); // Arrêter le mouvement pendant le bain de soleil
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
