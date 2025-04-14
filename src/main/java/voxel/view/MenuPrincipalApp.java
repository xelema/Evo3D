package voxel.view;
import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MenuPrincipalApp extends SimpleApplication implements ScreenController {


    private Nifty nifty;


    public static void main(String[] args) {

        new MenuPrincipalApp().start();

    }


    @Override

    public void simpleInitApp() {

        // Créer le système d'affichage Nifty lié à JME

        NiftyJmeDisplay niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(

                assetManager, inputManager, audioRenderer, guiViewPort);

        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        // Ajouter le menu principal

        createMenu();


        // Afficher le menu

        nifty.gotoScreen("start");
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);

        // Attacher Nifty à la GUI de JME

        guiViewPort.addProcessor(niftyDisplay);

    }


    public void createMenu() {

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

                        interactOnClick("openOptions()");

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
    
    public void modifierFaune() {

        nifty.addScreen("reglage", new ScreenBuilder("reglage") {{

            controller(MenuPrincipalApp.this); // liaison au ScreenController


            layer(new LayerBuilder("Modifications") {{

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


    public void quitGame() {

        System.out.println("Fermeture du jeu...");

        stop(); // Ferme l'application JME

    }

    public void reglageFaune() {
        System.out.println("Modification des paramètres de la faune...");
        modifierFaune();
    }


    // Méthodes du ScreenController

    public void bind(Nifty nifty, Screen screen) {}


    public void onStartScreen() {}


    public void onEndScreen() {}

}
