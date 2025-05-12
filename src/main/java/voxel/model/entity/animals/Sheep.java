package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Sheep extends Entity {

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.0f;
    
    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 4.0f;  // Un peu plus lent que la vache
    float maxDistance = 8.0f;    // Moins de distance que la vache

    boolean isGrazing = false;

    public Sheep(double x, double y, double z) {
        super(x, y, z);
        setSize(2.0f, 2.0f, 3.0f);  // Taille légèrement plus petite que la vache
    }

    @Override
    public void update(float tpf) {
        // Gestion de la rotation
        if (!isRotating) {
            // Démarrer une nouvelle rotation aléatoire entre -PI et PI
            targetRotation = (float) ((Math.random() * 2 * Math.PI) - Math.PI);
            isRotating = true;
        } else {
            // Continuer la rotation en cours
            setRotation(targetRotation, tpf);
        }
        
        // Gestion du mouvement
        if (!isMoving) {
            // Démarrer un nouveau mouvement avec une distance aléatoire
            targetDistance = (float) (Math.random() * maxDistance);
            isMoving = true;
        } else {
            // Continuer le mouvement en cours
            setMoving(targetDistance, tpf);
        }
        
        // Comportement de broutage (grazing) : Si le mouton est immobile, il peut commencer à brouter.
        if (!isMoving && !isRotating) {
            startGrazing();
        }
    }

    public void setRotation(float targetRot, float tpf) {
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

    /**
     * Fait broutter l'entité si elle est immobile.
     */
    public void startGrazing() {
        if (!isGrazing) {
            // System.out.println("Le mouton commence à brouter !");
            isGrazing = true;
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

    public void setMoving(float distance, float tpf) {
        // Calculer l'incrément de mouvement pour cette frame
        float step = movementSpeed * tpf;
        
        // Si on est presque à la destination, terminer le mouvement
        if (distance <= step) {
            // Avancer de la distance restante
            moveForward(distance);
            isMoving = false;  // Mouvement terminé
        } else {
            // Avancer d'un pas
            moveForward(step);
            targetDistance -= step;
        }
    }
    
    private void moveForward(float distance) {
        // Calculer le déplacement en fonction de la rotation actuelle
        double dx = distance * Math.sin(rotation);
        double dz = distance * Math.cos(rotation);
        
        // Mettre à jour la position
        setX(getX() + dx);
        setZ(getZ() + dz);
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
