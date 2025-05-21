package voxel.view.hud;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 * Écran de chargement affiché lors de la génération d'un nouveau monde.
 */
public class LoadingScreen extends AbstractGameMenu {
    
    /** Texte pour afficher la progression du chargement */
    private BitmapText loadingText;
    
    /** Progression actuelle (de 0 à 100) */
    private float progress = 0;
    
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
        
        // Initialiser le texte de chargement pour l'interface JME standard
        loadingText = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
        loadingText.setSize(32);
        loadingText.setColor(ColorRGBA.White);
        loadingText.setText("Chargement en cours...");
        
        // Centrer le texte
        float textWidth = loadingText.getLineWidth();
        float screenWidth = app.getContext().getSettings().getWidth();
        float screenHeight = app.getContext().getSettings().getHeight();
        loadingText.setLocalTranslation((screenWidth - textWidth) / 2, screenHeight / 2, 0);
        
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
                    
                    panel(new PanelBuilder("progressPanel") {{
                        childLayoutVertical();
                        alignCenter();
                        width("80%");
                        height("40%");
                        backgroundColor("#000a");
                        
                        control(new LabelBuilder("progressLabel", "Veuillez patienter...") {{
                            alignCenter();
                            valignCenter();
                            width("100%");
                            height("100%");
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
        
        // Ajoutez le texte de chargement à l'interface JME standard
        app.getGuiNode().attachChild(loadingText);
        progress = 0;
    }
    
    /**
     * Masque l'écran de chargement
     */
    @Override
    public void hideMenu() {
        super.hideMenu();
        
        // Retirez le texte de chargement de l'interface JME standard
        app.getGuiNode().detachChild(loadingText);
    }
    
    /**
     * Met à jour la progression du chargement
     * 
     * @param progress La progression (de 0 à 100)
     */
    public void setProgress(float progress) {
        this.progress = progress;
        loadingText.setText("Chargement : " + (int)progress + "%");
        
        // Mettre à jour l'interface Nifty si elle est visible
        if (menuVisible) {
            try {
                // Trouver l'élément du label et mettre à jour son texte
                Element progressLabel = nifty.getCurrentScreen().findElementById("progressLabel");
                if (progressLabel != null) {
                    progressLabel.getRenderer(TextRenderer.class)
                        .setText("Chargement : " + (int)progress + "%");
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour du texte: " + e.getMessage());
            }
        }
    }
} 