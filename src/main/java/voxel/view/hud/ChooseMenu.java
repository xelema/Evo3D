package voxel.view.hud;
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

import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.builder.ElementBuilder;

import voxel.controller.GameStateManager;

public class ChooseMenu extends AbstractGameMenu {

    private static final int NOMBRECASESMAX = 5;
    private static final int NOMBREPARAMETRES = 3;
    
    // Variables pour les cases colorées JME GUI
    private Node[][] croix = new Node[NOMBREPARAMETRES][NOMBRECASESMAX];
    private Geometry[][] carres = new Geometry[NOMBREPARAMETRES][NOMBRECASESMAX];
    private boolean[][] coche = new boolean[NOMBREPARAMETRES][NOMBRECASESMAX];
    private BitmapText[][] textes = new BitmapText[NOMBREPARAMETRES][NOMBRECASESMAX];
    private BitmapText[] titres = new BitmapText[NOMBREPARAMETRES];
    
    // Variables pour stocker les sélections
    private int[] selectionsParametres = new int[NOMBREPARAMETRES]; // -1 = non sélectionné
    private String[] nomsParametres = {"Température", "Humidité", "Relief"};
    private String[] optionsNiveaux = {"Minimal", "Faible", "Modéré", "Élevé", "Maximum"};
    
    // Variables temporaires pour la configuration en cours (non sauvegardées tant qu'on ne valide pas)
    private int[] selectionsTemporaires = new int[NOMBREPARAMETRES];
    private boolean[][] cocheTemporaire = new boolean[NOMBREPARAMETRES][NOMBRECASESMAX];
    
    // Flag pour éviter les listeners multiples
    private boolean listenersAjoutes = false;

    private boolean gameStarted = false;
    
    // Référence au GameStateManager pour accéder au WorldModel
    private GameStateManager gameStateManager;

