/*
 * Flore.java
 * Classe représentant une espèce de flore.
 * (implémente l'interface Espece). 
 */
import java.util.Map;

public class EspeceFlore implements Espece {
    private final String nom; // Nom de l'espèce de faune
    private final Map<Biome, Integer> compatibilite; // Compatibilité avec les biomes
    private final TYPE_VEGETAL typeVegetal; // Type de végétal


    public enum TYPE_VEGETAL { // Types de végétaux
        ARBRE,
        FLEUR,
        ARBUSTE,
        PLANTE_GRASSE,
        FONGIQUE,
        MOUSSE
    }

    /* 
     * Constructeur de la classe Flore.
     * @param p_nom Nom de l'espèce de flore.
     * @param p_compatibilite Compatibilité avec les biomes (sous forme de Map).
     * @param p_typeVegetal Type de végétal (sous forme d'énumération).
     */
    public EspeceFlore(String p_nom, Map<Biome, Integer> p_compatibilite, TYPE_VEGETAL p_typeVegetal) {
        this.nom = p_nom;
        this.compatibilite = p_compatibilite;
        this.typeVegetal = p_typeVegetal;
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
     * @return le type de l'espèce (flore).
     */
    @Override
    public String getType() {
        return "flore";
    }

    /*
     * Méthode pour obtenir le type de végétal.
     * @return le type de végétal (sous forme d'énumération).
     */
    public TYPE_VEGETAL getTypeVegetal() {
        return typeVegetal;
    }

    /*
     * Méthode pour obtenir la compatibilité avec les biomes.
     * @return la compatibilité avec les biomes (sous forme de Map).
     */
    @Override
    public Map<Biome, Integer> getCompatibilite() {
        return compatibilite;
    }


    /*
     * Méthode pour afficher la représentation sous forme de chaîne de l'espèce.
     * @return la représentation sous forme de chaîne de l'espèce.
     */
    @Override
    public String toString() {
        return "[Flore] " + nom + " - Compatibilité avec les biomes : " + compatibilite.toString()
                + " - Type de Végétal : " + typeVegetal.toString() ;
    }
}
