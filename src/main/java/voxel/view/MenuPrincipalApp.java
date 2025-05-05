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

public class MenuPrincipalApp extends SimpleApplication implements ScreenController {

    private static final int NOMBRECASESMAX = 6;
    private static final int NOMBREPARAMETRES = 3;
    private  Node[][] croix = new Node[NOMBREPARAMETRES][NOMBRECASESMAX];
    private Geometry[][] carres = new Geometry[NOMBREPARAMETRES][NOMBRECASESMAX];
    private boolean[][] coche = new boolean[NOMBREPARAMETRES][NOMBRECASESMAX];
    private BitmapText[][] textes = new BitmapText[NOMBREPARAMETRES][NOMBRECASESMAX];
    private Nifty nifty;

    public static void main(String[] args) {
        MenuPrincipalApp menu = new MenuPrincipalApp();
        menu.start();
        AppSettings reglages = new AppSettings(true);
        reglages.setResolution(1280, 720);
        reglages.setTitle("ProjetTOB");
        reglages.setFullscreen(false);
        menu.setSettings(reglages);
    }


    @Override

    public void simpleInitApp() {

        // Créer le système d'affichage Nifty lié à JME

        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(

                assetManager, inputManager, audioRenderer, guiViewPort);

        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        int tailleEcran = settings.getHeight();
        createMenu(tailleEcran);


        // Afficher le menu

        nifty.gotoScreen("start");
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);

        // Attacher Nifty à la GUI de JME

        guiViewPort.addProcessor(niftyDisplay);
        inputManager.setCursorVisible(true);
        nifty.getNiftyMouse().enableMouseCursor("curseur1");
        setDisplayFps(false);
        setDisplayStatView(false);
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
    
    public void formationCase( int nombreCases, int altitude, int parametre) {
        int xinitial = 100;
        int yinitial = altitude ;//375
        int espace2case = 100;
        String[] quantifieurs = {"aucun", "peu", "moyen", "beaucoup", "max"};
        for (int i = 0; i < nombreCases; i++) {
            Quad caseCoche = new Quad(20,20);
            Geometry geometry = new Geometry("Case" + i, caseCoche);
            Material contour = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            contour.setColor("Color", ColorRGBA.White);
            geometry.setMaterial(contour); 
            float x = xinitial + i* (20 + espace2case);
            geometry.setLocalTranslation(x,yinitial,0);
            guiNode.attachChild(geometry);
            BitmapText choix = new BitmapText(guiFont, false);
            choix.setSize(guiFont.getCharSet().getRenderedSize());
            choix.setText(quantifieurs[i]);
            choix.setLocalTranslation(x, yinitial - 20, 0);
            guiNode.attachChild(choix);
            textes[parametre][i] = choix;
            carres[parametre][i] = geometry;
            croix[parametre][i] = null;
        }
    }

    private void nettoyerCases() {

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < NOMBREPARAMETRES; j++) {
                if (carres[j][i] != null) {
    
                    guiNode.detachChild(carres[j][i]);
    
                    carres[j][i] = null;
    
                }
    
                if (croix[j][i] != null) {
    
                    guiNode.detachChild(croix[j][i]);
    
                    croix[j][i] = null;
                }

                if (textes[j][i] != null) {
    
                    guiNode.detachChild(textes[j][i]);
    
                    textes[j][i] = null;
                }
            }
    
        }
    
    }

    private void decocherCase(int parametre, int colonne) {
        if (croix[parametre][colonne] != null) {
            guiNode.detachChild(croix[parametre][colonne]);
        }
        croix[parametre][colonne] = null;
        coche[parametre][colonne] = false;
    }

    public void cliqueCase(int parametre) {
        inputManager.addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(new ActionListener() {
            @Override 
            public void onAction(String Name, boolean clique, float tpf) {
                if (!clique) return;
                Vector2f souris = inputManager.getCursorPosition(); 
                for (int i = 0; i < 6; i++) {
                    if (carres[parametre][i] == null) continue;
                    Geometry carre = carres[parametre][i];
                    Vector3f position = carre.getLocalTranslation();
                    float x = position.x;
                    float y = position.y;

                    boolean dansX = souris.x >= x && souris.x <= x + 50;
                    boolean dansY = souris.y >= y && souris.y <= y + 50;

                    if (dansX && dansY) {
                        cocherCase(i,x,y,parametre);
                        coche[parametre][i] = true;
                        for  (int caseCoche = 0; caseCoche < 6; caseCoche ++) {
                            if (caseCoche == i) {
                                continue;
                            }
                            if (coche[parametre][caseCoche] == true) {
                                decocherCase(parametre,caseCoche);
                            }
                        }
                    }

                }
                    
            }
        },"Click");

    }

    private void cocherCase(int index, float x, float y, int parametre) {

        if (croix[parametre][index] != null) return; // déjà une croix ici



        Node croixNode = new Node("Croix" + index);



        // Trait horizontal

        Quad traitHorizontal = new Quad(20, 5);

        // Trait vertical

        Quad traitVertical = new Quad(5, 20);

        Geometry geometrieHorizontal = new Geometry("BarreH", traitHorizontal);

        Geometry geometrieVerticale = new Geometry("BarreV", traitVertical);

        Material materialHorizontal = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        Material materielVerticale = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        materialHorizontal.setColor("Color", ColorRGBA.Red);

        materielVerticale.setColor("Color", ColorRGBA.Red);

        geometrieHorizontal.setMaterial(materialHorizontal);

        geometrieVerticale.setMaterial(materielVerticale);

        geometrieHorizontal.setLocalTranslation(0f, 7.5f, 0); // centré horizontalement

        geometrieVerticale.setLocalTranslation(7.5f, 0f, 0); // centré verticalement



        croixNode.attachChild(geometrieHorizontal);

        croixNode.attachChild(geometrieVerticale);



        // Positionner la croix dans la case

        croixNode.setLocalTranslation(x, y, 2); // couche au-dessus

        guiNode.attachChild(croixNode);



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
                    formationCase(5,375,0);
                    cliqueCase(0);
                    text(new TextBuilder() {{

                        text("Reglage de la température.");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();
                        //valignTop();

                    }});

                    panel (new PanelBuilder() {{
                        height("40%");
                    }});

                    formationCase(5,250,1);
                    cliqueCase(1);
                    text(new TextBuilder() {{

                        text("Reglage de l'humidité.");

                        font("Interface/Fonts/Default.fnt");

                        height("20%");

                        width("100%");

                        alignCenter();
                        //valignTop();

                    }});
                    panel (new PanelBuilder() {{
                        height("40%");
                    }});

                    formationCase(5,125,2);
                    cliqueCase(2);
                    text(new TextBuilder() {{

                        text("Reglage des reliefs");

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

        stop(); // Ferme l'application JME

    }

    public void reglageFaune() {
        System.out.println("Modification des paramètres de la faune...");
        modifierFaune();
        nifty.gotoScreen("faune");
    }


    // Méthodes du ScreenController

    public void bind(Nifty nifty, Screen screen) {}


    public void onStartScreen() {}


    public void onEndScreen() {}

}
