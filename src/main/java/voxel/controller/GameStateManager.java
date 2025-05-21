package voxel.controller;

import com.jme3.app.SimpleApplication;

import voxel.model.BiomeType;
import voxel.model.WorldModel;
import voxel.view.hud.ChooseMenu;
import voxel.view.hud.InGameMenu;
import voxel.view.hud.LoadingScreen;
import voxel.view.WorldRenderer;

/**
 * Gestionnaire des états du jeu et des transitions entre ces états.
 * Permet de gérer le cycle de vie des différents composants du jeu.
 */
public class GameStateManager {
    
    /** L'application JME3 */
    private final SimpleApplication app;
    
    /** Le modèle du monde actuel */
    private WorldModel currentWorld;
    
    /** Le rendu du monde actuel */
    private WorldRenderer worldRenderer;
    
    /** Le contrôleur du monde */
    private WorldController worldController;
    
    /** Le contrôleur des entités */
    private EntityController entityController;
    
    /** Le contrôleur des entrées utilisateur */
    private InputController inputController;
    
    /** Le contrôleur principal du jeu */
    private GameController gameController;
    
    /** Les différents menus du jeu */
    private ChooseMenu worldSelectionMenu;
    private InGameMenu inGameMenu;
    private LoadingScreen loadingScreen;
    
    /** Vitesse actuelle du temps dans le jeu */
    private float timeSpeed = 1.0f;
    
    /** État actuel du jeu */
    private GameState currentState;
    
    /**
     * États possibles du jeu
     */
    public enum GameState {
        /** Menu principal/sélection du monde */
        WORLD_SELECTION,
        
        /** Écran de chargement */
        LOADING,
        
        /** En jeu */
        IN_GAME,
        
        /** Menu en jeu */
        IN_GAME_MENU
    }
    
    /**
     * Constructeur du gestionnaire d'états
     * 
     * @param app L'application SimpleApplication
     */
    public GameStateManager(SimpleApplication app) {
        this.app = app;
        this.currentState = GameState.WORLD_SELECTION;
        
        // Initialisation des menus
        this.worldSelectionMenu = new ChooseMenu(app);
        this.inGameMenu = new InGameMenu(app, this);
        this.loadingScreen = new LoadingScreen(app);
        
        // Initialiser les menus
        worldSelectionMenu.initialize();
        inGameMenu.initialize();
        loadingScreen.initialize();
    }
    
    /**
     * Change l'état actuel du jeu
     * 
     * @param newState Le nouvel état du jeu
     */
    public void changeState(GameState newState) {
        // Nettoyage de l'état précédent
        cleanupCurrentState();
        
        // Configuration du nouvel état
        switch (newState) {
            case WORLD_SELECTION:
                setupWorldSelection();
                break;
            case LOADING:
                setupLoadingScreen();
                break;
            case IN_GAME:
                setupInGameState();
                break;
            case IN_GAME_MENU:
                setupInGameMenu();
                break;
        }
        
        currentState = newState;
    }
    
    /**
     * Nettoie l'état actuel du jeu avant de passer à un autre état
     */
    private void cleanupCurrentState() {
        // Cacher tous les menus
        worldSelectionMenu.hideMenu();
        inGameMenu.hideMenu();
        loadingScreen.hideMenu();
        
        // Actions spécifiques selon l'état actuel
        switch (currentState) {
            case WORLD_SELECTION:
                // Rien de spécial à nettoyer
                break;
            case LOADING:
                // Rien de spécial à nettoyer
                break;
            case IN_GAME:
                // Ne pas supprimer le monde, juste cacher le menu
                break;
            case IN_GAME_MENU:
                // Ne pas supprimer le monde, juste cacher le menu
                break;
        }
    }
    
    /**
     * Configure l'état de sélection du monde
     */
    private void setupWorldSelection() {
        worldSelectionMenu.showMenu();
    }
    
    /**
     * Configure l'écran de chargement
     */
    private void setupLoadingScreen() {
        loadingScreen.showMenu();
    }
    
