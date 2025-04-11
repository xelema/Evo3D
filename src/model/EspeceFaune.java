
/**
 * Faune.java
 * Classe représentant une espèce de faune.
 * (implémente l'interface Espece). 
 */
import java.util.Map;

public class EspeceFaune implements Espece {
    private final String nom; // Nom de l'espèce de faune
    private final Map<Biome, Integer> compatibilite; // Compatibilité avec les biomes
    private final REGIME_ALIMENTAIRE regime; // Régime alimentaire
    private final COMPORTEMENT comportement; // Comportement de l'espèce

    public enum REGIME_ALIMENTAIRE { // Régimes alimentaires
        HERBIVORE,
        CARNIVORE,
        OMNIVORE,              
        INSECTIVORE,
        FRUGIVORE    
    }
    public enum COMPORTEMENT { // Comportement de l'espèce
        NOCTURNE,
        DIURNE,
        CREPUSCULAIRE,
        MIGRATEUR,
        SEDENTAIRE
    }

    
    /**
     * Constructeur de la classe Faune.
     * @param p_nom Nom de l'espèce de faune.
     * @param p_compatibilité avec les biomes (sous forme de Map).
     * @param p_regime caracteristiques Regime alimentaire
     * @param p_comportement caracteristiques Comportement de l'espèce
     */    
    public EspeceFaune(String p_nom, Map<Biome, Integer> p_compatibilite, REGIME_ALIMENTAIRE p_regime, COMPORTEMENT p_comportement) {
        this.nom = p_nom;
        this.compatibilite = p_compatibilite;
        this.regime = p_regime;
        this.comportement = p_comportement;
    }

    /*
     * Méthode pour obtenir le nom de l'espèce.
     * @return le nom de l'espèce.
     */
    @Override
    public String getNom() {
        return nom;
    }

    /*
     * Méthode pour obtenir le type de l'espèce.
     * @return le type de l'espèce (faune).
     */
    @Override
    public String getType() {
        return "faune";
    }

    /*
     * Méthode pour obtenir la compatibilité de l'espèce avec les biomes.
     * @return la compatibilité de l'espèce avec les biomes (sous forme de Map).
     */
    @Override
    public Map<Biome, Integer> getCompatibilite() {
        return compatibilite;
    }

    /*
     * Méthode pour obtenir le régime alimentaire de l'espèce.
     * @return le régime alimentaire de l'espèce (sous forme d'énumération).
     */
    public REGIME_ALIMENTAIRE getRegime() {
        return regime;
    }

    /*
     * Méthode pour obtenir le comportement de l'espèce.
     * @return le comportement de l'espèce (sous forme d'énumération).
     */
    public COMPORTEMENT getComportement() {
        return comportement;
    }

    /*
     * Méthode pour afficher la représentation sous forme de chaîne de l'espèce.
     * @return la représentation sous forme de chaîne de l'espèce.
     */
    @Override
    public String toString() {
        return "[Faune] " + nom + " - Compatibilité avec les biomes : " + compatibilite.toString()
                + " - Régime alimentaire : " + regime.toString() + " - Comportement : " + comportement.toString();
    }
}
