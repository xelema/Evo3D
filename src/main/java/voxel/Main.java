package voxel;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import voxel.controller.GameController;
import voxel.controller.InputController;
import voxel.controller.WorldController;
import voxel.model.WorldModel;
import voxel.view.MenuPrincipalApp;
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
        settings.setResolution(1280, 720);
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
        setupMVC();
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
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
        // Positionnement initial de la caméra
        cam.setLocation(new Vector3f(10f, 10f, 30f));
        
        // Fond noir
        viewPort.setBackgroundColor(ColorRGBA.Black);
        
        // Configuration de la caméra volante
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(0); // Désactive le mouvement par défaut (géré par InputController)
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
        
        // Menu - Initialise le menu principal comme un HUD
        MenuPrincipalApp menu = new MenuPrincipalApp();
        menu.initialize(this);
        
        // Vue - Gère l'affichage
        WorldRenderer worldRenderer = new WorldRenderer(worldModel, assetManager);
        rootNode.attachChild(worldRenderer.getNode());
        
        // Contrôleurs - Gèrent les interactions et la logique
        WorldController worldController = new WorldController(worldModel, worldRenderer);
        InputController inputController = new InputController(inputManager, worldController, cam, menu);
        
        // Contrôleur principal qui coordonne tout
        gameController = new GameController(worldModel, worldRenderer, inputController, worldController, cam);
        gameController.initialize();
    }
} 