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
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.NiftyControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.Nifty;

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
    
    // Garder en mémoire l'état des modes
    private boolean wireframeMode = false;
    private boolean lightningMode = false;
    
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
        
        // Enregistrer les écouteurs
        nifty.subscribeAnnotations(this);
    }
    
    @NiftyEventSubscriber(id="timeSpeedSlider")
    public void onTimeSpeedSliderChanged(final String id, final SliderChangedEvent event) {
        // Mettre à jour la vitesse du temps lorsque le slider change
        float value = event.getValue();
        setTimeSpeed(value);
        System.out.println("Vitesse du temps modifiée: " + value);
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
                width("100%");
                height("100%");
                
                panel(new PanelBuilder("mainPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("50%");  // Pourcentage de la largeur de l'écran
                    height("80%"); // Pourcentage de la hauteur de l'écran
                    backgroundColor("#444f");
                    paddingLeft("2%");
                    paddingRight("2%");
                    paddingTop("2%");
                    paddingBottom("2%");
                    
                    text(new TextBuilder() {{
                        text("Menu de Jeu");
                        font("Interface/Fonts/Default.fnt");
                        height("8%");
                        width("100%");
                        alignCenter();
                        color("#fff");
                    }});
                    
                    panel(new PanelBuilder("timeControlPanel") {{
                        childLayoutHorizontal();
                        alignCenter();
                        height("8%");
                        width("100%");
                        paddingLeft("2%");
                        paddingRight("2%");
                        
                        text(new TextBuilder() {{
                            text("Vitesse du temps:");
                            font("Interface/Fonts/Default.fnt");
                            width("35%");
                            height("100%");
                            valignCenter();
                            color("#fff");
                        }});
                        
                        control(new SliderBuilder("timeSpeedSlider", false) {{
                            alignRight();
                            valignCenter();
                            width("65%");
                            height("50%");
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
                        height("16%");
                        width("100%");
                        paddingLeft("2%");
                        paddingRight("2%");
                        
                        panel(new PanelBuilder("wireframePanel") {{
                            childLayoutHorizontal();
                            alignCenter();
                            height("45%");
                            width("100%");
                            marginBottom("2%");
                            
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
                            height("45%");
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
                        height("3%");
                    }});
                    
                    control(new ButtonBuilder("worldSelectionButton", "Changer de monde") {{
                        alignCenter();
                        height("8%");
                        width("80%");
                        interactOnClick("openBiomeSelection()");
                    }});
                    
                    panel(new PanelBuilder("spacer2") {{
                        height("3%");
                    }});
                    
                    control(new ButtonBuilder("controlsButton", "Contrôles") {{
                        alignCenter();
                        height("8%");
                        width("80%");
                        interactOnClick("showControls()");
                    }});
                    
                    panel(new PanelBuilder("spacer3") {{
                        height("3%");
                    }});
                    
                    control(new ButtonBuilder("resumeButton", "Reprendre le jeu") {{
                        alignCenter();
                        height("8%");
                        width("80%");
                        interactOnClick("resumeGame()");
                    }});
                    
                    panel(new PanelBuilder("spacer4") {{
                        height("3%");
                    }});
                    
                    control(new ButtonBuilder("quitButton", "Quitter") {{
                        alignCenter();
                        height("8%");
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
                width("100%");
                height("100%");
                
                panel(new PanelBuilder("controlsPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("80%");
                    backgroundColor("#444f");
                    paddingLeft("2%");
                    paddingRight("2%");
                    paddingTop("2%");
                    paddingBottom("2%");
                    
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
                width("100%");
                height("100%");
                
                panel(new PanelBuilder("biomePanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("80%");
                    backgroundColor("#444f");
                    paddingLeft("2%");
                    paddingRight("2%");
                    paddingTop("2%");
                    paddingBottom("2%");
                    
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
                            marginBottom("2%");
                        }});
                        
                        control(new ButtonBuilder("desertButton", "Désert") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(DESERT)");
                            marginBottom("2%");
                        }});
                        
                        control(new ButtonBuilder("mountainsButton", "Montagnes") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(MOUNTAINS)");
                            marginBottom("2%");
                        }});
                        
                        control(new ButtonBuilder("plainsButton", "Plaines") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(PLAINS)");
                            marginBottom("2%");
                        }});
                        
                        control(new ButtonBuilder("jungleButton", "Jungle") {{
                            alignCenter();
                            height("12%");
                            width("80%");
                            interactOnClick("changeBiome(JUNGLE)");
                            marginBottom("2%");
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
        try {
            super.showMenu();
            
            // S'assurer que nous sommes sur le bon écran
            if (nifty.getCurrentScreen() == null || !"inGameMenu".equals(nifty.getCurrentScreen().getScreenId())) {
                nifty.gotoScreen("inGameMenu");
            }
            
            // Mettre à jour l'état des contrôles si nécessaire
            if (stateManager != null && stateManager.getWorldModel() != null) {
                updateControls();
            }
            
            // Réinitialiser l'état des boutons pour qu'ils ne soient pas sélectionnés
            resetButtonStates();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage du menu en jeu: " + e.getMessage());
        }
    }
    
    /**
     * Réinitialise l'état des boutons standards pour qu'ils ne soient pas sélectionnés
     */
    private void resetButtonStates() {
        if (nifty.getCurrentScreen() == null) {
            return;
        }
        
        // Liste des boutons standards qui ne doivent jamais être sélectionnés
        String[] standardButtons = {
            "worldSelectionButton", "controlsButton", "resumeButton", "quitButton",
            "backButton", "backToMenuButton", "savannaButton", "desertButton",
            "mountainsButton", "plainsButton", "jungleButton", "snowyButton"
        };
        
        for (String buttonId : standardButtons) {
            // Récupérer l'élément du bouton
            Element element = nifty.getCurrentScreen().findElementById(buttonId);
            if (element != null) {
                // Réappliquer le style par défaut pour éviter la sélection visuelle
                element.setStyle("default");
            }
        }
        
        // Mettre à jour l'apparence des boutons spéciaux en fonction de leur état
        updateButtonAppearance("wireframeButton", wireframeMode);
        updateButtonAppearance("lightningButton", lightningMode);
    }
    
    /**
     * Met à jour l'état des contrôles du menu
     */
    private void updateControls() {
        try {
            // Seulement si on est sur l'écran principal du menu
            if (!"inGameMenu".equals(nifty.getCurrentScreen().getScreenId())) {
                return;
            }
            
            // Mettre à jour le slider de vitesse
            Slider timeSlider = nifty.getCurrentScreen().findNiftyControl("timeSpeedSlider", Slider.class);
            if (timeSlider != null) {
                timeSlider.setValue(timeSpeed);
            }
            
            // Récupérer l'état actuel des modes depuis le modèle de monde
            if (stateManager != null && stateManager.getWorldModel() != null) {
                wireframeMode = stateManager.getWorldModel().getWireframeMode();
                lightningMode = stateManager.getWorldModel().getLightningMode();
                
                // Mettre à jour l'apparence des boutons en fonction de l'état
                updateButtonAppearance("wireframeButton", wireframeMode);
                updateButtonAppearance("lightningButton", lightningMode);
            }
        } catch (Exception e) {
            // Capture les erreurs potentielles lors de la mise à jour des contrôles
            System.err.println("Erreur lors de la mise à jour des contrôles du menu: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour l'apparence d'un bouton en fonction de son état
     * 
     * @param buttonId L'identifiant du bouton à mettre à jour
     * @param active Si le mode est actif ou non
     */
    private void updateButtonAppearance(String buttonId, boolean active) {
        Button button = nifty.getCurrentScreen().findNiftyControl(buttonId, Button.class);
        Element element = nifty.getCurrentScreen().findElementById(buttonId);
        
        if (button != null && element != null) {
            if (active) {
                // Mode actif: bouton "sélectionné" (rouge)
                element.setStyle("selected");
            } else {
                // Mode inactif: bouton normal
                element.setStyle("default");
            }
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
        resetButtonStates();
    }
    
    public void backToMenu() {
        nifty.gotoScreen("inGameMenu");
        updateControls(); // Met à jour l'état des boutons de mode
        resetButtonStates(); // Réinitialise les boutons standards
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
        wireframeMode = !wireframeMode;
        updateButtonAppearance("wireframeButton", wireframeMode);
    }
    
    /**
     * Bascule l'état de l'éclairage
     */
    public void toggleLightning() {
        stateManager.getWorldController().toggleLightning();
        lightningMode = !lightningMode;
        updateButtonAppearance("lightningButton", lightningMode);
    }
    
    /**
     * Ouvre l'écran de sélection de biome
     */
    public void openBiomeSelection() {
        nifty.gotoScreen("biomeSelectionScreen");
        resetButtonStates();
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
    
    /**
     * Ajoute un écouteur pour les changements de taille d'écran
     */
    public void onEndScreen() {
        super.onEndScreen();
        // Assurer que l'interface est correctement mise à jour lors des changements de résolution
        nifty.update();
    }
    
    /**
     * Effectue les actions nécessaires avant d'afficher l'écran
     */
    public void onStartScreen() {
        super.onStartScreen();
        // Assurer que l'interface est correctement mise à jour lors des changements de résolution
        nifty.update();
    }
} 