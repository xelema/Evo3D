package voxel.model;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.renderer.ViewPort;

public class MiniMap extends SimpleApplication {

    private ViewPort miniMapView;
    private Camera miniMapCam;
    private Geometry playerGeom;
    private Node playerNode;
    private boolean forward, backward, left, right;
    private com.jme3.input.controls.ActionListener actionListener = new com.jme3.input.controls.ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            switch (name) {
                case "Forward": forward = isPressed; break;
                case "Backward": backward = isPressed; break;
                case "Left": left = isPressed; break;
                case "Right": right = isPressed; break;
            }
        }
    };

    public static void main(String[] args) {
        MiniMap app = new MiniMap();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Monde : un sol simple
        Box ground = new Box(20, 0.1f, 20);
        Geometry groundGeom = new Geometry("Ground", ground);
        Material groundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        groundMat.setColor("Color", ColorRGBA.Gray);
        groundGeom.setMaterial(groundMat);
        rootNode.attachChild(groundGeom);

        // Créer le "joueur" : une petite sphère
        playerNode = new Node("PlayerNode");
        Sphere playerSphere = new Sphere(16, 16, 0.5f);
        playerGeom = new Geometry("Player", playerSphere);
        Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        playerMat.setColor("Color", ColorRGBA.Red);
        playerGeom.setMaterial(playerMat);
        playerNode.attachChild(playerGeom);

        playerNode.setLocalTranslation(0, 1, 0); // Position initiale
        rootNode.attachChild(playerNode);

        // Créer la mini-caméra (vue du dessus)
        miniMapCam = cam.clone();
        miniMapCam.setViewPort(0.75f, 1f, 0f, 0.25f); // en haut à droite
        miniMapCam.setLocation(new Vector3f(0, 40, 0)); // en hauteur
        miniMapCam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y); // regarde vers le bas (Y)

        miniMapView = renderManager.createMainView("MiniMapView", miniMapCam);
        miniMapView.setClearFlags(true, true, true);
        miniMapView.attachScene(rootNode);

        // Déplacement libre désactivé
        flyCam.setEnabled(false);

        // Ajout des touches pour le mouvement en temps réel.
        inputManager.addMapping("Forward", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_W));
        inputManager.addMapping("Backward", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_S));
        inputManager.addMapping("Left", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_A));
        inputManager.addMapping("Right", new com.jme3.input.controls.KeyTrigger(com.jme3.input.KeyInput.KEY_D));
        inputManager.addListener(actionListener, "Forward", "Backward", "Left", "Right");
    }

    @Override
    public void simpleUpdate(float tpf) {
        // Simulation : déplacer le joueur
        float speed = 5f;
        if (forward) playerNode.move(0, 0, -speed * tpf);
        if (backward) playerNode.move(0, 0, speed * tpf);
        if (left) playerNode.move(-speed * tpf, 0, 0);
        if (right) playerNode.move(speed * tpf, 0, 0);
        // Mettre à jour la position de la mini-cam pour suivre le joueur
        Vector3f playerPos = playerNode.getWorldTranslation();
        miniMapCam.setLocation(new Vector3f(playerPos.x, 40, playerPos.z));
        miniMapCam.lookAt(new Vector3f(playerPos.x, 0, playerPos.z), Vector3f.UNIT_Y);

        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
    }
}



