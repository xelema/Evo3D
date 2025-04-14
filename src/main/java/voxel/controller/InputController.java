package voxel.controller;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import voxel.model.entity.Player;
import voxel.Main;
import com.jme3.system.AppSettings;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

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

    private final InputManager inputManager; // Gestionnaire d'entrées de jMonkeyEngine
    private final WorldController worldController; // Référence au contrôleur de monde
    private final EntityController entityController;
    private final Camera camera; // Référence à la caméra de la scène
    private Main app; // Référence à l'application principale
    private AppSettings settings; // Paramètres de l'application
    
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
    
    private Player currentPlayer = null; // Référence au joueur actuel
    private boolean inPlayerMode = false; // Mode joueur (true) ou mode caméra libre (false)
    private boolean thirdPersonView = false; // Vue à la 3ème personne (true) ou vue à la 1ère personne (false)

    /**
     * Crée un nouveau contrôleur d'entrées.
     *
     * @param inputManager Le gestionnaire d'entrées de jMonkey
     * @param worldController Référence au contrôleur de monde
     * @param camera Référence à la caméra
     */
    public InputController(InputManager inputManager, WorldController worldController,
                           EntityController entityController, Camera camera) {
        this.inputManager = inputManager;
        this.worldController = worldController;
        this.camera = camera;
        this.entityController = entityController;
        setupInputs();
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
        inputManager.addMapping(ACTION_SPAWN_PLAYER, new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping(ACTION_TOGGLE_FULLSCREEN, new KeyTrigger(KeyInput.KEY_F11));
        inputManager.addMapping(ACTION_TOGGLE_COORDINATES, new KeyTrigger(KeyInput.KEY_F3));
        inputManager.addMapping(ACTION_TOGGLE_CAMERA_MODE, new KeyTrigger(KeyInput.KEY_V));
        inputManager.addMapping(ACTION_TOGGLE_THIRD_PERSON, new KeyTrigger(KeyInput.KEY_F5));

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
                ACTION_TOGGLE_THIRD_PERSON
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
            case ACTION_SPAWN_PLAYER:
                if (isPressed) {
                    // Crée une entité joueur à la position actuelle de la caméra
                    System.out.println("Spawn player");
                    currentPlayer = (Player) entityController.createEntityAtCamera(Player.class);
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
                    toggleCameraMode();
                }
                break;
            case ACTION_TOGGLE_THIRD_PERSON:
                if (isPressed && inPlayerMode) {
                    toggleThirdPersonView();
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
        if (inPlayerMode && currentPlayer != null) {
            // En mode joueur, la caméra suit le joueur
            updatePlayerMovement(tpf);
            
            double playerX = currentPlayer.getX();
            double playerY = currentPlayer.getY();
            double playerZ = currentPlayer.getZ();
            
            if (thirdPersonView) {
                // Vue à la 3ème personne
                Vector3f cameraDirection = camera.getDirection().normalize();
                Vector3f cameraPosition = new Vector3f(
                    (float)playerX - cameraDirection.x * 3,
                    (float)playerY + 2, // Un peu au-dessus du joueur
                    (float)playerZ - cameraDirection.z * 5
                );
                
                camera.setLocation(cameraPosition);
            } else {
                // Vue à la 1ère personne
                // Calculer la position des yeux (90% de la hauteur du joueur depuis le bas)
                // Sachant que le joueur a une hauteur de 1.9, et le point de référence est au centre du joueur
                // La caméra doit être à playerY + (hauteur/2 - 0.1) pour être à 10% du haut
                float eyeHeight = (float)(playerY + currentPlayer.getHeight() * 0.4);
                
                Vector3f cameraPosition = new Vector3f(
                    (float)playerX,
                    eyeHeight, // À la hauteur des yeux
                    (float)playerZ
                );
                
                camera.setLocation(cameraPosition);
            }
        } else {
            // En mode caméra libre, comportement original
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
     * Bascule entre le mode caméra libre et le mode joueur.
     */
    private void toggleCameraMode() {
        if (currentPlayer == null) {
            // Si pas de joueur actuel, en créer un à la position de la caméra
            currentPlayer = (Player) entityController.createEntityAtCamera(Player.class);
        }
        
        inPlayerMode = !inPlayerMode;
        System.out.println("Mode " + (inPlayerMode ? "joueur" : "caméra libre") + " activé");
        
        // Ajuster le frustum de la caméra en fonction du mode
        updateCameraFrustum();
    }

    /**
     * Met à jour la position du joueur en fonction des touches enfoncées.
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    private void updatePlayerMovement(float tpf) {
        if (currentPlayer == null) return;
        
        // Vitesse de déplacement du joueur
        float speed = speedFly ? 12f : 5f;
        
        // Vecteurs de direction basés sur l'orientation de la caméra
        Vector3f camDir = camera.getDirection().clone();
        camDir.y = 0; // Maintenir le déplacement sur le plan horizontal
        camDir.normalizeLocal();
        
        Vector3f camLeft = camera.getLeft().clone();
        camLeft.y = 0; // Maintenir le déplacement sur le plan horizontal
        camLeft.normalizeLocal();
        
        // Calcul du vecteur de mouvement
        Vector3f moveDir = new Vector3f(0, 0, 0);
        
        if (movingForward) moveDir.addLocal(camDir);
        if (movingBackward) moveDir.addLocal(camDir.negate());
        if (movingLeft) moveDir.addLocal(camLeft);
        if (movingRight) moveDir.addLocal(camLeft.negate());
        
        // Normaliser le vecteur de mouvement s'il n'est pas nul
        if (moveDir.length() > 0) {
            moveDir.normalizeLocal();
            moveDir.multLocal(speed);
        }
        
        // Saut
        if (movingUp && currentPlayer.isOnGround()) {
            currentPlayer.jump();
        }
        
        // Appliquer la vitesse horizontale
        currentPlayer.setVelocity(moveDir.x, currentPlayer.getVy(), moveDir.z);
    }

    /**
     * @return le joueur actuel, ou null s'il n'existe pas
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * @return true si en mode joueur, false si en mode caméra libre
     */
    public boolean isInPlayerMode() {
        return inPlayerMode;
    }
    
    /**
     * Définit l'entité joueur actuelle.
     * 
     * @param player l'entité joueur
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Bascule entre la vue à la première personne et la vue à la troisième personne.
     */
    private void toggleThirdPersonView() {
        thirdPersonView = !thirdPersonView;
        System.out.println("Vue " + (thirdPersonView ? "à la 3ème personne" : "à la 1ère personne") + " activée");
        
        // Ajuster le frustum de la caméra en fonction du mode de vue
        updateCameraFrustum();
    }
    
    /**
     * Met à jour le frustum de la caméra en fonction du mode actuel.
     * En mode première personne, le plan near est plus proche pour éviter le clipping.
     */
    private void updateCameraFrustum() {
        float aspectRatio = (float) camera.getWidth() / camera.getHeight();
        
        if (inPlayerMode && !thirdPersonView) {
            // En mode première personne, utiliser un plan near plus proche
            camera.setFrustumPerspective(90f, aspectRatio, 0.1f, 1000f);
        } else {
            // En mode libre ou troisième personne, utiliser le plan near par défaut
            camera.setFrustumPerspective(80f, aspectRatio, 1f, 1000f);
        }
        
        // Appliquer les changements à la caméra
        camera.update();
    }
}