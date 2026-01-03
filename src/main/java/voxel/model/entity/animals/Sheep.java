package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Sheep extends Entity {

    public static String MODEL_PATH = "Models/Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Colobus_Animations.glb";

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.0f;
    
    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 4.0f;  // Un peu plus lent que la vache
    float maxDistance = 8.0f;    // Moins de distance que la vache
    
    // Nouveaux attributs pour le système de vélocité
    float movingTime = 0.0f;
    float targetMovingTime = 0.0f;

    boolean isGrazing = false;

    public Sheep(double x, double y, double z) {
        super(x, y, z);
        setSize(2.0f, 3.6f, 3.0f);  // Taille légèrement plus petite que la vache
    }

    @Override
    public void update(float tpf) {
        // Gestion de la rotation
        if (!isRotating && !isGrazing) {
            // Démarrer une nouvelle rotation aléatoire entre -PI et PI
            targetRotation = (float) ((Math.random() * 2 * Math.PI) - Math.PI);
            isRotating = true;
        } else if (isRotating) {
            // Continuer la rotation en cours
            updateRotation(targetRotation, tpf);
        }
        
        // Gestion du mouvement
        if (!isMoving && !isGrazing) {
            // Démarrer un nouveau mouvement
            startMoving();
        } else if (isMoving) {
            // Continuer le mouvement en cours
            updateMovement(tpf);
        }
        
        // Comportement de broutage : Si le mouton est immobile, il peut commencer à brouter.
        if (!isMoving && !isRotating && !isGrazing) {
            startGrazing();
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
     * Fait broutter l'entité si elle est immobile.
     */
    public void startGrazing() {
        if (!isGrazing) {
            // System.out.println("Le mouton commence à brouter !");
            isGrazing = true;
            stopHorizontalMovement(); // Arrêter le mouvement pendant le broutage
            // Simuler un broutage avec une petite attente (par exemple, 5 secondes de broutage)
            // Après 5 secondes, le mouton reprend son activité normale.
            new Thread(() -> {
                try {
                    Thread.sleep(5000);  // Le mouton broutte pendant 5 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isGrazing = false;  // Fin du broutage
                // System.out.println("Le mouton a fini de brouter.");
            }).start();
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
