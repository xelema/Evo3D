package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Owl extends Entity {

    public static String MODEL_PATH = "Models/Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Sparrow_Animations.glb";

    boolean isFlying = false;
    float targetAltitude = 10.0f;   // Altitude de vol par défaut
    float flightSpeed = 3.5f;       // Vitesse de montée/descente
    float glideSpeed = 5.0f;        // Vitesse en vol

    boolean isResting = true;
    boolean isNight = false;
    
    boolean isTakingOff = false;
    boolean isLanding = false;

    public Owl(double x, double y, double z) {
        super(x, y, z);
        setSize(1.5f, 1f, 1.5f); // Plus petit qu'un mouton
    }

    @Override
    public void update(float tpf) {
        if (isNight) {
            if (isResting && !isTakingOff) {
                takeOff();
            } else if (isTakingOff) {
                updateTakeOff(tpf);
            } else if (isFlying) {
                glide(tpf);
            }
        } else {
            if (!isResting && !isLanding) {
                land();
            } else if (isLanding) {
                updateLanding(tpf);
            }
        }
    }

    public void takeOff() {
        // System.out.println("Le hibou déploie ses ailes et décolle...");
        isTakingOff = true;
        isResting = false;
        // Définir une vélocité verticale pour monter
        setVelocity(0, flightSpeed, 0);
    }

    public void updateTakeOff(float tpf) {
        // Vérifier si on a atteint l'altitude cible
        if (getY() >= targetAltitude) {
            isTakingOff = false;
            isFlying = true;
            // Commencer le vol horizontal
            setVelocity(0, 0, 0);
        }
        // Sinon, continuer à monter avec la vélocité déjà définie
    }

    public void glide(float tpf) {
        // Mouvement horizontal pendant le vol
        double vx = glideSpeed * Math.sin(rotation);
        double vz = glideSpeed * Math.cos(rotation);
        setVelocity(vx, 0, vz);

        // Tourne doucement
        this.rotation += 0.1f * tpf;
    }

    public void land() {
        // System.out.println("Le hibou retourne se percher.");
        isFlying = false;
        isLanding = true;
        // Définir une vélocité verticale négative pour descendre
        setVelocity(0, -flightSpeed, 0);
    }

    public void updateLanding(float tpf) {
        // Vérifier si on a atteint le sol (hauteur minimale)
        if (getY() <= 2.0f) {
            isLanding = false;
            isResting = true;
            // Arrêter tout mouvement
            setVelocity(0, 0, 0);
            setY(2.0f); // Fixer la position au sol
        }
        // Sinon, continuer à descendre avec la vélocité déjà définie
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return (this.y < 0);
    }
}
