package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Cow extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Colobus_Animations.glb";

    boolean isRotating = false;
    float targetRotation = 0.0f;
    float rotationSpeed = 1.0f;
    
    boolean isMoving = false;
    float targetDistance = 0.0f;
    float movementSpeed = 5.0f;
    float maxDistance = 10.0f;

    public Cow(double x, double y, double z) {
        super(x, y, z);
        setSize(2.0f, 2.0f, 4.0f);
    }

    @Override
    public void update(float tpf) {
        // Gestion de la rotation
        if (!isRotating) {
            // Démarrer une nouvelle rotation aléatoire entre -PI et PI
            targetRotation = (float) ((Math.random() * 2*Math.PI) - Math.PI);
//            System.out.println("Nouvelle rotation cible: " + targetRotation);
            isRotating = true;
        } else {
            // Continuer la rotation en cours
            setRotation(targetRotation, tpf);
        }
        
        // Gestion du mouvement
        if (!isMoving) {
            // Démarrer un nouveau mouvement avec une distance aléatoire
            targetDistance = (float) (Math.random() * maxDistance);
//            System.out.println("Nouvelle distance cible: " + targetDistance);
            isMoving = true;
        } else {
            // Continuer le mouvement en cours
            setMoving(targetDistance, tpf);
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
     * Fait sauter l'entité s'il est au sol.
     */
    public void jump() {
        if (onGround) {
            setVerticalVelocity(8.0);
            onGround = false;
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
