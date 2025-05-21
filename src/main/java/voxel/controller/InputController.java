package voxel.controller;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.system.AppSettings;

import voxel.Main;
import voxel.model.entity.Player;

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
    private static final String ACTION_SPAWN_PLAYER = "SpawnPlayer"; // Action pour faire apparaître le joueur
    private static final String ACTION_TOGGLE_FULLSCREEN = "ToggleFullscreen"; // Action pour basculer en plein écran
    private static final String ACTION_TOGGLE_COORDINATES = "ToggleCoordinates"; // Action pour afficher/masquer les coordonnées
    private static final String ACTION_TOGGLE_CAMERA_MODE = "ToggleCameraMode"; // Action pour basculer entre caméra libre et joueur
    private static final String ACTION_TOGGLE_THIRD_PERSON = "ToggleThirdPerson"; // Action pour basculer entre vue 1ère et 3ème personne
    private static final String ACTION_DEBUG_ENTITES_LIST = "DebugEntitiesList"; // Action pour afficher la liste des entités
    private static final String ACTION_OPEN_INGAME_MENU = "OpenInGameMenu"; // Action pour ouvrir le menu en jeu

    private final InputManager inputManager; // Gestionnaire d'entrées de jMonkeyEngine
    private final WorldController worldController; // Référence au contrôleur de monde
    private final EntityController entityController;
    private final Camera camera; // Référence à la caméra de la scène
    private Main app; // Référence à l'application principale
    private AppSettings settings; // Paramètres de l'application
    private GameStateManager gameStateManager; // Référence au gestionnaire d'états du jeu
    
    // Paramètres originaux pour restaurer la fenêtre
    private int originalWidth;
    private int originalHeight;
    private int originalBitsPerPixel;
    private int originalFrequency;
    private boolean originalFullscreen;

    private boolean movingForward = false; // État du mouvement vers l'avant
    private boolean movingBackward = false; // État du mouvement vers l'arrière
    private boolean movingLeft = false; // État du mouvement vers la gauche
    private boolean movingRight = false; // État du mouvement vers la droite
    private boolean movingUp = false; // État du mouvement vers le haut
    private boolean movingDown = false; // État du mouvement vers le bas
    private boolean speedFly = false; // État du vol rapide
    private boolean displayCoordinates = false; // État d'affichage des coordonnées
    
    private PlayerController playerController; // Contrôleur du joueur
    private boolean cameraControlsEnabled = true; // Ajout: état des contrôles de caméra

    /**
     * Crée un nouveau contrôleur d'entrées.
     *
     * @param inputManager Le gestionnaire d'entrées de jMonkey
     * @param worldController Référence au contrôleur de monde
     * @param entityController Référence au contrôleur d'entités
     * @param camera Référence à la caméra
     */
    public InputController(InputManager inputManager, WorldController worldController,
                           EntityController entityController, Camera camera) {
        this.inputManager = inputManager;
        this.worldController = worldController;
        this.camera = camera;
        this.entityController = entityController;
        this.playerController = new PlayerController(worldController, entityController, camera, inputManager);
        setupInputs();
    }
    
    /**
     * Définit le gestionnaire d'états du jeu
     * 
     * @param gameStateManager Le gestionnaire d'états du jeu
     */
    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Configure l'application et les paramètres pour le mode plein écran.
     * 
     * @param app L'application principale
     * @param settings Les paramètres de l'application
     */
    public void setAppAndSettings(Main app, AppSettings settings) {
        this.app = app;
        this.settings = settings;
        
        // Mémoriser les paramètres originaux
        this.originalWidth = settings.getWidth();
        this.originalHeight = settings.getHeight();
        this.originalBitsPerPixel = settings.getBitsPerPixel();
        this.originalFrequency = settings.getFrequency();
        this.originalFullscreen = settings.isFullscreen();
    }

    /**
     * Configure les mappings d'entrées et enregistre les listeners.
     */
    public void setupInputs() {
        // Clean des mappings
        inputManager.deleteMapping(ACTION_TOGGLE_WIREFRAME);
        inputManager.deleteMapping(ACTION_TOGGLE_LIGHTNING);
        inputManager.deleteMapping(ACTION_MOVE_FORWARD);
        inputManager.deleteMapping(ACTION_MOVE_BACKWARD);
        inputManager.deleteMapping(ACTION_MOVE_LEFT);
        inputManager.deleteMapping(ACTION_MOVE_RIGHT);
        inputManager.deleteMapping(ACTION_MOVE_UP);
        inputManager.deleteMapping(ACTION_MOVE_DOWN);
        inputManager.deleteMapping(ACTION_SPEED_FLY);
        inputManager.deleteMapping(ACTION_SPAWN_PLAYER);
        inputManager.deleteMapping(ACTION_TOGGLE_FULLSCREEN);
        inputManager.deleteMapping(ACTION_TOGGLE_COORDINATES);
        inputManager.deleteMapping(ACTION_TOGGLE_CAMERA_MODE);
        inputManager.deleteMapping(ACTION_TOGGLE_THIRD_PERSON);
        inputManager.deleteMapping(ACTION_DEBUG_ENTITES_LIST);
        inputManager.deleteMapping(ACTION_OPEN_INGAME_MENU);
        
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
        inputManager.addMapping(ACTION_SPAWN_PLAYER, new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping(ACTION_TOGGLE_FULLSCREEN, new KeyTrigger(KeyInput.KEY_F11));
        inputManager.addMapping(ACTION_TOGGLE_COORDINATES, new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping(ACTION_TOGGLE_CAMERA_MODE, new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping(ACTION_TOGGLE_THIRD_PERSON, new KeyTrigger(KeyInput.KEY_F5));
        inputManager.addMapping(ACTION_DEBUG_ENTITES_LIST, new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addMapping(ACTION_OPEN_INGAME_MENU, new KeyTrigger(KeyInput.KEY_ESCAPE));

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
                ACTION_SPAWN_PLAYER,
                ACTION_TOGGLE_FULLSCREEN,
                ACTION_TOGGLE_COORDINATES,
                ACTION_TOGGLE_CAMERA_MODE,
                ACTION_TOGGLE_THIRD_PERSON,
                ACTION_DEBUG_ENTITES_LIST,
                ACTION_OPEN_INGAME_MENU
        );
        
        // S'assurer que les contrôles de caméra sont activés par défaut
        cameraControlsEnabled = true;
    }

    /**
     * Active ou désactive les contrôles de la caméra (souris et clavier)
     * @param enabled true pour activer, false pour désactiver
     */
    public void setCameraControlsEnabled(boolean enabled) {
        this.cameraControlsEnabled = enabled;
        
        // Si désactivés, annuler tous les mouvements en cours
        if (!enabled) {
            movingForward = false;
            movingBackward = false;
            movingLeft = false;
            movingRight = false;
            movingUp = false;
            movingDown = false;
            speedFly = false;
            
            // Informer le contrôleur du joueur
            playerController.setMovingForward(false);
            playerController.setMovingBackward(false);
            playerController.setMovingLeft(false);
            playerController.setMovingRight(false);
            playerController.setMovingUp(false);
            playerController.setSpeedFly(false);
            
            // Désactiver FlyByCamera si l'app est disponible
            if (app != null) {
                app.getFlyByCamera().setEnabled(false);
            }
        } else if (app != null) {
            // Réactiver FlyByCamera si l'app est disponible
            app.getFlyByCamera().setEnabled(true);
        }
        
        // Configurer le curseur de souris
        inputManager.setCursorVisible(!enabled);
    }
    
    /**
     * @return true si les contrôles de caméra sont activés
     */
    public boolean areCameraControlsEnabled() {
        return cameraControlsEnabled;
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
        // Si dans un menu et que ce n'est pas la touche ESCAPE, ignorer les entrées
        if (!cameraControlsEnabled && !name.equals(ACTION_OPEN_INGAME_MENU)) {
            return;
        }
        
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
                playerController.setMovingForward(isPressed);
                break;
            case ACTION_MOVE_BACKWARD:
                movingBackward = isPressed;
                playerController.setMovingBackward(isPressed);
                break;
            case ACTION_MOVE_LEFT:
                movingLeft = isPressed;
                playerController.setMovingLeft(isPressed);
                break;
            case ACTION_MOVE_RIGHT:
                movingRight = isPressed;
                playerController.setMovingRight(isPressed);
                break;
            case ACTION_MOVE_UP:
                movingUp = isPressed;
                playerController.setMovingUp(isPressed);
                break;
            case ACTION_MOVE_DOWN:
                movingDown = isPressed;
                break;
            case ACTION_SPEED_FLY:
                speedFly = isPressed;
                playerController.setSpeedFly(isPressed);
                break;
            case ACTION_SPAWN_PLAYER:
                if (isPressed) {
                    playerController.spawnPlayerAtCamera();
                }
                break;
            case ACTION_TOGGLE_FULLSCREEN:
                if (isPressed && app != null) {
                    toggleToFullscreen(app);
                }
                break;
            case ACTION_TOGGLE_COORDINATES:
                if (isPressed) {
                    displayCoordinates = !displayCoordinates;
                    worldController.toggleCoordinatesDisplay(displayCoordinates);
                }
                break;
            case ACTION_TOGGLE_CAMERA_MODE:
                if (isPressed) {
                    playerController.toggleCameraMode();
                }
                break;
            case ACTION_TOGGLE_THIRD_PERSON:
                if (isPressed && playerController.isInPlayerMode()) {
                    playerController.toggleThirdPersonView();
                }
                break;
            case ACTION_DEBUG_ENTITES_LIST:
                if (isPressed) {
                    entityController.printEntitiesList();
                }
                break;
            case ACTION_OPEN_INGAME_MENU:
                if (isPressed && gameStateManager != null) {
                    // Si on est en jeu, afficher le menu en jeu
                    if (gameStateManager.getCurrentState() == GameStateManager.GameState.IN_GAME) {
                        gameStateManager.changeState(GameStateManager.GameState.IN_GAME_MENU);
                        setCameraControlsEnabled(false); // Désactiver les contrôles de caméra en mode menu
                    } 
                    // Si on est déjà dans le menu en jeu, revenir au jeu
                    else if (gameStateManager.getCurrentState() == GameStateManager.GameState.IN_GAME_MENU) {
                        gameStateManager.changeState(GameStateManager.GameState.IN_GAME);
                        setCameraControlsEnabled(true); // Réactiver les contrôles de caméra en mode jeu
                    }
                }
                break;
        }
    }

    /**
     * Met à jour la position de la caméra en fonction des touches enfoncées.
     * Doit être appelé depuis la méthode simpleUpdate.
     *
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void updateCameraMovement(float tpf) {
        // Ne pas mettre à jour la caméra si les contrôles sont désactivés
        if (!cameraControlsEnabled) {
            return;
        }
        
        if (playerController.isInPlayerMode() && playerController.getCurrentPlayer() != null) {
            // En mode joueur, la caméra suit le joueur
            playerController.updatePlayerMovement(tpf);
            playerController.updateCameraPosition();
        } else {
            // En mode caméra libre, comportement original
            float speed;

            // Ajustement de la vitesse de déplacement en fonction du mode de vol
            if(speedFly) {
                speed = 60f;
            }
            else{
                speed = 20f;
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
        
        // Limiter la rotation verticale de la caméra
        limitCameraRotation();
    }
    
    /**
     * Limite la rotation verticale de la caméra à environ 180 degrés (+-90° par rapport à l'horizontale)
     */
    private void limitCameraRotation() {
        // Obtenir la rotation actuelle de la caméra sous forme de quaternion
        Quaternion rotation = camera.getRotation();
        
        // Convertir le quaternion en radians
        float[] angles = new float[3];
        rotation.toAngles(angles);
        
        // angles[0] = pitch (rotation verticale)
        // angles[1] = yaw (rotation horizontale)
        // angles[2] = roll (inclinaison latérale)
        
        // Limiter le pitch à environ +-85 degrés (légèrement moins que 90° pour éviter les problèmes)
        float maxPitch = (float) Math.toRadians(85);
        boolean modified = false;
        
        if (angles[0] > maxPitch) {
            angles[0] = maxPitch;
            modified = true;
        } else if (angles[0] < -maxPitch) {
            angles[0] = -maxPitch;
            modified = true;
        }
        
        // Si l'angle a été modifié, appliquer la nouvelle rotation
        if (modified) {
            // Créer un nouveau quaternion à partir des angles radians modifiés
            Quaternion newRotation = new Quaternion();
            newRotation.fromAngles(angles);
            
            // Appliquer la nouvelle rotation à la caméra
            camera.setRotation(newRotation);
        }
    }
    
    /**
     * Bascule l'application entre le mode plein écran et le mode fenêtré.
     * 
     * @param app L'application principale
     */
    public void toggleToFullscreen(Main app) {
        boolean isCurrentlyFullscreen = settings.isFullscreen();
        
        if (isCurrentlyFullscreen) {
            settings.setResolution(originalWidth, originalHeight);
            settings.setBitsPerPixel(originalBitsPerPixel);
            settings.setFrequency(originalFrequency);
            settings.setFullscreen(false);
        } else {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode currentMode = device.getDisplayMode(); // Obtenir le mode d'affichage actuel de l'écran
            
            settings.setResolution(currentMode.getWidth(), currentMode.getHeight());
            settings.setFrequency(currentMode.getRefreshRate());
            settings.setBitsPerPixel(currentMode.getBitDepth());
            settings.setFullscreen(device.isFullScreenSupported());
        }
        
        app.setSettings(settings);
        app.restart();
    }

    /**
     * @return le joueur actuel, ou null s'il n'existe pas
     */
    public Player getCurrentPlayer() {
        return playerController.getCurrentPlayer();
    }
    
    /**
     * @return true si en mode joueur, false si en mode caméra libre
     */
    public boolean isInPlayerMode() {
        return playerController.isInPlayerMode();
    }
    
    /**
     * @return le contrôleur du joueur
     */
    public PlayerController getPlayerController() {
        return playerController;
    }
}