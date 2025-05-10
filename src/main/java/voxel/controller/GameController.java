package voxel.controller;

import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

import voxel.model.WorldModel;
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
                         Camera camera) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
        this.inputController = inputController;
        this.worldController = worldController;
        this.camera = camera;
    }

    /**
     * Initialise le jeu. Appelé une seule fois au démarrage.
     */
    public void initialize() {
        // Aucune initialisation supplémentaire nécessaire pour l'instant
        // Tout est configuré dans les constructeurs des composants
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
    }
} 