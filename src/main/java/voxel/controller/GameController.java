package voxel.controller;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

import voxel.model.WorldModel;
import voxel.model.entity.Entity;
import voxel.model.entity.Player;
import voxel.model.entity.animals.*;
import voxel.view.WorldRenderer;
import voxel.model.ChunkModel;
import java.util.Arrays;
import java.util.List;

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

    /** Permet d'initialiser le monde après un certain temps */
    boolean readyToInit = true;

    /** Compteur de temps */
    float timeElapsed = 0;
    float lastTimeElapsed = 0;

    int[][] treeSize = {
            // Très petits arbres (width <= 5 || height <= 5)
            {3, 3},
            {3, 4},
            {5, 5},
            // Petits arbres (width <= 10 || height <= 10)
            {6, 6},
            {7, 8},
            {9, 9},
            {10, 10},
            // Grands arbres (width > 10 && height > 10)
            {12, 12},
            {15, 15},
            {18, 18},
            {24, 20},
            {30, 22},
            {35, 25},
            {40, 28},
            {45, 31},
            {50, 35},
            {55, 38},
            {60, 40},
            {65, 45},
            {70, 50},
            {75, 55},
            {80, 60},
            {85, 65},
            {90, 70},
            {95, 75},
            {100, 80},
            {105, 85},

    };

    int state = 0;


    /** Indique si les entités ont été initialisées */
    private boolean entitiesInitialized = false;

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

        // Définir les coordonnées de spawn X et Z
        int spawnX = 8;
        int spawnZ = -2;
        
        // Trouver la hauteur du sol à ces coordonnées
        int groundHeight = worldModel.getGroundHeightAt(spawnX, spawnZ);
        
        // Si aucun sol n'est trouvé, utiliser une hauteur par défaut
        float spawnY = (groundHeight != -1) ? groundHeight + 2f : 155f; // +2 pour que le joueur soit légèrement au-dessus du sol
        
        // Créer automatiquement un joueur à la position de spawn calculée
        Vector3f spawnPosition = new Vector3f(spawnX, spawnY, spawnZ);
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
    public void update(float tpf, ViewPort mainViewport) {
        // Mise à jour des mouvements de la caméra
        inputController.updateCameraMovement(tpf);
        
        // Mise à jour du monde voxel avec la position actuelle de la caméra
        worldController.update(tpf, mainViewport);

        // Permet d'initialiser proprement le monde
        if (timeElapsed > 0.1 && readyToInit){
            initialize();
            readyToInit = false;
        }

        entityController.update(tpf);
        timeElapsed += tpf;
    }
} 