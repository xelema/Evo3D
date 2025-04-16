package voxel.controller;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import voxel.model.WorldModel;
import voxel.model.entity.Player;
import voxel.view.WorldRenderer;

/**
 * Contrôleur principal du jeu qui coordonne tous les autres contrôleurs.
 * Gère la logique globale de l'application.
 */
public class GameController {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Référence au renderer du monde */
    private final WorldRenderer worldRenderer;
    
    /** Référence au contrôleur d'entrées */
    private final InputController inputController;
    
    /** Référence au contrôleur du monde */
    private final WorldController worldController;

    /** Référence au contrôleur d'entités */
    private final EntityController entityController;
    
    /** Référence au contrôleur du joueur */
    private final PlayerController playerController;
    
    /** Caméra pour la position du joueur */
    private final Camera camera;

    /**
     * Crée un nouveau contrôleur de jeu.
     * 
     * @param worldModel Le modèle du monde
     * @param worldRenderer Le renderer du monde
     * @param inputController Le contrôleur d'entrées
     * @param worldController Le contrôleur du monde
     * @param camera La caméra du joueur
     */
    public GameController(WorldModel worldModel, WorldRenderer worldRenderer, 
                         InputController inputController, WorldController worldController,
                         EntityController entityController, Camera camera) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
        this.inputController = inputController;
        this.worldController = worldController;
        this.entityController = entityController;
        this.playerController = inputController.getPlayerController();
        this.camera = camera;
    }

    /**
     * Initialise le jeu. Appelé une seule fois au démarrage.
     */
    public void initialize() {
        // Aucune initialisation supplémentaire nécessaire pour l'instant
        // Tout est configuré dans les constructeurs des composants
        
        // Créer automatiquement un joueur à une position de spawn
        Vector3f spawnPosition = new Vector3f(0f, 160f, 0f);
        Player player = (Player) entityController.createEntity(Player.class, spawnPosition);

        // Définir ce joueur comme le joueur actuel et activer le mode joueur
        playerController.setCurrentPlayer(player);
        
        // Activer le mode joueur (simule un appui sur la touche V)
        inputController.onAction("ToggleCameraMode", true, 0);
    }

    /**
     * Met à jour l'état du jeu à chaque frame.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf) {
        // Mise à jour des mouvements de la caméra
        inputController.updateCameraMovement(tpf);
        
        // Mise à jour du monde voxel avec la position actuelle de la caméra
        worldController.update(tpf);

        entityController.update(tpf);
    }
} 