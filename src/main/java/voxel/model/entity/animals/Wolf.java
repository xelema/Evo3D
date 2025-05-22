package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Wolf extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Pudu_Animations.glb";

    private boolean isPatrolling = false;
    private float patrolRadius = 6.0f;
    private float patrolSpeed = 4.0f;

    private boolean isResting = true;
    private boolean isNight = false;

    private float patrolAngle = 0.0f;

    public Wolf(double x, double y, double z) {
        super(x, y, z);
        setSize(2.2f, 2.2f, 3.5f); // Taille un peu plus grande qu'un mouton
    }

    @Override
    public void update(float tpf) {
        if (isNight) {
            if (!isPatrolling) {
                startPatrolling();
            } else {
                patrol(tpf);
            }
        } else {
            if (!isResting) {
                rest();
            }
        }
    }

    private void startPatrolling() {
        // System.out.println("Le loup sort de sa tanière et commence à patrouiller...");
        isResting = false;
        isPatrolling = true;
    }

    private void patrol(float tpf) {
        // Mouvement circulaire autour de son point de départ
        patrolAngle += patrolSpeed * tpf;
        
        // Calculer la vélocité pour le mouvement circulaire
        double vx = -patrolRadius * patrolSpeed * Math.sin(patrolAngle);
        double vz = patrolRadius * patrolSpeed * Math.cos(patrolAngle);
        
        // Appliquer la vélocité
        setVelocity(vx, getVy(), vz);
        
        // Mettre à jour la rotation pour que le loup regarde dans la direction du mouvement
        if (vx != 0 || vz != 0) {
            float newRotation = (float) Math.atan2(vx, vz);
            setRotation(newRotation);
        }
    }

    private void rest() {
        // System.out.println("Le loup retourne se cacher pour dormir.");
        isPatrolling = false;
        isResting = true;
        // Arrêter le mouvement pendant le repos
        stopHorizontalMovement();
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
