
/*
 * Espece.java
 * Interface représentant une espèce (faune ou flore).
 */
import java.util.Map;

public interface Espece {
    String getNom(); // Nom de l'espèce

    Map<Biome, Integer> getCompatibilite(); // Compatibilité avec les biomes

    public String getType(); // Type de l'espèce (faune ou flore)

    // Obtenir la compatibilité et la note pour un biome spécifique
    default int getCompatibilite(Biome biome) {
        return getCompatibilite().getOrDefault(biome, 0); // Renvoie 0 si le biome n'est pas trouvé
    }

    // Obtenir la note de compatibilité sous forme (chaîne)
    default String getNote(Biome biome) {
        int score = getCompatibilite(biome);
        String note;
        if (score >= 70) {
            note = "Adapté";
        } else if (score >= 40) {
            note = "Risque";
        } else {
            note = "Inadapté";
        }
        return note;
    }

    @Override
    String toString(); // Représentation sous forme de chaîne de l'espèce
}