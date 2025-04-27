package voxel.model;

import com.jme3.math.ColorRGBA;

/**
 * Énumération représentant les différents types de blocs disponibles dans le monde voxel.
 * Chaque type de bloc possède un identifiant unique et une couleur associée.
 */
public enum BlockType {

    AIR(0, new ColorRGBA(1, 1, 1, 1)), // Bloc d'air (transparent)
    GRASS(1, new ColorRGBA(0.15f, 0.8f, 0.15f, 1)), // Bloc d'herbe
    STONE(2, new ColorRGBA(0.5f, 0.5f, 0.5f, 1)), // Bloc de pierre
    DIRT(3, new ColorRGBA(0.5f, 0.3f, 0.2f, 1)), // Bloc de terre
    SAND(4, new ColorRGBA(0.98f, 0.929f, 0.772f, 1)), // Bloc de sable
    WATER(5, new ColorRGBA(0.2f, 0.2f, 0.7f, 0.5f)), // Bloc d'eau (semi-transparent)
    LOG(6, new ColorRGBA(0.6f, 0.3f, 0.1f, 1)), // Bloc de bois
    LEAVES(7, new ColorRGBA(0.2f, 0.7f, 0.2f, 1)), // Bloc de feuillage
    SAVANNA_GRASS(8, new ColorRGBA(0.741f, 0.717f, 0.333f, 1)), // Bloc d'herbe de savane
    JUNGLE_GRASS(9, new ColorRGBA(0.349f, 0.788f, 0.235f, 1)), // Bloc d'herbe de jungle
    SNOW(10, new ColorRGBA(1.0f, 1.0f, 1.0f, 1)), // Bloc de neige
    CLOUD(11, new ColorRGBA(1.0f, 1.0f, 1.0f, 0.8f)); // Bloc de nuage (semi-transparent)


    /** Identifiant unique du type de bloc */
    private final int id;
    
    /** Couleur associée au type de bloc */
    private final ColorRGBA color;

    /**
     * Constructeur de l'énumération.
     * 
     * @param id Identifiant unique du type de bloc
     * @param color Couleur associée au type de bloc
     */
    BlockType(int id, ColorRGBA color) {
        this.id = id;
        this.color = color;
    }

    /**
     * Récupère l'identifiant du type de bloc.
     * 
     * @return L'identifiant unique
     */
    public int getId() {
        return id;
    }

    /**
     * Récupère la couleur associée au type de bloc.
     * 
     * @return La couleur RGBA
     */
    public ColorRGBA getColor() {
        return color;
    }

    /**
     * Récupère un type de bloc à partir de son identifiant.
     * 
     * @param id L'identifiant du bloc recherché
     * @return Le type de bloc correspondant, ou AIR si aucun bloc ne correspond
     */
    public static BlockType fromId(int id) {
        for (BlockType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return AIR;
    }
} 