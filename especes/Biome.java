
/**
 * Biome.java
 * Classe représentant un biome.
 */
import java.util.Map;

public class Biome {
    private final BIOME nom; // Nom du biome

    public enum BIOME { // Types de biomes
        FORET,
        DESERT,
        PRAIRIE,
        MONTAGNE
    }

    /**
     * Constructeur de la classe Biome.
     * @param p_nom Nom du biome (sous forme d'énumération).
     */
    public Biome(BIOME p_nom) {
        this.nom = p_nom;
}
}