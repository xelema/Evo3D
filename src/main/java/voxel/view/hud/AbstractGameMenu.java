package voxel.view.hud;

import com.jme3.app.SimpleApplication;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 * Classe abstraite servant de base pour tous les menus du jeu.
 * Implémente l'interface ScreenController pour l'intégration avec NiftyGUI.
 */
public abstract class AbstractGameMenu implements ScreenController {
    
    /** Application principale */
    protected SimpleApplication app;
    
    /** Interface Nifty pour le rendu du menu */
    protected Nifty nifty;
    
    /** Affichage Nifty pour JME3 */
    protected NiftyJmeDisplay niftyDisplay;
    
    /** Indique si le menu est actuellement visible */
    protected boolean menuVisible = false;
    
    /**
     * Constructeur avec l'application JME3
     * 
     * @param app L'application SimpleApplication
     */
    public AbstractGameMenu(SimpleApplication app) {
        this.app = app;
    }
    
    /**
     * Initialise le menu et ses composants
     */
    public abstract void initialize();
    
    /**
     * Affiche le menu
     */
    public void showMenu() {
        if (!menuVisible) {
            app.getInputManager().setCursorVisible(true);
            
            // Seulement ajouter le processeur Nifty sans toucher aux mappings
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
     * Récupère l'interface Nifty
     * 
     * @return L'interface Nifty
     */
    public Nifty getNifty() {
        return this.nifty;
    }
    
    // Implémentation des méthodes de ScreenController
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        // À implémenter dans les classes dérivées si nécessaire
    }
    
    @Override
    public void onStartScreen() {
        // À implémenter dans les classes dérivées si nécessaire
    }
    
    @Override
    public void onEndScreen() {
        // À implémenter dans les classes dérivées si nécessaire
    }
} 