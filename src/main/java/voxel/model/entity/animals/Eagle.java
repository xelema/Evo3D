package voxel.model.entity.animals;

import voxel.model.entity.Entity;

public class Eagle extends Entity {

    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Sparrow_Animations.glb";

    private boolean isCircling = true;
    private float circleRadius = 12.0f;
    private float angle = 0.0f;
    private float flightSpeed = 1.5f;
    private double centerX, centerZ; // Centre du cercle de vol

    public Eagle(double x, double y, double z) {
        super(x, y + 80, z); // Commence en altitude
        setSize(2.5f, 10.0f, 3.0f); // Envergure
        // Mémoriser le centre du cercle de vol
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public void update(float tpf) {
        if (isCircling) {
            angle += flightSpeed * tpf;
            
            // Calculer la position cible dans le cercle
            double targetX = centerX + circleRadius * Math.cos(angle);
            double targetZ = centerZ + circleRadius * Math.sin(angle);
            double targetY = 80 + 2 * Math.sin(angle * 0.5); // Oscillation douce
            
            // Calculer les vélocités pour atteindre cette position
            double vx = (targetX - getX()) / tpf;
            double vz = (targetZ - getZ()) / tpf;
            double vy = (targetY - getY()) / tpf;
            
            // Appliquer les vélocités
            setVelocity(vx, vy, vz);
            
            // Mettre à jour la rotation pour que l'aigle regarde dans la direction du mouvement
            if (vx != 0 || vz != 0) {
                float newRotation = (float) Math.atan2(vx, vz);
                setRotation(newRotation);
            }
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return this.y < 0;
    }
}
