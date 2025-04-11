
/**
 * Biome.java
 * Classe représentant un biome.
 */
public class Biome {

    public enum TypeRelief {
        PLAT, COLLINE, MONTAGNEUX, VALLONNÉ
    }

    // Attributs caractéristiques du biome
    private final double humidite;             // Entre 0.0 et 1.0
    private final double vegetation;           // Densité végétale, entre 0.0 et 1.0
    private final double temperature;          // En degrés Celsius
    private final TypeRelief relief;


    /*
     * Constructeur de la classe Biome.
     * 
     * @param humidite    Humidité du biome (entre 0.0 et 1.0)
     * @param vegetation  Densité végétale (entre 0.0 et 1.0)
     * @param temperature Température en degrés Celsius
     * @param relief      Type de relief (PLAT, COLLINE, MONTAGNEUX, VALLONNÉ)
     */
    public Biome(double humidite, double vegetation, double temperature, TypeRelief relief) {
        this.humidite = verifierPlage(humidite);
        this.vegetation = verifierPlage(vegetation);
        this.temperature = temperature;
        this.relief = relief;
    }

    /*
     * Verifie si la valeur est dans la plage [0.0, 1.0]
     */
    private double verifierPlage(double valeur) {
        if (valeur < 0.0) return 0.0;
        if (valeur > 1.0) return 1.0;
        return valeur;
    }

    /*
     * Accesseurs pour les attributs du biome
     */
    public double getHumidite() {
        return humidite;
    }

    /*
     * Accesseur pour l'humidité du biome
     */
    public double getVegetation() {
        return vegetation;
    }

    /*
     * Accesseur pour la végétation du biome
     */
    public double getTemperature() {
        return temperature;
    }

    /*
     * Accesseur pour la température du biome
     */
    public TypeRelief getRelief() {
        return relief;
    }

    @Override
    public String toString() {
        return "Biome : " + "\n"
                + "- Humidité : " + (humidite * 100) + "%\n"
                + "- Végétation : " + (vegetation * 100) + "%\n"
                + "- Température : " + temperature + "°C\n"
                + "- Relief : " + relief.name();
    }
}