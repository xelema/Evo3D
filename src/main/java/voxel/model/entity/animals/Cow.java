package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Cow extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Herring_Animations.glb";

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.0f;
    
    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 5.0f;
    float maxDistance = 10.0f;
    
    // Nouveaux attributs pour le système de vélocité
    float movingTime = 0.0f;
    float targetMovingTime = 0.0f;

    public Cow(double x, double y, double z) {
        super(x, y, z);
        setSize(1.5f, 1.8f, 4.0f);
    }

    @Override
    public void update(float tpf) {
        // Gestion de la rotation
        if (!isRotating) {
            // Démarrer une nouvelle rotation aléatoire entre -PI et PI
            targetRotation = (float) ((Math.random() * 2*Math.PI) - Math.PI);
            isRotating = true;
        } else {
            // Continuer la rotation en cours
            updateRotation(targetRotation, tpf);
        }
        
        // Gestion du mouvement
        if (!isMoving) {
            // Démarrer un nouveau mouvement
            startMoving();
        } else {
            // Continuer le mouvement en cours
            updateMovement(tpf);
        }
        
        // Si l'entité n'est pas en mouvement, arrêter la vélocité
        if (!isMoving) {
            stopHorizontalMovement();
        }
    }

    public void updateRotation(float targetRot, float tpf) {
        // Calculer l'incrément de rotation pour cette frame
        float step = rotationSpeed * tpf;
        
        // Si on est presque à la cible, terminer la rotation
        if (Math.abs(targetRot) <= step) {
            this.rotation += targetRot;
            isRotating = false;  // Rotation terminée
        } else {
            // Appliquer une partie de la rotation
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

    /**
     * Fait sauter l'entité s'il est au sol.
     */
    public void jump() {
        if (onGround) {
            setVerticalVelocity(8.0);
            onGround = false;
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
