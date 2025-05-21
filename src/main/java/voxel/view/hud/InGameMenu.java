package voxel.view.hud;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.slider.builder.SliderBuilder;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.Slider;

import voxel.controller.GameStateManager;
import voxel.model.BiomeType;

/**
 * Menu disponible pendant le jeu pour contrôler les paramètres en temps réel.
 */
public class InGameMenu extends AbstractGameMenu {
    
    /** Vitesse de base du temps dans le jeu */
    private static final float DEFAULT_TIME_SPEED = 1.0f;
    
    /** Vitesse actuelle du temps dans le jeu */
    private float timeSpeed = DEFAULT_TIME_SPEED;
    
    /** Référence au contrôleur du jeu pour effectuer les actions */
    private GameStateManager stateManager;
    
    /**
     * Constructeur du menu en jeu
     * 
     * @param app L'application SimpleApplication
     * @param stateManager Le gestionnaire d'états du jeu
     */
    public InGameMenu(SimpleApplication app, GameStateManager stateManager) {
        super(app);
        this.stateManager = stateManager;
    }
    
    /**
     * Initialise le menu et ses composants
     */
    @Override
    public void initialize() {
        // Créer le système d'affichage Nifty lié à JME
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        createMenu();

        // Initialiser le menu mais ne pas l'afficher immédiatement
        nifty.gotoScreen("inGameMenu");
        hideMenu();
    }
    
