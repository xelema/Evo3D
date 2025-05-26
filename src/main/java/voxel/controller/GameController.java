package voxel.controller;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;

import voxel.model.WorldModel;
import voxel.view.WorldRenderer;

import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

import com.jme3.scene.shape.Box;
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

    //Ajout test mini map
    private Spatial player;

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
        Box box = new Box(0.5f, 0.5f, 0.5f);
        Geometry playerGeom = new Geometry("Player", box);
        Material mat = new Material(worldRenderer.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        playerGeom.setMaterial(mat);
        playerGeom.setLocalTranslation(0, 1, 0);

        worldRenderer.getNode().attachChild(playerGeom);

        this.player = playerGeom;

    }

    /**
     * Met à jour l'état du jeu à chaque frame.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf) {
        // Mise à jour des mouvements de la caméra
        inputController.updateCameraMovement(tpf);
        if (player != null){
            player.setLocalTranslation(camera.getLocation());
        }
        // Mise à jour du monde voxel avec la position actuelle de la caméra
        worldController.update(tpf);
    }

    //Ajout test mini map
    public Spatial getPlayer() {
        return player;
    }
} 