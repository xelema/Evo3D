package voxel.model.entity;

public class Player extends Entity {

    //public static String MODEL_PATH = "Sukuna Model/Sukuna Character GLTF.j3o";
//    public static String MODEL_PATH = "Quirky-Series-FREE-Animals-v1.4/3D Files/GLTF/Animations/Colobus_Animations.glb";

    public Player(double x, double y, double z) {
        super(x, y, z);
//        float randomSize = (float) (0.3 + Math.random() * (1.5 - 0.3));
        setSize(1.8f, 3.8f, 1.4f);
    }

    @Override
    public void update(float tpf) {}
    
    /**
     * Fait sauter le joueur s'il est au sol.
     */
    public void jump() {
        if (onGround) {
            setVerticalVelocity(8.0);
            onGround = false;
        }
    }

    @Override
    public boolean isMarkedForRemoval() {
        return false;
    }
}
