package voxel;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import voxel.controller.GameStateManager;
import voxel.model.BiomeType;
import voxel.model.WorldModel;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * Classe principale qui est le point d'entrée du programme.
 * Hérite de SimpleApplication pour gérer le cycle de vie de l'application jMonkeyEngine.
 */
public class Main extends SimpleApplication {

    /** Référence au gestionnaire d'états du jeu */
    private GameStateManager gameStateManager;

    /**
     * Point d'entrée du programme.
     * 
     * @param args Arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        Main app = new Main();

        // Obtention de la résolution et du taux de rafraîchissement de l'écran principal
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode displayMode = device.getDisplayMode();
        int screenWidth = displayMode.getWidth();
        int screenHeight = displayMode.getHeight();
        int refreshRate = displayMode.getRefreshRate();

        // Configuration des paramètres de l'application
        AppSettings settings = new AppSettings(true);
        settings.setTitle("Evo3D");
        settings.setResolution(screenWidth, screenHeight);
        settings.setFullscreen(true);
        settings.setFrequency(refreshRate); // Définir le taux de rafraîchissement
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

        // Enmpêche la possibilité de quitter le jeu via 'ESC'
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        
        // Initialiser le gestionnaire d'états du jeu
        gameStateManager = new GameStateManager(this);

        // Démarrer avec l'île volante
        gameStateManager.changeState(GameStateManager.GameState.IN_GAME);
    }

    /**
     * Met à jour l'application à chaque image.
     * Appelé automatiquement par jMonkeyEngine à chaque frame.
     *
     * @param tpf Temps écoulé depuis la dernière image (time per frame)
     */
    @Override
    public void simpleUpdate(float tpf) {
        // Déléguer la mise à jour au gestionnaire d'états du jeu
        if (gameStateManager != null) {
            gameStateManager.update(tpf);
        }
    }

    /**
     * Configure la caméra et les paramètres d'affichage.
     */
    private void setupCamera() {
        cam.setLocation(new Vector3f(0, 40f, 0));

        // Fond bleu ciel
        viewPort.setBackgroundColor(new ColorRGBA((float) 135/255, (float) 206/255, (float) 235/255, 1.0F));
        
        // Configuration de la caméra volante (de base avec JME3)
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(0);
        flyCam.setRotationSpeed(1f);
        flyCam.setDragToRotate(false);
    }
}