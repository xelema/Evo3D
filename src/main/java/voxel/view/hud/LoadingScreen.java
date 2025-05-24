package voxel.view.hud;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.tools.SizeValue;

/**
 * Écran de chargement affiché lors de la génération d'un nouveau monde.
 */
public class LoadingScreen extends AbstractGameMenu {
    
    /** Progression actuelle (de 0 à 100) */
    private float progress = 0;
    
    /** ID du panneau de progression */
    private static final String PROGRESS_FILL_ID = "progressFill";
    
    /** ID du texte de progression */
    private static final String PROGRESS_TEXT_ID = "progressText";
    
    /**
     * Constructeur de l'écran de chargement
     * 
     * @param app L'application SimpleApplication
     */
    public LoadingScreen(SimpleApplication app) {
        super(app);
    }
    
    /**
     * Initialise l'écran de chargement
     */
    @Override
    public void initialize() {
        // Créer le système d'affichage Nifty lié à JME
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        createLoadingScreen();
        
        // Initialiser le menu mais ne pas l'afficher immédiatement
        nifty.gotoScreen("loading");
        hideMenu();
    }
    
    /**
     * Crée l'interface pour l'écran de chargement
     */
    private void createLoadingScreen() {
        nifty.addScreen("loading", new ScreenBuilder("loading") {{
            controller(LoadingScreen.this);
            
            layer(new LayerBuilder("background") {{
                childLayoutCenter();
                backgroundColor("#000c");
                
                panel(new PanelBuilder("loadingPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("25%");
                    backgroundColor("#222a");
                    
                    text(new TextBuilder() {{
                        text("Génération du monde en cours...");
                        font("Interface/Fonts/Default.fnt");
                        height("30%");
                        width("100%");
                        alignCenter();
                        color("#fff");
                    }});
                    
                    // Conteneur pour la barre de progression
                    panel(new PanelBuilder("progressContainer") {{
                        childLayoutVertical();
                        alignCenter();
                        width("80%");
                        height("40%");
                        backgroundColor("#222a");
                        
                        // Panneau pour afficher le fond de la barre de progression
                        panel(new PanelBuilder("progressBackground") {{
                            childLayoutHorizontal();
                            alignCenter();
                            valignCenter();
                            width("100%");
                            height("30px");
                            backgroundColor("#000a");
                            paddingLeft("2px");
                            paddingRight("2px");
                            paddingTop("2px");
                            paddingBottom("2px");
                            
                            // Panneau de remplissage de la barre de progression
                            panel(new PanelBuilder(PROGRESS_FILL_ID) {{
                                childLayoutHorizontal();
                                width("0%"); // Commencer à 0%
                                height("100%");
                                backgroundColor("#1E90FFaa"); // Bleu avec transparence
                            }});
                        }});
                        
                        // Texte pour afficher le pourcentage
                        text(new TextBuilder(PROGRESS_TEXT_ID) {{
                            text("Chargement: 0%");
                            font("Interface/Fonts/Default.fnt");
                            height("20px");
                            width("100%");
                            alignCenter();
                            valignCenter();
                            color("#fff");
                        }});
                    }});
                    
                    text(new TextBuilder() {{
                        text("Cette opération peut prendre quelques instants");
                        font("Interface/Fonts/Default.fnt");
                        height("30%");
                        width("100%");
                        alignCenter();
                        color("#aaa");
                    }});
                }});
            }});
        }}.build(nifty));
    }
    
    /**
     * Affiche l'écran de chargement
     */
    @Override
    public void showMenu() {
        super.showMenu();
        progress = 0;
        updateProgressDisplay();
    }
    
    /**
     * Masque l'écran de chargement
     */
    @Override
    public void hideMenu() {
        super.hideMenu();
    }
    
    /**
     * Met à jour la progression du chargement
     * 
     * @param progress La progression (de 0 à 100)
     */
    public void setProgress(float progress) {
        this.progress = progress;
        
        // Mettre à jour l'interface Nifty si elle est visible
        if (menuVisible) {
            updateProgressDisplay();
        }
    }
    
    /**
     * Met à jour l'affichage de la progression
     */
    private void updateProgressDisplay() {
        try {
            // Mettre à jour la barre de progression visuelle
            Element progressFill = nifty.getCurrentScreen().findElementById(PROGRESS_FILL_ID);
            if (progressFill != null) {
                progressFill.setConstraintWidth(new SizeValue(progress + "%"));
                progressFill.getParent().layoutElements();
            }
            
            // Mettre à jour le texte de progression
            Element progressText = nifty.getCurrentScreen().findElementById(PROGRESS_TEXT_ID);
            if (progressText != null) {
                progressText.getRenderer(TextRenderer.class).setText("Chargement: " + (int)progress + "%");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour de la progression: " + e.getMessage());
        }
    }
} 