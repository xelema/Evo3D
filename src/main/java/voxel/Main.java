package voxel;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

/**
 * Classe principale qui est le point d'entrée du programme.
 * Hérite de SimpleApplication pour gérer le cycle de vie de l'application jMonkeyEngine.
 */
public class Main extends SimpleApplication {

    /** Référence au monde voxel */
    private VoxelWorld voxelWorld;
    
    /** Référence au gestionnaire d'entrées */
    private InputHandler inputHandler;

    /**
     * Point d'entrée du programme.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Main app = new Main();
        
        // Configuration des paramètres de l'application
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Voxel World");
        settings.setResolution(1600, 900);
        settings.setFullscreen(false);
        
        app.setSettings(settings);
        app.start();
    }

    /**
     * Initialise l'application.
     * Appelé automatiquement par jMonkeyEngine au démarrage.
     */
    @Override
    public void simpleInitApp() {
        setupCamera();
        setupVoxelWorld();
        setupInputHandler();
    }

    /**
     * Met à jour l'application à chaque image.
     * Appelé automatiquement par jMonkeyEngine à chaque frame.
     *
     * @param tpf Temps écoulé depuis la dernière image (time per frame)
     */
    @Override
    public void simpleUpdate(float tpf) {
        // Mise à jour des mouvements de la caméra
        inputHandler.updateCameraMovement(tpf);
        
        // Mise à jour du monde voxel
        voxelWorld.update(tpf, cam.getLocation().x, cam.getLocation().y, cam.getLocation().z);
    }

    /**
     * Configure la caméra et les paramètres d'affichage.
     */
    private void setupCamera() {
        // Positionnement initial de la caméra
        cam.setLocation(new Vector3f(10f, 10f, 30f));
        
        // Fond noir
        viewPort.setBackgroundColor(ColorRGBA.Black);
        
        // Configuration de la caméra volante
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(0); // Désactive le mouvement par défaut (géré par InputHandler)
        flyCam.setRotationSpeed(1f);
        flyCam.setDragToRotate(false);
    }

    /**
     * Crée et initialise le monde voxel.
     */
    private void setupVoxelWorld() {
        voxelWorld = new VoxelWorld(this);
        rootNode.attachChild(voxelWorld.getNode());
    }

    /**
     * Crée et initialise le gestionnaire d'entrées.
     */
    private void setupInputHandler() {
        inputHandler = new InputHandler(inputManager, voxelWorld, cam);
    }
} 