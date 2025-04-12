package voxel.view;
import com.jme3.app.SimpleApplication;

import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;

import de.lessvoid.nifty.builder.*;

import de.lessvoid.nifty.screen.*;


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


        // Ajouter le menu principal

        createMenu();


        // Afficher le menu

        nifty.gotoScreen("start");


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


                    control(new ButtonBuilder("startButton", "Démarrer le jeu") {{

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

            }});

        }}.build(nifty));

    }


    // Méthodes déclenchées par les boutons

    public void startGame() {

        System.out.println("Lancement du jeu...");

        // Tu peux ici changer d'écran ou démarrer la logique du jeu

    }


    public void openOptions() {

        System.out.println("Ouverture des options...");

        // Ici tu peux basculer vers un écran "options"

    }


    public void quitGame() {

        System.out.println("Fermeture du jeu...");

        stop(); // Ferme l'application JME

    }


    // Méthodes du ScreenController

    public void bind(Nifty nifty, Screen screen) {}


    public void onStartScreen() {}


    public void onEndScreen() {}

}
