package voxel.controller;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;

import voxel.view.MenuPrincipalApp;

/**
 * Gère les entrées utilisateur pour contrôler la caméra et interagir avec le monde.
 * Implémente ActionListener pour réagir aux événements clavier.
 */
public class InputController implements ActionListener {

    private static final String ACTION_TOGGLE_WIREFRAME = "ToggleWireframe"; // Action pour basculer le mode filaire
    private static final String ACTION_TOGGLE_LIGHTNING = "ToggleLightning"; // Action pour basculer l'éclairage
    private static final String ACTION_MOVE_FORWARD = "MoveForward"; // Action pour déplacer la caméra vers l'avant
    private static final String ACTION_MOVE_BACKWARD = "MoveBackward"; // Action pour déplacer la caméra vers l'arrière
    private static final String ACTION_MOVE_LEFT = "MoveLeft"; // Action pour déplacer la caméra vers la gauche
    private static final String ACTION_MOVE_RIGHT = "MoveRight"; // Action pour déplacer la caméra vers la droite
    private static final String ACTION_MOVE_UP = "MoveUp"; // Action pour déplacer la caméra vers le haut
    private static final String ACTION_MOVE_DOWN = "MoveDown"; // Action pour déplacer la caméra vers le bas
    private static final String ACTION_SPEED_FLY = "SpeedFly"; // Action pour activer le vol rapide
    private static final String ACTION_OPEN_MENU = "OpenMenu";

    private final InputManager inputManager; // Gestionnaire d'entrées de jMonkeyEngine
    private final WorldController worldController; // Référence au contrôleur de monde
    private final Camera camera; // Référence à la caméra de la scène
    private final MenuPrincipalApp menuPrincipal;

    private boolean movingForward = false; // État du mouvement vers l'avant
    private boolean movingBackward = false; // État du mouvement vers l'arrière
    private boolean movingLeft = false; // État du mouvement vers la gauche
    private boolean movingRight = false; // État du mouvement vers la droite
    private boolean movingUp = false; // État du mouvement vers le haut
    private boolean movingDown = false; // État du mouvement vers le bas
    private boolean speedFly = false; // État du vol rapide

    /**
     * Crée un nouveau contrôleur d'entrées.
     * 
     * @param inputManager Le gestionnaire d'entrées de jMonkey
     * @param worldController Référence au contrôleur de monde
     * @param camera Référence à la caméra
     */
    public InputController(InputManager inputManager, WorldController worldController, Camera camera, MenuPrincipalApp menuPrincipal) {
        this.inputManager = inputManager;
        this.worldController = worldController;
        this.camera = camera;
        this.menuPrincipal = menuPrincipal;
        setupInputs();
    }

    /**
     * Configure les mappings d'entrées et enregistre les listeners.
     */
    private void setupInputs() {
        // Configuration des mappings (association touche/action)
        inputManager.addMapping(ACTION_TOGGLE_WIREFRAME, new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping(ACTION_TOGGLE_LIGHTNING, new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping(ACTION_MOVE_FORWARD, new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping(ACTION_MOVE_BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(ACTION_MOVE_LEFT, new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(ACTION_MOVE_RIGHT, new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(ACTION_MOVE_UP, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping(ACTION_MOVE_DOWN, new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping(ACTION_SPEED_FLY, new KeyTrigger(KeyInput.KEY_LCONTROL));
        inputManager.addMapping(ACTION_OPEN_MENU,new KeyTrigger(KeyInput.KEY_G));

        // Enregistrement du listener pour toutes les actions
        inputManager.addListener(this, 
                ACTION_TOGGLE_WIREFRAME,
                ACTION_TOGGLE_LIGHTNING,
                ACTION_MOVE_FORWARD,
                ACTION_MOVE_BACKWARD, 
                ACTION_MOVE_LEFT, 
                ACTION_MOVE_RIGHT,
                ACTION_MOVE_UP, 
                ACTION_MOVE_DOWN,
                ACTION_SPEED_FLY,
                ACTION_OPEN_MENU
        );
    }

    /**
     * Appelé quand une action est déclenchée (touche appuyée ou relâchée).
     * 
     * @param name Nom de l'action
     * @param isPressed true si la touche est appuyée, false si relâchée
     * @param tpf Temps écoulé depuis la dernière image
     */
    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case ACTION_TOGGLE_WIREFRAME:
                if (isPressed) {
                    worldController.toggleWireframe();
                }
                break;
            case ACTION_TOGGLE_LIGHTNING:
                if (isPressed) {
                    worldController.toggleLightning();
                }
                break;
            case ACTION_MOVE_FORWARD: 
                movingForward = isPressed; 
                break;
            case ACTION_MOVE_BACKWARD: 
                movingBackward = isPressed; 
                break;
            case ACTION_MOVE_LEFT: 
                movingLeft = isPressed; 
                break;
            case ACTION_MOVE_RIGHT: 
                movingRight = isPressed; 
                break;
            case ACTION_MOVE_UP: 
                movingUp = isPressed; 
                break;
            case ACTION_MOVE_DOWN: 
                movingDown = isPressed; 
                break;
            case ACTION_SPEED_FLY:
                speedFly = isPressed;
                break;
            case ACTION_OPEN_MENU:
                if (isPressed) { 
                        AppSettings reglages = new AppSettings(true);
                        reglages.setResolution(1280, 720);
                        reglages.setTitle("ProjetTOB");
                        reglages.setFullscreen(false);
                        menuPrincipal.setSettings(reglages);
                        menuPrincipal.start();
                }   
        }
    }

    /**
     * Met à jour la position de la caméra en fonction des touches enfoncées.
     * Doit être appelé depuis la méthode simpleUpdate.
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void updateCameraMovement(float tpf) {
        float speed;

        // Ajustement de la vitesse de déplacement en fonction du mode de vol
        if(speedFly) {
            speed = 60f;
        }
        else{
            speed = 10f;
        }

        // Calcul des vecteurs de direction de la caméra
        Vector3f camDir = camera.getDirection().clone().multLocal(tpf * speed);
        Vector3f camLeft = camera.getLeft().clone().multLocal(tpf * speed);
        Vector3f movement = new Vector3f(0, 0, 0);

        // Application des mouvements selon les touches enfoncées
        if (movingForward) movement.addLocal(camDir);
        if (movingBackward) movement.addLocal(camDir.negate());
        if (movingLeft) movement.addLocal(camLeft);
        if (movingRight) movement.addLocal(camLeft.negate());
        if (movingUp) movement.addLocal(0, tpf * speed, 0);
        if (movingDown) movement.addLocal(0, -tpf * speed, 0);

        // Application du mouvement à la caméra
        camera.setLocation(camera.getLocation().add(movement));
    }
} 