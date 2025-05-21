package voxel.view;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MenuPrincipalApp implements ScreenController {

    private static final int NOMBRECASESMAX = 6;
    private static final int NOMBREPARAMETRES = 3;
    private  Node[][] croix = new Node[NOMBREPARAMETRES][NOMBRECASESMAX];
    private Geometry[][] carres = new Geometry[NOMBREPARAMETRES][NOMBRECASESMAX];
    private boolean[][] coche = new boolean[NOMBREPARAMETRES][NOMBRECASESMAX];
    private BitmapText[][] textes = new BitmapText[NOMBREPARAMETRES][NOMBRECASESMAX];
    private BitmapText[] titres = new BitmapText[NOMBREPARAMETRES];
    private Nifty nifty;
    private NiftyJmeDisplay niftyDisplay;
    private boolean menuVisible = false;
    private SimpleApplication app;

    /**
     * Initialise le menu dans l'application fournie
     * @param app L'application dans laquelle intégrer le menu
     */
    public void initialize(SimpleApplication app) {
        this.app = app;
        // Créer le système d'affichage Nifty lié à JME
        niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(
                app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());

        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        int tailleEcran = app.getContext().getSettings().getHeight();
        createMenu(tailleEcran);

        // Initialiser le menu mais ne pas l'afficher immédiatement
        nifty.gotoScreen("start");
        app.getInputManager().setCursorVisible(false);
        
        // Ne pas attacher le processeur au début pour qu'il soit invisible
        hideMenu();
    }

    /**
     * Bascule la visibilité du menu
     */
    public void toggleMenuVisibility() {
        if (menuVisible) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    /**
     * Affiche le menu
     */
    public void showMenu() {
        if (!menuVisible) {
            app.getInputManager().setCursorVisible(true);
            app.getGuiViewPort().addProcessor(niftyDisplay);
            menuVisible = true;
        }
    }

    /**
     * Cache le menu
     */
    public void hideMenu() {
        if (menuVisible || app.getGuiViewPort().getProcessors().contains(niftyDisplay)) {
            app.getInputManager().setCursorVisible(false);
            app.getGuiViewPort().removeProcessor(niftyDisplay);
            menuVisible = false;
        }
    }

    public void createMenu(int tailleEcran) {

        String police;

        if (tailleEcran < 800) {
            police = "Interface/Fonts/Arial16.fnt";
        } else if (tailleEcran < 1080) {
            police = "Interface/Fonts/Arial32.fnt";
        } else {
            police = "Interface/Fonts/Arial48.fnt";
        }
        nifty.addScreen("start", new ScreenBuilder("start") {{

            controller(MenuPrincipalApp.this); // liaison au ScreenController


            layer(new LayerBuilder("layer") {{

                childLayoutCenter();


                panel(new PanelBuilder("panel") {{

                    childLayoutVertical();

                    alignCenter();

                    valignCenter();

                    width("50%");

                    height("50%");


                    text(new TextBuilder() {{

                        text("Menu Principal");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();

                    }});


                    control(new ButtonBuilder("demarrerLeJeu", "Demarrer le jeu") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("startGame()");

                    }});


                    control(new ButtonBuilder("optionsButton", "Options") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("reglageFaune()");

                    }});


                    control(new ButtonBuilder("quitButton", "Quitter") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("quitGame()");

                    }});

                }});
                System.out.println("Menu construit !");
            }});

        }}.build(nifty));

    }


    public void fenetreReglage() {

        nifty.addScreen("reglage", new ScreenBuilder("reglage") {{

            controller(MenuPrincipalApp.this); // liaison au ScreenController


            layer(new LayerBuilder("layerReglage") {{

                childLayoutCenter();


                panel(new PanelBuilder("panelReglage") {{

                    childLayoutVertical();

                    alignCenter();

                    valignCenter();

                    width("50%");

                    height("50%");


                    text(new TextBuilder() {{

                        text("Reglage de l'environnement");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();

                    }});


                    control(new ButtonBuilder("faune", "Faune") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("reglageFaune()");

                    }});


                    control(new ButtonBuilder("flore", "Flore") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("reglageFlore");

                    }});


                    control(new ButtonBuilder("conditionEnvironnement", "Condition de l' environnement") {{

                        alignCenter();

                        height("20%");

                        width("60%");

                        interactOnClick("reglageEnvironnement()");

                    }});

                }});
                System.out.println("Menu construit !");
            }});

        }}.build(nifty));

    }
    
    public void formationCase(String nomParametre, int nombreCases, int parametre) {
        int largeurEcran = app.getContext().getSettings().getWidth();
        int hauteurEcran = app.getContext().getSettings().getHeight();
        float largeurCase = 30;
        
        // Get the GUI font
        BitmapText tempText = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
        float charSize = tempText.getFont().getCharSet().getRenderedSize();
        
        // Taille du texte
        float tailleTexte = charSize * 0.8f;
        float espaceParBloc = charSize + hauteurEcran * 0.3f + tailleTexte;
        // Position de x et de y dynamique.
        float ytitre = hauteurEcran - charSize - parametre * espaceParBloc;
        BitmapText titre = new BitmapText(tempText.getFont(), false);
        float policeTexte = Math.max(18, hauteurEcran * 0.025f);
        titre.setSize(policeTexte);
        titre.setText(nomParametre);
        float ycases = ytitre - charSize - hauteurEcran* 0.15f;
        float ytexte = ycases - hauteurEcran * 0.025f;
        float yDebut = hauteurEcran * 0.75f ;//375
        float yEcart = hauteurEcran * 0.35f;
        float decalageDroite = largeurEcran * 0.1f;
        titres[parametre] = titre;
        float yinitial = yDebut - parametre* yEcart;
        float largeurDispo = largeurEcran * 0.8f;
        float espaceEntreCases = largeurEcran * 0.03f;
        float xinitial = (largeurEcran - largeurDispo) / 2 + decalageDroite;
        titre.setLocalTranslation(xinitial, ytitre, 0);
        app.getGuiNode().attachChild(titre);
        String[] quantifieurs = {"aucun", "peu", "moyen", "beaucoup", "max"};
        for (int i = 0; i < nombreCases; i++) {
            Quad caseCoche = new Quad(largeurCase, largeurCase);
            Geometry geometry = new Geometry("Case" + i, caseCoche);
            Material contour = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
            contour.setColor("Color", ColorRGBA.White);
            geometry.setMaterial(contour); 
            float x = xinitial + i * (largeurCase + espaceEntreCases);
            geometry.setLocalTranslation(x, ycases, 0);
            app.getGuiNode().attachChild(geometry);
            BitmapText choix = new BitmapText(tempText.getFont(), false);
            choix.setSize(policeTexte);
            choix.setText(quantifieurs[i]);
            float texteX = x + (largeurCase / 2f) - (choix.getLineWidth() / 2f);
            float texteY = ycases - 15;
            choix.setLocalTranslation(texteX, texteY, 0);
            app.getGuiNode().attachChild(choix);

            textes[parametre][i] = choix;
            carres[parametre][i] = geometry;
            croix[parametre][i] = null;
        }
    }

    private void nettoyerCases() {

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < NOMBREPARAMETRES; j++) {
                if (carres[j][i] != null) {
    
                    app.getGuiNode().detachChild(carres[j][i]);
    
                    carres[j][i] = null;
    
                }
    
                if (croix[j][i] != null) {
    
                    app.getGuiNode().detachChild(croix[j][i]);
    
                    croix[j][i] = null;
                }

                if (textes[j][i] != null) {
    
                    app.getGuiNode().detachChild(textes[j][i]);
    
                    textes[j][i] = null;
                }
                if (titres[j] != null) {
                    app.getGuiNode().detachChild(titres[j]);
                    titres[j] = null;
                }
            }
    
        }
    
    }

    private void decocherCase(int parametre, int colonne) {
        if (croix[parametre][colonne] != null) {
            app.getGuiNode().detachChild(croix[parametre][colonne]);
        }
        croix[parametre][colonne] = null;
        coche[parametre][colonne] = false;
    }

    public void cliqueCase(int parametre) {
        app.getInputManager().addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(new ActionListener() {
            @Override 
            public void onAction(String Name, boolean clique, float tpf) {
                if (!clique) return;
                Vector2f souris = app.getInputManager().getCursorPosition();
                System.out.println("Position souris: " + souris.x + ", " + souris.y);
                
                for (int i = 0; i < 6; i++) {
                    if (carres[parametre][i] == null) continue;
                    Geometry carre = carres[parametre][i];
                    Vector3f position = carre.getLocalTranslation();
                    float x = position.x;
                    float y = position.y;
                    
                    System.out.println("Case " + i + ": " + x + ", " + y);

                    float largeurCase = 30;
                    boolean dansX = souris.x >= x && souris.x <= x + largeurCase;
                    boolean dansY = souris.y >= y && souris.y <= y + largeurCase;

                    if (dansX && dansY) {
                        System.out.println("Case " + i + " cliquée!");
                        cocherCase(i, x, y, parametre);
                        coche[parametre][i] = true;
                        for (int caseCoche = 0; caseCoche < 6; caseCoche++) {
                            if (caseCoche == i) {
                                continue;
                            }
                            if (coche[parametre][caseCoche] == true) {
                                decocherCase(parametre, caseCoche);
                            }
                        }
                    }
                }
            }
        }, "Click");
    }

    private void cocherCase(int index, float x, float y, int parametre) {

        if (croix[parametre][index] != null) return; // déjà une croix ici



        Node croixNode = new Node("Croix" + index);



        // Trait horizontal

        Quad traitHorizontal = new Quad(30, 5);

        // Trait vertical

        Quad traitVertical = new Quad(5, 30);

        Geometry geometrieHorizontal = new Geometry("BarreH", traitHorizontal);

        Geometry geometrieVerticale = new Geometry("BarreV", traitVertical);

        Material materialHorizontal = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        Material materielVerticale = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        materialHorizontal.setColor("Color", ColorRGBA.Red);

        materielVerticale.setColor("Color", ColorRGBA.Red);

        geometrieHorizontal.setMaterial(materialHorizontal);

        geometrieVerticale.setMaterial(materielVerticale);

        geometrieHorizontal.setLocalTranslation(-15f, -2.5f, 0); // centré horizontalement

        geometrieVerticale.setLocalTranslation(-2.5f, -15f, 0); // centré verticalement



        croixNode.attachChild(geometrieHorizontal);

        croixNode.attachChild(geometrieVerticale);



        // Positionner la croix dans la case

        croixNode.setLocalTranslation(x + 15f, y + 15f, 2); // couche au-dessus

        app.getGuiNode().attachChild(croixNode);



        croix[parametre][index] = croixNode;

    

    }
    public void modifierFaune() {

        
        nifty.addScreen("faune", new ScreenBuilder("faune") {{

            controller(MenuPrincipalApp.this); // liaison au ScreenController
           
           
            layer(new LayerBuilder("Modifications") {{

                childLayoutVertical();


                panel(new PanelBuilder("panelReglage") {{

                    childLayoutVertical();

                    width("50%");

                    height("50%");
                    paddingTop("2%");
                    formationCase("Reglage de la température",5,0);
                    cliqueCase(0);
                    text(new TextBuilder() {{

                       // text("Reglage de la température.");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();
                        //valignTop();

                    }});

                    panel (new PanelBuilder() {{
                        height("40%");
                    }});

                    formationCase("reglage de l'humidité",5,1);
                    cliqueCase(1);
                    text(new TextBuilder() {{

                     //   text("Reglage de l'humidité.");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();
                        //valignTop();

                    }});
                    panel (new PanelBuilder() {{
                        height("40%");
                    }});

                    formationCase("reglage des reliefs",5,2);
                    cliqueCase(2);
                    text(new TextBuilder() {{

                       // text("Reglage des reliefs");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();
                        //valignTop();

                    }});
                    layer(new LayerBuilder("layerBas") {{
                        childLayoutHorizontal();
                        valignBottom();
                        width("100%");
                        height("10%");
                        panel (new PanelBuilder() {{
                            childLayoutHorizontal();

                            valignBottom();
                            height("10%");
                            width("100%");
                   
                            control(new ButtonBuilder("Retour", "Retour") {{

                                alignLeft();

                                valignBottom();

                                height("80%");

                                width("15%");

                                interactOnClick("retour()");

                            }});
                        }});
                    }});
                }});
            }});

        }}.build(nifty));

    }

    // Méthodes déclenchées par les boutons

    public void startGame() {

        System.out.println("Lancement du jeu...");
        System.out.println("Bouton démarrer cliqué !");
        // Tu peux ici changer d'écran ou démarrer la logique du jeu

    }


    public void openOptions() {

        System.out.println("Ouverture des options...");
        fenetreReglage();
        nifty.gotoScreen("reglage");
        // Ici tu peux basculer vers un écran "options"

    }

    public void retour() {

        System.out.println("Retour...");
        fenetreReglage();
        nettoyerCases();
        nettoyerCases();
        nettoyerCases();
        nifty.gotoScreen("start");
        // Ici tu peux basculer vers un écran "options"

    }


    public void quitGame() {

        System.out.println("Fermeture du jeu...");

        app.stop(); // Ferme l'application JME

    }

    public void reglageFaune() {
        System.out.println("Modification des paramètres de la faune...");
        modifierFaune();
        nifty.gotoScreen("faune");
    }

    public Nifty getNifty() {
        return this.nifty;
    }

    // Méthodes du ScreenController

    public void bind(Nifty nifty, Screen screen) {}


    public void onStartScreen() {}


    public void onEndScreen() {}

}
