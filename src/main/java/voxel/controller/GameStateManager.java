package voxel.controller;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import voxel.Main;
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
    
    /** Vitesse spécialisée pour le soleil et la croissance des arbres */
    private float environmentTimeSpeed = 1.0f;
    
    /** État actuel du jeu */
    private GameState currentState;
    
    /** Variables pour l'affichage du message dans WORLD_SELECTION */
    private float worldSelectionTimer = 0.0f;
    private BitmapText instructionText;
    private float blinkTimer = 0.0f;
    private boolean textVisible = true;
    
    /** Référence au rectangle d'arrière-plan */
    private Geometry backgroundRectangle;
    
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
        
        // Configurer la référence au GameStateManager dans le ChooseMenu
        worldSelectionMenu.setGameStateManager(this);
        
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
                // Nettoyer le message d'instruction
                hideInstructionText();
                worldSelectionTimer = 0.0f;
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
        // Créer un monde avec le biome FLOATING_ISLAND
        if (worldRenderer == null) {
            WorldModel worldModel = new WorldModel(BiomeType.FLOATING_ISLAND, 4, 3);
            setupMVC(worldModel);
        }
        
        // Garder le curseur visible pour les interactions avec le menu de sélection
        app.getInputManager().setCursorVisible(true);
        
        // Activer les contrôles de la caméra pour explorer le monde
        if (inputController != null) {
            inputController.setCameraControlsEnabled(true);
        }
        
        // Réinitialiser le timer
        worldSelectionTimer = 0.0f;
        
        // Cacher le menu de sélection du monde pour le moment
        worldSelectionMenu.hideMenu();
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
            // Cacher le curseur en mode jeu
            app.getInputManager().setCursorVisible(false);
            
            // Activer les contrôles de la caméra
            if (inputController != null) {
                inputController.setCameraControlsEnabled(true);
            }
        } else {
            WorldModel worldModel = new WorldModel(BiomeType.FLOATING_ISLAND);
            setupMVC(worldModel);
        }
    }
    
    /**
     * Configure le menu en jeu
     */
    private void setupInGameMenu() {
        // Désactiver les contrôles de caméra quand le menu est affiché
        if (inputController != null) {
            inputController.setCameraControlsEnabled(false);
        }
        
        // Afficher le curseur pour interagir avec le menu
        app.getInputManager().setCursorVisible(true);
        
        // Réinitialiser le menu avant de l'afficher pour éviter les erreurs
        try {
            // Forcer le retour à l'écran principal du menu (pas l'écran de sélection de biome)
            if (inGameMenu.getNifty() != null) {
                inGameMenu.getNifty().gotoScreen("inGameMenu");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du changement d'écran: " + e.getMessage());
        }
        
        // Afficher le menu
        inGameMenu.showMenu();
    }
    
    /**
     * Change le monde actuel pour un nouveau monde avec le biome spécifié
     * 
     * @param biome Le biome du nouveau monde
     */
    public void changeWorld(BiomeType biome) {
        changeWorldWithParameters(biome, 2, 2, 2); // Valeurs par défaut
    }
    
    /**
     * Change le monde actuel pour un nouveau monde avec le biome et les paramètres environnementaux spécifiés
     * 
     * @param biome Le biome du nouveau monde
     * @param temperature Niveau de température (0-4)
     * @param humidity Niveau d'humidité (0-4)  
     * @param reliefComplexity Niveau de complexité du relief (0-4)
     */
    public void changeWorldWithParameters(BiomeType biome, int temperature, int humidity, int reliefComplexity) {
        // Mémoriser l'état précédent pour y revenir après le chargement
        GameState previousState = this.currentState;

        // Désactive les coordonnées
        inputController.setActionToggleCoordinates(false);
        worldController.toggleCoordinatesDisplay(false);

        // Afficher l'écran de chargement
        changeState(GameState.LOADING);

        // Générer le nouveau monde (dans un thread séparé)
        Thread worldGenThread = new Thread(() -> {
            try {
                // Simuler les étapes de génération avec la progression
                for (int i = 0; i <= 100; i++) {
                    final int progress = i;
                    // Mettre à jour la barre de progression
                    app.enqueue(() -> {
                        loadingScreen.setProgress(progress);
                        return null;
                    });
                    Thread.sleep(30); // Petite pause pour simuler le chargement
                }
                
                // Créer le nouveau monde avec les paramètres environnementaux
                WorldModel newWorld = new WorldModel(biome, WorldModel.DEFAULT_WORLD_SIZE,8, temperature, humidity, reliefComplexity);

                // Initialiser le rendu et les contrôleurs pour ce monde
                app.enqueue(() -> {
                    // Détruire les anciennes ressources si elles existent
                    cleanupCurrentWorld();

                    // Créer les nouveaux composants MVC
                    setupMVC(newWorld);
                    
                    // S'assurer que la référence au GameStateManager est correctement définie
                    if (inputController != null) {
                        // Reconfigurer complètement le contrôleur d'entrées
                        inputController.setGameStateManager(this);
                        
                        // Réinitialiser les mappings d'entrées
                        inputController.setupInputs();
                    }
                    
                    // Appliquer un délai court pour s'assurer que tout est initialisé
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                // Passer à l'état de jeu
                                app.enqueue(() -> {
                                    changeState(GameState.IN_GAME);
                                    return null;
                                });
                            }
                        }, 
                        500 // Délai de 500ms pour s'assurer que tout est bien initialisé
                    );
                    
                    return null;
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        // Définir la référence au GameStateManager pour la vitesse du temps de l'environnement
        worldRenderer.setGameStateManager(this);
        app.getRootNode().attachChild(worldRenderer.getSkyNode());
        app.getRootNode().attachChild(worldRenderer.getNode());
        
        // Initialiser l'effet de bloom pour le soleil
        worldRenderer.initializeBloomEffect(app.getViewPort());
        
        // Initialisation de l'interface utilisateur
        worldRenderer.initializeUI(app.getGuiNode(), app.getCamera());
        
        // Contrôleurs - Gèrent les interactions et la logique
        worldController = new WorldController(worldModel, worldRenderer);
        // Définir la référence au GameStateManager pour la vitesse du temps de l'environnement
        worldController.setGameStateManager(this);
        entityController = new EntityController(worldModel, worldRenderer, app.getCamera());
        inputController = new InputController(app.getInputManager(), worldController, entityController, app.getCamera());
        
        // Définir la référence au GameStateManager dans l'InputController
        inputController.setGameStateManager(this);
        
        // Définir la référence à l'application et aux paramètres
        if (app instanceof voxel.Main) {
            inputController.setAppAndSettings((Main) app, app.getContext().getSettings());
        }
        
        // Contrôleur principal qui coordonne tout
        gameController = new GameController(worldModel, worldRenderer, inputController, worldController,
                entityController, app.getCamera());
        
        // Définir la référence au GameStateManager dans le GameController pour la vitesse du temps de l'environnement
        gameController.setGameStateManager(this);
    }
    
    /**
     * Définit la vitesse du temps dans le jeu
     * 
     * @param speed Nouvelle vitesse du temps
     */
    public void setTimeSpeed(float speed) {
        this.timeSpeed = speed;
        // La vitesse du temps général n'affecte plus que l'environnement (soleil + arbres)
        this.environmentTimeSpeed = speed;
        // Ne plus appliquer au gameController pour éviter d'affecter la vitesse du joueur
    }
    
    /**
     * Récupère la vitesse du temps pour l'environnement (soleil et arbres)
     * 
     * @return La vitesse du temps pour l'environnement
     */
    public float getEnvironmentTimeSpeed() {
        return environmentTimeSpeed;
    }
    
    /**
     * Récupère le monde actuel (modele)
     * 
     * @return Le modèle du monde actuel (modele)
     */
    public WorldModel getWorldModel() {
        return currentWorld;
    }

    /**
     * Récupère le monde actuel (controlleur)
     *
     * @return Le modèle du monde actuel (controlleur)
     */
    public WorldController getWorldController() {
        return worldController;
    }
    
    /**
     * Récupère l'état actuel du jeu
     * 
     * @return L'état actuel du jeu
     */
    public GameState getCurrentState() {
        return currentState;
    }

    public ChooseMenu getWorldSelectionMenu() {
        return worldSelectionMenu;
    }
    
    /**
     * Ouvre le menu de création de simulation (ChooseMenu)
     * Utilisé quand on appuie sur X dans l'état WORLD_SELECTION
     */
    public void openCreationMenu() {
        if (currentState == GameState.WORLD_SELECTION) {
            // Désactiver les contrôles de caméra pour permettre l'interaction avec le menu
            if (inputController != null) {
                inputController.setCameraControlsEnabled(false);
            }
            
            // Afficher le curseur pour le menu
            app.getInputManager().setCursorVisible(true);
            
            // Cacher le texte d'instruction
            hideInstructionText();
            
            // Afficher le menu de choix
            worldSelectionMenu.showMenu();
        }
    }
    
    /**
     * Met à jour le jeu
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void update(float tpf) {
        // Mettre à jour le jeu selon l'état actuel
        if (currentState == GameState.IN_GAME && gameController != null) {
            // Utiliser le temps normal (non modifié) pour éviter d'affecter la vitesse du joueur
            gameController.update(tpf, app.getViewPort());
        }
        
        // Gestion spéciale pour l'état WORLD_SELECTION
        if (currentState == GameState.WORLD_SELECTION) {
            // Mettre à jour le monde en arrière-plan avec le temps normal aussi
            if (gameController != null) {
                gameController.update(tpf, app.getViewPort());
            }
            
            // Gérer l'affichage du message après 5 secondes
            worldSelectionTimer += tpf;
            if (worldSelectionTimer >= 5.0f) {
                showInstructionText();
                updateBlinkingText(tpf);
            }
        }

        // Si le joueur a cliqué sur "Démarrer le jeu" dans le menu principal
        if (worldSelectionMenu.hasGameStarted()){
            worldSelectionMenu.setGameStarted(false);
            
            // Récupérer les paramètres environnementaux du ChooseMenu
            int[] parametres = worldSelectionMenu.getParametresEnvironnementaux();
            int temperature = parametres[0];
            int humidity = parametres[1];
            int reliefComplexity = parametres[2];
            
            System.out.println("Création du monde avec les paramètres:");
            System.out.println("- Température: " + temperature);
            System.out.println("- Humidité: " + humidity);
            System.out.println("- Relief: " + reliefComplexity);
            
            // Créer le nouveau monde avec les paramètres personnalisés
            changeWorldWithParameters(null, temperature, humidity, reliefComplexity);
        }
    }
    
    /**
     * Affiche le texte d'instruction clignotant
     */
    private void showInstructionText() {
        // Ne pas afficher le texte si le menu ChooseMenu est ouvert
        if (worldSelectionMenu.isMenuVisible()) {
            return;
        }
        
        if (instructionText == null) {
            BitmapFont font = app.getAssetManager().loadFont("Fonts/ArialBlack_24.fnt");
            instructionText = new BitmapText(font, false);
//            instructionText.setSize(font.getCharSet().getRenderedSize() * 1.2f);

            // try {
            //     FtBitmapFont ftFont = new FtBitmapFont(app.getAssetManager(), "Fonts/arial.ttf", 28);
            //     instructionText = new BitmapText(ftFont, false);
            //     instructionText.setSize(28);
            // } catch (Exception e) {
            //     // Fallback vers police bitmap si TTF échoue
            //     BitmapFont font = app.getAssetManager().loadFont("Fonts/ArialBlack_24.fnt");
            //     instructionText = new BitmapText(font, false);
            //     instructionText.setSize(font.getCharSet().getRenderedSize() * 1.2f);
            // }
            
            // Configuration du texte
            instructionText.setText("Appuyer sur X pour ouvrir les paramètres de création de la simulation");
            instructionText.setColor(ColorRGBA.White);
            
            // Positionner le texte au centre avec des coordonnées entières
            float textWidth = instructionText.getLineWidth();
            float textHeight = instructionText.getLineHeight();
            float screenWidth = app.getCamera().getWidth();
            float screenHeight = app.getCamera().getHeight();
            
            int centerX = Math.round((screenWidth - textWidth) / 2);
            int centerY = Math.round(screenHeight / 2);
            
            // Arrière-plan semi-transparent
            backgroundRectangle = new Geometry("InstructionBackground", new Quad(textWidth + 60, textHeight + 40));
            Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", new ColorRGBA(0, 0, 0, 0.7f));
            material.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Alpha);
            material.setTransparent(true);
            backgroundRectangle.setMaterial(material);
            
            // Positionnement
            backgroundRectangle.setLocalTranslation(centerX - 30, centerY - textHeight - 20, -0.1f);
            instructionText.setLocalTranslation(centerX, centerY, 0);
            
            // Ajout au GUI
            app.getGuiNode().attachChild(backgroundRectangle);
            app.getGuiNode().attachChild(instructionText);
        }
    }
    
    /**
     * Cache le texte d'instruction
     */
    private void hideInstructionText() {
        if (instructionText != null) {
            app.getGuiNode().detachChild(instructionText);
            instructionText = null;
        }
        
        if (backgroundRectangle != null) {
            app.getGuiNode().detachChild(backgroundRectangle);
            backgroundRectangle = null;
        }
    }
    
    /**
     * Met à jour le clignotement du texte
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    private void updateBlinkingText(float tpf) {
        // Cacher le texte si le menu ChooseMenu est ouvert
        if (worldSelectionMenu.isMenuVisible()) {
            if (instructionText != null) {
                hideInstructionText();
            }
            return;
        }
        
        if (instructionText != null) {
            blinkTimer += tpf;
            
            // Cycle total de 2.0 secondes (1.5 visible + 0.5 invisible)
            float cycleTime = 2.0f;
            float timeInCycle = blinkTimer % cycleTime;
            
            // Le texte est visible pendant les 1.5 premières secondes du cycle
            boolean shouldBeVisible = timeInCycle < 1.5f;
            
            // Mettre à jour la visibilité seulement si elle a changé
            if (textVisible != shouldBeVisible) {
                textVisible = shouldBeVisible;
                
                // Appliquer le clignotement au texte
                instructionText.setCullHint(textVisible ? 
                    com.jme3.scene.Spatial.CullHint.Never : 
                    com.jme3.scene.Spatial.CullHint.Always);
                
                // Appliquer le clignotement à l'arrière-plan
                if (backgroundRectangle != null) {
                    backgroundRectangle.setCullHint(textVisible ? 
                        com.jme3.scene.Spatial.CullHint.Never : 
                        com.jme3.scene.Spatial.CullHint.Always);
                }
            }
        }
    }
} 