package voxel;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import voxel.controller.EntityController;
import voxel.controller.GameController;
import voxel.controller.InputController;
import voxel.controller.WorldController;
import voxel.model.WorldModel;
import voxel.view.WorldRenderer;

/**
 * Classe principale qui est le point d'entrée du programme.
 * Hérite de SimpleApplication pour gérer le cycle de vie de l'application jMonkeyEngine.
 */
public class Main extends SimpleApplication {

    /** Référence au contrôleur principal du jeu */
    private GameController gameController;

    /**
     * Point d'entrée du programme.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Main app = new Main();
        
        // Configuration des paramètres de l'application
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Evo3D");
        settings.setResolution(1600, 900);
        settings.setFullscreen(false);
        settings.setVSync(true);
        settings.setGammaCorrection(true);
        settings.setSamples(4); // Anti-aliasing

        app.setSettings(settings);
        app.start();
    }

    /**
     * Initialise l'application.
     * Appelé automatiquement par jMonkeyEngine au démarrage.
     */
    @Override
    public void simpleInitApp() {
        // Permet de faire des screenshots
        ScreenshotAppState screenShotState = new ScreenshotAppState(".");
        this.stateManager.attach(screenShotState);

        setupCamera();
        setupMVC();
    }

    /**
     * Met à jour l'application à chaque image.
     * Appelé automatiquement par jMonkeyEngine à chaque frame.
     *
     * @param tpf Temps écoulé depuis la dernière image (time per frame)
     */
    @Override
    public void simpleUpdate(float tpf) {
        // Déléguer la mise à jour au contrôleur de jeu
        gameController.update(tpf);
    }

    /**
     * Configure la caméra et les paramètres d'affichage.
     */
    private void setupCamera() {
        // Positionnement initial de la caméra au-dessus de l'île
        cam.setLocation(new Vector3f(-4f, 153f, 7f));
        // Fond bleu ciel
        viewPort.setBackgroundColor(new ColorRGBA((float) 135/255, (float) 206/255, (float) 235/255, 1.0F));
        
        // Configuration de la caméra volante (de base avec JME3)
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(1f);
        flyCam.setDragToRotate(false);
    }

    /**
     * Configure l'architecture MVC (Modèle-Vue-Contrôleur).
     */
    private void setupMVC() {
        // Création des composants selon l'architecture MVC
        
        // Modèle - Représente les données
        WorldModel worldModel = new WorldModel();
        
        // Vue - Gère l'affichage
        WorldRenderer worldRenderer = new WorldRenderer(worldModel, assetManager);
        rootNode.attachChild(worldRenderer.getNode());
        
        // Initialisation de l'interface utilisateur
        worldRenderer.initializeUI(guiNode, cam);
        
        // Contrôleurs - Gèrent les interactions et la logique
        WorldController worldController = new WorldController(worldModel, worldRenderer);
        EntityController entityController = new EntityController(worldModel, worldRenderer, cam);
        InputController inputController = new InputController(inputManager, worldController, entityController, cam);
        
        // Passer l'application et les paramètres pour le mode plein écran
        inputController.setAppAndSettings(this, settings);

        // Contrôleur principal qui coordonne tout
        gameController = new GameController(worldModel, worldRenderer, inputController, worldController,
                entityController, cam);
        gameController.initialize();
    }

}