    /**
     * Configure l'état de jeu
     */
    private void setupInGameState() {
        // Assurez-vous que le monde est initialisé
        if (worldRenderer != null) {
            app.getInputManager().setCursorVisible(false);
        } else {
            WorldModel worldModel = new WorldModel(BiomeType.SAVANNA);
            setupMVC(worldModel);
        }
    }
    
    /**
     * Configure le menu en jeu
     */
    private void setupInGameMenu() {
        inGameMenu.showMenu();
    }
    
    /**
     * Change le monde actuel pour un nouveau monde avec le biome spécifié
     * 
     * @param biome Le biome du nouveau monde
     */
    public void changeWorld(BiomeType biome) {
        // Sauvegarder l'état du monde actuel si nécessaire
        
//        // Afficher l'écran de chargement
//        changeState(GameState.LOADING);

        // Générer le nouveau monde (dans un thread séparé)
        Thread worldGenThread = new Thread(() -> {
            // Créer le nouveau monde
            WorldModel newWorld = new WorldModel(biome);

            // Initialiser le rendu et les contrôleurs pour ce monde
            app.enqueue(() -> {
                // Détruire les anciennes ressources si elles existent
                cleanupCurrentWorld();

                // Créer les nouveaux composants MVC
                setupMVC(newWorld);
                
                // Passer à l'état de jeu
                changeState(GameState.IN_GAME);
                return null;
            });
        });
        worldGenThread.start();
    }
    
    /**
     * Nettoie les ressources du monde actuel
     */
    private void cleanupCurrentWorld() {
        if (worldRenderer != null) {
            // Détacher les nœuds de rendu
            app.getRootNode().detachChild(worldRenderer.getNode());
            app.getRootNode().detachChild(worldRenderer.getSkyNode());
            
            // Réinitialiser les références
            worldRenderer = null;
            worldController = null;
            entityController = null;
            gameController = null;
            currentWorld = null;
        }
    }
    
    /**
     * Configure les composants MVC pour un nouveau monde
     * 
     * @param worldModel Le modèle du nouveau monde
     */
    private void setupMVC(WorldModel worldModel) {
        this.currentWorld = worldModel;
        
        // Vue - Gère l'affichage
        worldRenderer = new WorldRenderer(worldModel, app.getAssetManager());
        app.getRootNode().attachChild(worldRenderer.getSkyNode());
        app.getRootNode().attachChild(worldRenderer.getNode());
        
        // Initialisation de l'interface utilisateur
        worldRenderer.initializeUI(app.getGuiNode(), app.getCamera());
        
        // Contrôleurs - Gèrent les interactions et la logique
        worldController = new WorldController(worldModel, worldRenderer);
        entityController = new EntityController(worldModel, worldRenderer, app.getCamera());
        inputController = new InputController(app.getInputManager(), worldController, entityController, app.getCamera());
        
        // Contrôleur principal qui coordonne tout
        gameController = new GameController(worldModel, worldRenderer, inputController, worldController,
                entityController, app.getCamera());
    }
    
    /**
     * Définit la vitesse du temps dans le jeu
     * 
     * @param speed Nouvelle vitesse du temps
     */
    public void setTimeSpeed(float speed) {
        this.timeSpeed = speed;
        // Appliquer la vitesse au contrôleur du jeu si disponible
        if (gameController != null) {
            // Implémenter la logique pour appliquer la vitesse aux mises à jour
        }
    }
    
    /**
     * Récupère le monde actuel
     * 
     * @return Le modèle du monde actuel
     */
    public WorldModel getCurrentWorld() {
        return currentWorld;
    }
    
    /**
     * Récupère l'état actuel du jeu
     * 
     * @return L'état actuel du jeu
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Met à jour le jeu
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void update(float tpf) {
        // Mettre à jour le jeu selon l'état actuel
        if (currentState == GameState.IN_GAME && gameController != null) {
            // Appliquer la vitesse du temps
            gameController.update(tpf * timeSpeed, app.getViewPort());
        }

        if (worldSelectionMenu.hasGameStarted()){
            changeState(GameState.IN_GAME);
            worldSelectionMenu.setGameStarted(false);
        }
    }
} 