    /**
     * Constructeur du menu de choix
     * @param app L'application SimpleApplication
     */
    public ChooseMenu(SimpleApplication app) {
        super(app);
        // Initialiser les sélections
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            selectionsParametres[i] = -1; // Aucune sélection par défaut
            selectionsTemporaires[i] = -1; // Aucune sélection temporaire par défaut
            for (int j = 0; j < NOMBRECASESMAX; j++) {
                cocheTemporaire[i][j] = false;
            }
        }
    }
    
    /**
     * Définit la référence au GameStateManager
     * @param gameStateManager Le gestionnaire d'états du jeu
     */
    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
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
     * Cache le menu et nettoie tous les éléments GUI
     */
    public void hideMenu() {
        if (menuVisible || app.getGuiViewPort().getProcessors().contains(niftyDisplay)) {
            app.getInputManager().setCursorVisible(false);
            app.getGuiViewPort().removeProcessor(niftyDisplay);
            
            // Nettoyer les éléments JME GUI
            nettoyerCases();
            
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

            controller(ChooseMenu.this); // liaison au ScreenController


            layer(new LayerBuilder("backgroundLayer") {{
                backgroundColor("#000000aa");
                childLayoutAbsolute();
            }});

            layer(new LayerBuilder("layer") {{

                childLayoutCenter();


                panel(new PanelBuilder("panel") {{

                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("70%");
                    backgroundColor("#1a1a1aee");
                    style("nifty-panel");

                    // Titre principal avec style amélioré
                    text(new TextBuilder() {{
                        text("--- CRÉATION DU MONDE ---");
                        font("Interface/Fonts/Default.fnt");
                        height("15%");
                        width("100%");
                        alignCenter();
                        color("#ffffff");
                        style("nifty-label");
                    }});

                    // Espacement
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});

                    control(new ButtonBuilder("optionsButton", "CONFIGURATION DU MONDE") {{
                        alignCenter();
                        height("12%");
                        width("70%");
                        interactOnClick("openOptions()");
                        style("nifty-button");
                        focusable(!hasAllParametersConfigured());
                    }});

                    // Espacement
                    panel(new PanelBuilder() {{
                        height("3%");
                    }});

                    control(new ButtonBuilder("demarrerLeJeu", "DÉMARRER LE JEU") {{
                        alignCenter();
                        height("12%");
                        width("70%");
                        interactOnClick("startGame()");
                        style("nifty-button");
                        focusable(hasAllParametersConfigured());
                    }});

                    // Espacement
                    panel(new PanelBuilder() {{
                        height("3%");
                    }});

                    control(new ButtonBuilder("quitButton", "QUITTER") {{
                        alignCenter();
                        height("12%");
                        width("70%");
                        interactOnClick("quitGame()");
                        style("nifty-button");
                    }});

                    // Espacement du bas
                    panel(new PanelBuilder() {{
                        height("10%");
                    }});

                }});
                System.out.println("Menu construit !");
            }});

        }}.build(nifty));

    }


    public void fenetreReglage() {

        nifty.addScreen("reglage", new ScreenBuilder("reglage") {{

            controller(ChooseMenu.this); // liaison au ScreenController

            // Arrière-plan sombre
            layer(new LayerBuilder("backgroundLayer") {{
                backgroundColor("#000000aa");
                childLayoutAbsolute();
            }});

            layer(new LayerBuilder("layerReglage") {{

                childLayoutCenter();


                panel(new PanelBuilder("panelReglage") {{

                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("70%");
                    height("80%");
                    backgroundColor("#1a1a1aee");
                    style("nifty-panel");

                    // Titre avec style amélioré
                    text(new TextBuilder() {{
                        text("--- CONFIGURATION DE L'ENVIRONNEMENT ---");
                        font("Interface/Fonts/Default.fnt");
                        height("15%");
                        width("100%");
                        alignCenter();
                        color("#ffffff");
                        style("nifty-label");
                    }});

                    // Espacement (réduit pour monter les sections)
                    panel(new PanelBuilder() {{
                        height("2%");
                    }});

                    // Section Biomes
                    panel(new PanelBuilder("biomesSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("90%");
                        height("20%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("2%");
                        paddingBottom("2%");

                        text(new TextBuilder() {{
                            text("BIOMES & ÉCOSYSTÈMES");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#66ff66");
                        }});

                        control(new ButtonBuilder("faune", "Paramètres d'Écosystème") {{
                            alignCenter();
                            height("50%");
                            width("80%");
                            interactOnClick("reglageFaune()");
                            style("nifty-button");
                            focusable(false);
                        }});
                    }});

                    // Espacement (réduit)
                    panel(new PanelBuilder() {{
                        height("2%");
                    }});

                    // Section Terrain
                    panel(new PanelBuilder("terrainSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("90%");
                        height("20%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("2%");
                        paddingBottom("2%");

                        text(new TextBuilder() {{
                            text("GÉNÉRATION DU TERRAIN");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#ffaa66");
                        }});

                        control(new ButtonBuilder("flore", "Paramètres Géologiques") {{
                            alignCenter();
                            height("50%");
                            width("80%");
                            interactOnClick("reglageFlore");
                            style("nifty-button");
                            focusable(false);
                        }});
                    }});

                    // Espacement (réduit)
                    panel(new PanelBuilder() {{
                        height("2%");
                    }});

                    // Section Climat
                    panel(new PanelBuilder("climatSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("90%");
                        height("20%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("2%");
                        paddingBottom("2%");

                        text(new TextBuilder() {{
                            text("CONDITIONS CLIMATIQUES");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#66aaff");
                        }});

                        control(new ButtonBuilder("conditionEnvironnement", "Paramètres Météorologiques") {{
                            alignCenter();
                            height("50%");
                            width("80%");
                            interactOnClick("reglageEnvironnement()");
                            style("nifty-button");
                            focusable(false);
                        }});
                    }});

                    // Espacement avant le bouton retour (pour le descendre)
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});

                    // Bouton retour stylisé
                    control(new ButtonBuilder("retourPrincipal", "← RETOUR AU MENU PRINCIPAL") {{
                        alignCenter();
                        height("8%");
                        width("60%");
                        interactOnClick("retour()");
                        style("nifty-button");
                    }});

                }});
                System.out.println("Menu construit !");
            }});

        }}.build(nifty));

    }
    
    public void modifierFaune() {
        nifty.addScreen("faune", new ScreenBuilder("faune") {{
            controller(ChooseMenu.this);
            
            // Arrière-plan sombre
            layer(new LayerBuilder("backgroundLayer") {{
                backgroundColor("#000000aa");
                childLayoutAbsolute();
            }});
           
            layer(new LayerBuilder("Modifications") {{
                childLayoutCenter();

                panel(new PanelBuilder("panelReglage") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("85%");
                    height("90%");
                    backgroundColor("#1a1a1aee");
                    style("nifty-panel");
                    paddingTop("2%");

                    // Titre principal
                    text(new TextBuilder() {{
                        text("CONFIGURATION DES BIOMES");
                        font("Interface/Fonts/Default.fnt");
                        height("8%");
                        width("100%");
                        alignCenter();
                        color("#66ff66");
                    }});

                    // Section Température avec espace pour les cases
                    panel(new PanelBuilder("tempSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("95%");
                        height("25%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("1%");
                        paddingBottom("1%");

                        text(new TextBuilder() {{
                            text("TEMPÉRATURE AMBIANTE");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#ff9999");
                        }});

                        // Espace réservé pour les cases JME GUI
                        panel(new PanelBuilder("espaceTemp") {{
                            height("70%");
                            width("100%");
                        }});
                    }});
                    
                    // Espacement
                    panel(new PanelBuilder() {{
                        height("2%");
                    }});

                    // Section Humidité avec espace pour les cases
                    panel(new PanelBuilder("humSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("95%");
                        height("25%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("1%");
                        paddingBottom("1%");

                        text(new TextBuilder() {{
                            text("TAUX D'HUMIDITÉ");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#99ccff");
                        }});

                        // Espace réservé pour les cases JME GUI
                        panel(new PanelBuilder("espaceHum") {{
                            height("70%");
                            width("100%");
                        }});
                    }});

                    // Espacement
                    panel(new PanelBuilder() {{
                        height("2%");
                    }});

                    // Section Relief avec espace pour les cases
                    panel(new PanelBuilder("relSection") {{
                        childLayoutVertical();
                        alignCenter();
                        width("95%");
                        height("25%");
                        backgroundColor("#2a2a2aaa");
                        paddingTop("1%");
                        paddingBottom("1%");

                        text(new TextBuilder() {{
                            text("COMPLEXITÉ DU RELIEF");
                            font("Interface/Fonts/Default.fnt");
                            height("30%");
                            width("100%");
                            alignCenter();
                            color("#ffcc99");
                        }});

                        // Espace réservé pour les cases JME GUI
                        panel(new PanelBuilder("espaceRel") {{
                            height("70%");
                            width("100%");
                        }});
                    }});

                    // Espacement
                    panel(new PanelBuilder() {{
                        height("8%");
                    }});

                    // Panneau des boutons en bas
                    panel(new PanelBuilder() {{
                        childLayoutHorizontal();
                        alignCenter();
                        height("8%");
                        width("80%");

                        control(new ButtonBuilder("retourButton", "RETOUR ←") {{
                            alignLeft();
                            valignCenter();
                            height("100%");
                            width("30%");
                            interactOnClick("retour()");
                            style("nifty-button");
                            focusable(false);
                        }});

                        // Espacement pour séparer les boutons
                        panel(new PanelBuilder() {{
                            width("40%");
                            height("100%");
                        }});

                        control(new ButtonBuilder("validerButton", "VALIDER ✓") {{
                            alignRight();
                            valignCenter();
                            height("100%");
                            width("30%");
                            interactOnClick("validerConfiguration()");
                            style("nifty-button");
                            focusable(true); // Ce bouton peut avoir le focus
                        }});
                    }});
                }});
            }});
        }}.build(nifty));
    }

    public void creerCasesColorees() {
        int largeurEcran = app.getContext().getSettings().getWidth();
        int hauteurEcran = app.getContext().getSettings().getHeight();
        
        // Dimensions des cases
        float largeurCase = 50;
        float hauteurCase = 50;
        float espaceEntreCases = 20;
        
        // Position de départ centrée
        float largeurTotale = (largeurCase * NOMBRECASESMAX) + (espaceEntreCases * (NOMBRECASESMAX - 1));
        float xDepart = (largeurEcran - largeurTotale) / 2;
        
        // Positions Y pour chaque section (ajustées pour être mieux centrées)
        float[] positionsY = {
            hauteurEcran * 0.73f, // Température
            hauteurEcran * 0.50f, // Humidité
            hauteurEcran * 0.26f  // Relief
        };
        
        String[] couleurs = {"#5577dd", "#66aa66", "#dddd55", "#dd8844", "#dd4444"};
        ColorRGBA[] couleursCase = {
            new ColorRGBA(0.33f, 0.47f, 0.87f, 0.9f), // Bleu
            new ColorRGBA(0.4f, 0.67f, 0.4f, 0.9f),   // Vert
            new ColorRGBA(0.87f, 0.87f, 0.33f, 0.9f), // Jaune
            new ColorRGBA(0.87f, 0.53f, 0.27f, 0.9f), // Orange
            new ColorRGBA(0.87f, 0.27f, 0.27f, 0.9f)  // Rouge
        };
        
        // Get the GUI font
        BitmapText tempText = new BitmapText(app.getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
        
        for (int parametre = 0; parametre < NOMBREPARAMETRES; parametre++) {
//            // Titre du paramètre
//            BitmapText titre = new BitmapText(tempText.getFont(), false);
////            titre.setSize(24);
//            titre.setText(nomsParametres[parametre].toUpperCase());
//            titre.setColor(ColorRGBA.Cyan);
//            float titreLargeur = titre.getLineWidth();
//            titre.setLocalTranslation((largeurEcran - titreLargeur) / 2, positionsY[parametre] + 80, 1);
//            app.getGuiNode().attachChild(titre);
//            titres[parametre] = titre;
            
            for (int i = 0; i < NOMBRECASESMAX; i++) {
                // Créer la case colorée
                Quad caseCoche = new Quad(largeurCase, hauteurCase);
                Geometry geometry = new Geometry("Case_" + parametre + "_" + i, caseCoche);
                Material material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                material.setColor("Color", couleursCase[i]);
                geometry.setMaterial(material);
                
                float x = xDepart + i * (largeurCase + espaceEntreCases);
                float y = positionsY[parametre];
                geometry.setLocalTranslation(x, y, 0);
            app.getGuiNode().attachChild(geometry);
                
                // Texte sous la case
            BitmapText choix = new BitmapText(tempText.getFont(), false);
//                choix.setSize(16);
                choix.setText(optionsNiveaux[i]);
                choix.setColor(ColorRGBA.White);
            float texteX = x + (largeurCase / 2f) - (choix.getLineWidth() / 2f);
                float texteY = y - 25;
                choix.setLocalTranslation(texteX, texteY, 1);
            app.getGuiNode().attachChild(choix);

            textes[parametre][i] = choix;
            carres[parametre][i] = geometry;
            croix[parametre][i] = null;
                
                // Cocher la case si elle était déjà sélectionnée (selon les sélections temporaires)
                if (cocheTemporaire[parametre][i]) {
                    cocherCase(i, x, y, parametre);
                }
            }
        }
        
        // Ajouter le listener de clic une seule fois
        if (!listenersAjoutes) {
            ajouterListenerClics();
            listenersAjoutes = true;
        }
    }

    private void ajouterListenerClics() {
        app.getInputManager().addMapping("ClickCases", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(new ActionListener() {
            @Override 
            public void onAction(String name, boolean pressed, float tpf) {
                if (!pressed) return;
                Vector2f souris = app.getInputManager().getCursorPosition();
                
                for (int parametre = 0; parametre < NOMBREPARAMETRES; parametre++) {
                    for (int i = 0; i < NOMBRECASESMAX; i++) {
                        if (carres[parametre][i] == null) continue;
                        
                        Geometry carre = carres[parametre][i];
                        Vector3f position = carre.getLocalTranslation();
                        float x = position.x;
                        float y = position.y;
                        
                        // Vérifier si le clic est dans cette case
                        boolean dansX = souris.x >= x && souris.x <= x + 50; // largeurCase = 50
                        boolean dansY = souris.y >= y && souris.y <= y + 50; // hauteurCase = 50

                        if (dansX && dansY) {
                            System.out.println("Sélection: " + nomsParametres[parametre] + " -> " + optionsNiveaux[i]);
                            selectionnerCase(parametre, i, x, y);
                            return; // Sortir après la première case trouvée
                        }
                    }
                }
            }
        }, "ClickCases");
    }

    private void selectionnerCase(int parametre, int caseIndex, float x, float y) {
        // Décocher toutes les autres cases de ce paramètre
        for (int i = 0; i < NOMBRECASESMAX; i++) {
            if (i != caseIndex && cocheTemporaire[parametre][i]) {
                decocherCase(parametre, i);
            }
        }
        
        // Cocher la case sélectionnée
        if (!cocheTemporaire[parametre][caseIndex]) {
            cocherCase(caseIndex, x, y, parametre);
            cocheTemporaire[parametre][caseIndex] = true;
            selectionsTemporaires[parametre] = caseIndex;
        }
    }

    private void nettoyerCases() {
        for (int parametre = 0; parametre < NOMBREPARAMETRES; parametre++) {
            for (int i = 0; i < NOMBRECASESMAX; i++) {
                if (carres[parametre][i] != null) {
                    app.getGuiNode().detachChild(carres[parametre][i]);
                    carres[parametre][i] = null;
                }
                
                if (croix[parametre][i] != null) {
                    app.getGuiNode().detachChild(croix[parametre][i]);
                    croix[parametre][i] = null;
                }

                if (textes[parametre][i] != null) {
                    app.getGuiNode().detachChild(textes[parametre][i]);
                    textes[parametre][i] = null;
                }
            }
            
            if (titres[parametre] != null) {
                app.getGuiNode().detachChild(titres[parametre]);
                titres[parametre] = null;
            }
        }
    }

    private void decocherCase(int parametre, int colonne) {
        if (croix[parametre][colonne] != null) {
            app.getGuiNode().detachChild(croix[parametre][colonne]);
            croix[parametre][colonne] = null;
        }
        cocheTemporaire[parametre][colonne] = false;
    }

    private void cocherCase(int index, float x, float y, int parametre) {
        if (croix[parametre][index] != null) return; // déjà un carré ici

        Node carreNode = new Node("Carre_" + parametre + "_" + index);

        // Création d'un petit carré blanc de sélection
        float tailleCarreSelection = 30; // Carré plus petit que la case (50x50)
        Quad carreSelection = new Quad(tailleCarreSelection, tailleCarreSelection);
        
        Geometry geometrieCarreSelection = new Geometry("CarreSelection", carreSelection);
        Material materialCarreSelection = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");

        // Couleur blanche
        ColorRGBA couleurSelection = new ColorRGBA(1.0f, 1.0f, 1.0f, 0.95f);
        materialCarreSelection.setColor("Color", couleurSelection);

        geometrieCarreSelection.setMaterial(materialCarreSelection);

        // Centrer le petit carré dans la case
        float offsetX = (50 - tailleCarreSelection) / 2; // 50 = taille de la case principale
        float offsetY = (50 - tailleCarreSelection) / 2;
        geometrieCarreSelection.setLocalTranslation(offsetX, offsetY, 0);

        carreNode.attachChild(geometrieCarreSelection);

        // Positionner le carré dans la case
        carreNode.setLocalTranslation(x, y, 2); // couche au-dessus
        app.getGuiNode().attachChild(carreNode);

        croix[parametre][index] = carreNode; // On garde le même nom de variable pour la compatibilité
    }

    // Méthode appelée quand un niveau est sélectionné (pour compatibilité)
    public void selectionnerNiveau(int parametre, int niveau) {
        System.out.println("Sélection: " + nomsParametres[parametre] + " -> " + optionsNiveaux[niveau]);
        selectionsParametres[parametre] = niveau;
    }

    // Méthodes déclenchées par les boutons

    public void startGame() {
        System.out.println("Lancement du jeu...");
        System.out.println("Bouton démarrer cliqué !");
        
        // Vérifier si tous les paramètres ont été configurés
        if (!hasAllParametersConfigured()) {
            // Au moins un paramètre manque, afficher l'avertissement
            creerEcranAvertissement();
            nifty.gotoScreen("avertissementParametres");
            return;
        }
        
        // Continuer avec le lancement normal du jeu
        lancerJeuAvecParametres();
    }
    
    /**
     * Lance effectivement le jeu avec les paramètres configurés
     */
    private void lancerJeuAvecParametres() {
        // Afficher les paramètres qui seront utilisés
        int[] parametres = getParametresEnvironnementaux();
        System.out.println("Paramètres qui seront utilisés pour créer le monde:");
        System.out.println("- Température: " + parametres[0] + " (" + optionsNiveaux[parametres[0]] + ")");
        System.out.println("- Humidité: " + parametres[1] + " (" + optionsNiveaux[parametres[1]] + ")");
        System.out.println("- Relief: " + parametres[2] + " (" + optionsNiveaux[parametres[2]] + ")");
        
        // Indiquer que le jeu a démarré
        this.gameStarted = true;
        // Cacher ce menu (le GameStateManager s'occupera d'afficher l'écran de chargement)
        hideMenu();
    }

    public boolean hasGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean state) {
        this.gameStarted = state;
    }

    public void openOptions() {
        System.out.println("Ouverture des options...");
        fenetreReglage();
        nifty.gotoScreen("reglage");
    }

    public void retour() {
        System.out.println("Retour...");
        // Restaurer les sélections temporaires (annuler les changements non validés)
        restaurerSelectionsTemporaires();
        nettoyerCases(); // Nettoyer les cases avant de retourner
        nifty.gotoScreen("start");
    }

    public void quitGame() {
        System.out.println("Fermeture du jeu...");
        app.stop(); // Ferme l'application JME
    }

    public void reglageFaune() {
        System.out.println("Modification des paramètres de la faune...");
        // Initialiser les sélections temporaires avec les valeurs actuellement sauvegardées
        initialiserSelectionsTemporaires();
        modifierFaune();
        nifty.gotoScreen("faune");
        // Créer les cases colorées après avoir affiché l'écran
        creerCasesColorees();
    }

    /**
     * Initialise les sélections temporaires avec les valeurs actuellement sauvegardées
     */
    private void initialiserSelectionsTemporaires() {
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            selectionsTemporaires[i] = selectionsParametres[i];
            for (int j = 0; j < NOMBRECASESMAX; j++) {
                cocheTemporaire[i][j] = (selectionsParametres[i] == j);
            }
        }
    }
    
    /**
     * Restaure les sélections temporaires avec les valeurs sauvegardées (annule les changements non validés)
     */
    private void restaurerSelectionsTemporaires() {
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            selectionsTemporaires[i] = selectionsParametres[i];
            for (int j = 0; j < NOMBRECASESMAX; j++) {
                cocheTemporaire[i][j] = (selectionsParametres[i] == j);
            }
        }
    }

    /**
     * Récupère les paramètres environnementaux sélectionnés
     * @return Tableau des paramètres [température, humidité, relief] avec valeurs par défaut si non sélectionnés
     */
    public int[] getParametresEnvironnementaux() {
        int[] parametres = new int[3];
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            // Si aucune sélection, utiliser valeur par défaut (2 = Modéré)
            parametres[i] = selectionsParametres[i] >= 0 ? selectionsParametres[i] : 2;
        }
        return parametres;
    }
    
    /**
     * Vérifie si des paramètres ont été configurés (au moins un paramètre sélectionné)
     * @return true si au moins un paramètre a été sélectionné
     */
    public boolean hasConfiguredParameters() {
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            if (selectionsParametres[i] >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si TOUS les paramètres ont été configurés
     * @return true si tous les paramètres ont été sélectionnés
     */
    public boolean hasAllParametersConfigured() {
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            if (selectionsParametres[i] < 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Récupère la liste des paramètres non configurés
     * @return String avec les paramètres manquants
     */
    public String getMissingParameters() {
        StringBuilder missing = new StringBuilder();
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            if (selectionsParametres[i] < 0) {
                if (missing.length() > 0) {
                    missing.append(", ");
                }
                missing.append(nomsParametres[i]);
            }
        }
        return missing.toString();
    }

    public void validerConfiguration() {
        System.out.println("Configuration validée !");
        
        // Sauvegarder les sélections temporaires dans les variables définitives
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            selectionsParametres[i] = selectionsTemporaires[i];
        }
        
        // Afficher les sélections actuelles
        for (int i = 0; i < NOMBREPARAMETRES; i++) {
            if (selectionsParametres[i] >= 0) {
                System.out.println(nomsParametres[i] + ": " + optionsNiveaux[selectionsParametres[i]] + " (niveau " + selectionsParametres[i] + ")");
            } else {
                System.out.println(nomsParametres[i] + ": Non sélectionné (utilisation par défaut: niveau 2)");
            }
        }
        
        // Recréer le menu principal pour mettre à jour le focus
        int tailleEcran = app.getContext().getSettings().getHeight();
        createMenu(tailleEcran);
        
        retour();
    }

    /**
     * Crée l'écran d'avertissement pour les paramètres par défaut
     */
    private void creerEcranAvertissement() {
        nifty.addScreen("avertissementParametres", new ScreenBuilder("avertissementParametres") {{
            controller(ChooseMenu.this);
            
            // Arrière-plan sombre
            layer(new LayerBuilder("backgroundLayer") {{
                backgroundColor("#000000cc");
                childLayoutAbsolute();
            }});
            
            layer(new LayerBuilder("avertissementLayer") {{
                childLayoutCenter();
                
                panel(new PanelBuilder("avertissementPanel") {{
                    childLayoutVertical();
                    alignCenter();
                    valignCenter();
                    width("60%");
                    height("50%");
                    backgroundColor("#1a1a1aee");
                    style("nifty-panel");
                    paddingTop("3%");
                    paddingBottom("3%");
                    paddingLeft("3%");
                    paddingRight("3%");
                    
                    // Titre d'avertissement
                    text(new TextBuilder() {{
                        text("AVERTISSEMENT");
                        font("Interface/Fonts/Default.fnt");
                        height("20%");
                        width("100%");
                        alignCenter();
                        color("#ffaa00");
                    }});
                    
                    // Espacement
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});
                    
                    // Message d'avertissement
                    text(new TextBuilder() {{
                        String parametresManquants = getMissingParameters();
                        String messageConfigures = "";
                        String messageParDefaut = "";
                        
                        // Construire le message pour les paramètres configurés
                        for (int i = 0; i < NOMBREPARAMETRES; i++) {
                            if (selectionsParametres[i] >= 0) {
                                if (messageConfigures.length() > 0) {
                                    messageConfigures += "\n";
                                }
                                messageConfigures += "• " + nomsParametres[i] + " : " + optionsNiveaux[selectionsParametres[i]];
                            }
                        }
                        
                        // Construire le message pour les paramètres par défaut
                        for (int i = 0; i < NOMBREPARAMETRES; i++) {
                            if (selectionsParametres[i] < 0) {
                                if (messageParDefaut.length() > 0) {
                                    messageParDefaut += "\n";
                                }
                                messageParDefaut += "• " + nomsParametres[i] + " : Modéré (par défaut)";
                            }
                        }
                        
                        String messageComplet = "Paramètres manquants : " + parametresManquants + "\n\n";
                        
                        if (messageConfigures.length() > 0) {
                            messageComplet += "Paramètres configurés :\n" + messageConfigures + "\n\n";
                        }
                        
                        messageComplet += "Paramètres qui seront utilisés par défaut :\n" + messageParDefaut + "\n\n";
                        messageComplet += "Souhaitez-vous continuer ?";
                        
                        text(messageComplet);
                        font("Interface/Fonts/Default.fnt");
                        height("60%");
                        width("100%");
                        alignCenter();
                        valignCenter();
                        color("#ffffff");
                        wrap(true);
                    }});
                    
                    // Espacement
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});
                    
                    // Panneau des boutons
                    panel(new PanelBuilder("boutonsPanel") {{
                        childLayoutHorizontal();
                        alignCenter();
                        height("20%");
                        width("100%");
                        
                        control(new ButtonBuilder("continuerButton", "CONTINUER") {{
                            alignLeft();
                            valignCenter();
                            height("100%");
                            width("40%");
                            interactOnClick("continuerAvecParametresDefaut()");
                            style("nifty-button");
                            focusable(false);
                        }});
                        
                        // Espacement entre les boutons
                        panel(new PanelBuilder() {{
                            width("20%");
                            height("100%");
                        }});
                        
                        control(new ButtonBuilder("configurerButton", "CONFIGURER") {{
                            alignRight();
                            valignCenter();
                            height("100%");
                            width("40%");
                            interactOnClick("retournerConfiguration()");
                            style("nifty-button");
                            focusable(true); // Ce bouton encourage la configuration
                        }});
                    }});
                }});
            }});
        }}.build(nifty));
    }

    // Les méthodes ScreenController sont héritées de AbstractGameMenu
    
    /**
     * Continue le lancement du jeu avec les paramètres par défaut
     */
    public void continuerAvecParametresDefaut() {
        System.out.println("Lancement du jeu avec paramètres par défaut...");
        lancerJeuAvecParametres();
    }
    
    /**
     * Retourne à la configuration pour permettre à l'utilisateur de configurer les paramètres
     */
    public void retournerConfiguration() {
        System.out.println("Retour à la configuration des paramètres...");
        openOptions();
    }
}