    /**
     * Crée l'interface du menu en jeu
     */
    private void createMenu() {
        nifty.addScreen("inGameMenu", new ScreenBuilder("inGameMenu") {{
            controller(InGameMenu.this);
            
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
                backgroundColor("#000a");
                
                panel(new PanelBuilder("mainPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("50%");
                    height("70%");
                    backgroundColor("#444f");
                    padding("20px");
                    
                    text(new TextBuilder() {{
                        text("Menu de Jeu");
                        font("Interface/Fonts/Default.fnt");
                        height("10%");
                        width("100%");
                        alignCenter();
                        color("#fff");
                    }});
                    
                    panel(new PanelBuilder("timeControlPanel") {{
                        childLayoutHorizontal();
                        alignCenter();
                        height("10%");
                        width("100%");
                        padding("10px");
                        
                        text(new TextBuilder() {{
                            text("Vitesse du temps:");
                            font("Interface/Fonts/Default.fnt");
                            width("35%");
                            height("100%");
                            valignCenter();
                            color("#fff");
                        }});
                        
                        control(new SliderBuilder("timeSpeedSlider", true) {{
                            alignRight();
                            valignCenter();
                            width("65%");
                            height("20px");
                            initial(1.0f);
                            min(0.1f);
                            max(5.0f);
                            stepSize(0.1f);
                            buttonStepSize(0.5f);
                        }});
                    }});
                    
                    panel(new PanelBuilder("displayPanel") {{
                        childLayoutVertical();
                        alignCenter();
                        height("15%");
                        width("100%");
                        padding("10px");
                        
                        panel(new PanelBuilder("wireframePanel") {{
                            childLayoutHorizontal();
                            alignCenter();
                            height("50%");
                            width("100%");
                            
                            control(new ButtonBuilder("wireframeButton", "Mode filaire") {{
                                alignCenter();
                                valignCenter();
                                height("100%");
                                width("100%");
                                interactOnClick("toggleWireframe()");
                            }});
                        }});
                        
                        panel(new PanelBuilder("lightningPanel") {{
                            childLayoutHorizontal();
                            alignCenter();
                            height("50%");
                            width("100%");
                            
                            control(new ButtonBuilder("lightningButton", "Éclairage") {{
                                alignCenter();
                                valignCenter();
                                height("100%");
                                width("100%");
                                interactOnClick("toggleLightning()");
                            }});
                        }});
                    }});
                    
                    panel(new PanelBuilder("spacer") {{
                        height("5%");
                    }});
                    
                    control(new ButtonBuilder("worldSelectionButton", "Changer de monde") {{
                        alignCenter();
                        height("10%");
                        width("80%");
                        interactOnClick("openBiomeSelection()");
                    }});
                    
                    panel(new PanelBuilder("spacer2") {{
                        height("5%");
                    }});
                    
                    control(new ButtonBuilder("controlsButton", "Contrôles") {{
                        alignCenter();
                        height("10%");
                        width("80%");
                        interactOnClick("showControls()");
                    }});
                    
                    panel(new PanelBuilder("spacer3") {{
                        height("5%");
                    }});
                    
                    control(new ButtonBuilder("resumeButton", "Reprendre le jeu") {{
                        alignCenter();
                        height("10%");
                        width("80%");
                        interactOnClick("resumeGame()");
                    }});
                    
                    panel(new PanelBuilder("spacer4") {{
                        height("5%");
                    }});
                    
                    control(new ButtonBuilder("quitButton", "Quitter") {{
                        alignCenter();
                        height("10%");
                        width("80%");
                        interactOnClick("quitGame()");
                    }});
                }});
            }});
        }}.build(nifty));
        
        // Écran de contrôles
        nifty.addScreen("controlsScreen", new ScreenBuilder("controlsScreen") {{
            controller(InGameMenu.this);
            
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
                backgroundColor("#000a");
                
                panel(new PanelBuilder("controlsPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("80%");
                    backgroundColor("#444f");
                    padding("20px");
                    
                    text(new TextBuilder() {{
                        text("Contrôles du jeu");
                        font("Interface/Fonts/Default.fnt");
                        height("10%");
                        width("100%");
                        alignCenter();
                        color("#fff");
                    }});
                    
                    panel(new PanelBuilder("controlsList") {{
                        childLayoutVertical();
                        alignCenter();
                        height("75%");
                        width("90%");
                        
                        text(new TextBuilder() {{
                            text("Z, Q, S, D : Déplacement\n"
                                + "Espace : Saut\n"
                                + "E : Interaction\n"
                                + "Souris : Orientation\n"
                                + "Clic gauche : Détruire un bloc\n"
                                + "Clic droit : Placer un bloc\n"
                                + "ESC : Menu en jeu\n"
                                + "F : Mode plein écran\n"
                                + "L : Mode filaire\n"
                                + "T : Mode d'éclairage");
                            font("Interface/Fonts/Default.fnt");
                            height("100%");
                            width("100%");
                            color("#fff");
                        }});
                    }});
                    
                    control(new ButtonBuilder("backButton", "Retour") {{
                        alignCenter();
                        height("10%");
                        width("40%");
                        interactOnClick("backToMenu()");
                    }});
                }});
            }});
        }}.build(nifty));
        
        // Écran de sélection de biome
        nifty.addScreen("biomeSelectionScreen", new ScreenBuilder("biomeSelectionScreen") {{
            controller(InGameMenu.this);
            
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
                backgroundColor("#000a");
                
                panel(new PanelBuilder("biomePanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("80%");
                    backgroundColor("#444f");
                    padding("20px");
                    
                    text(new TextBuilder() {{
                        text("Sélection du biome");
                        font("Interface/Fonts/Default.fnt");
                        height("10%");
                        width("100%");
                        alignCenter();
                        color("#fff");
                    }});
                    
                    panel(new PanelBuilder("biomeButtonsPanel") {{
                        childLayoutVertical();
                        alignCenter();
                        height("75%");
                        width("90%");
                        
                        control(new ButtonBuilder("savannaButton", "Savane") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(SAVANNA)");
                        }});
                        
                        panel(new PanelBuilder("spacer5") {{
                            height("2%");
                        }});
                        
                        control(new ButtonBuilder("desertButton", "Désert") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(DESERT)");
                        }});
                        
                        panel(new PanelBuilder("spacer6") {{
                            height("2%");
                        }});
                        
                        control(new ButtonBuilder("mountainsButton", "Montagnes") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(MOUNTAINS)");
                        }});
                        
                        panel(new PanelBuilder("spacer7") {{
                            height("2%");
                        }});
                        
                        control(new ButtonBuilder("plainsButton", "Plaines") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(PLAINS)");
                        }});
                        
                        panel(new PanelBuilder("spacer8") {{
                            height("2%");
                        }});
                        
                        control(new ButtonBuilder("jungleButton", "Jungle") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(JUNGLE)");
                        }});
                        
                        panel(new PanelBuilder("spacer9") {{
                            height("2%");
                        }});
                        
                        control(new ButtonBuilder("snowyButton", "Neige") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(SNOWY)");
                        }});
                    }});
                    
                    control(new ButtonBuilder("backToMenuButton", "Retour") {{
                        alignCenter();
                        height("10%");
                        width("40%");
                        interactOnClick("backToMenu()");
                    }});
                }});
            }});
        }}.build(nifty));
    }
    
    /**
     * Effectue les actions nécessaires lorsque le menu est affiché
     */
    @Override
    public void showMenu() {
        super.showMenu();
        
        // Mettre à jour l'état des contrôles si nécessaire
        if (stateManager != null && stateManager.getWorldModel() != null) {
            updateControls();
        }
    }
    
    /**
     * Met à jour l'état des contrôles du menu
     */
    private void updateControls() {
        // Mettre à jour le slider de vitesse
        Slider timeSlider = nifty.getCurrentScreen().findNiftyControl("timeSpeedSlider", Slider.class);
        if (timeSlider != null) {
            timeSlider.setValue(timeSpeed);
        }
        
        // Mettre à jour les checkboxes
        CheckBox wireframeBox = nifty.getCurrentScreen().findNiftyControl("wireframeCheckbox", CheckBox.class);
        CheckBox lightningBox = nifty.getCurrentScreen().findNiftyControl("lightningCheckbox", CheckBox.class);
        
        if (wireframeBox != null && lightningBox != null && stateManager.getWorldModel() != null) {
            wireframeBox.setChecked(stateManager.getWorldModel().getWireframeMode());
            lightningBox.setChecked(stateManager.getWorldModel().getLightningMode());
        }
    }
    
    /**
     * Définit la vitesse du temps dans le jeu
     * 
     * @param speed Nouvelle vitesse du temps
     */
    public void setTimeSpeed(float speed) {
        this.timeSpeed = speed;
        // Notifier le gestionnaire d'état
        if (stateManager != null) {
            stateManager.setTimeSpeed(speed);
        }
    }
    
    /**
     * Récupère la vitesse actuelle du temps
     * 
     * @return La vitesse actuelle du temps
     */
    public float getTimeSpeed() {
        return timeSpeed;
    }
    
    // Méthodes appelées par les boutons
    
    public void openWorldSelection() {
        if (stateManager != null) {
            hideMenu();
            stateManager.changeState(GameStateManager.GameState.WORLD_SELECTION);
        }
    }
    
    public void showControls() {
        nifty.gotoScreen("controlsScreen");
    }
    
    public void backToMenu() {
        nifty.gotoScreen("inGameMenu");
    }
    
    public void resumeGame() {
        stateManager.changeState(GameStateManager.GameState.IN_GAME);
        hideMenu();
    }
    
    public void quitGame() {
        app.stop();
    }
    
    /**
     * Bascule l'état du mode filaire
     */
    public void toggleWireframe() {
        stateManager.getWorldController().toggleWireframe();
    }
    
    /**
     * Bascule l'état de l'éclairage
     */
    public void toggleLightning() {
        stateManager.getWorldController().toggleLightning();
    }
    
    /**
     * Ouvre l'écran de sélection de biome
     */
    public void openBiomeSelection() {
        nifty.gotoScreen("biomeSelectionScreen");
    }
    
    /**
     * Change le biome actuel pour un nouveau monde
     * 
     * @param biomeType Le type de biome à générer
     */
    public void changeBiome(String biomeType) {
        try {
            BiomeType biome = BiomeType.valueOf(biomeType);
            if (stateManager != null) {
                hideMenu();
                stateManager.changeWorld(biome);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Type de biome invalide: " + biomeType);
        }
    }
} 