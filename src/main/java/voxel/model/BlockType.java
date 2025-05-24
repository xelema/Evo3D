package voxel.model;

import com.jme3.math.ColorRGBA;

/**
 * Énumération représentant les différents types de blocs disponibles dans le monde voxel.
 * Chaque type de bloc possède un identifiant unique et une couleur associée.
 */
public enum BlockType {

    INVISIBLE(-2, new ColorRGBA(0, 0, 0, 0)), // Bloc invisible (utilisé pour les collisions)
    VOID(-1, new ColorRGBA(0, 0, 0, 0)), // Bloc vide (invisible);
    AIR(0, new ColorRGBA(1, 1, 1, 1)), // Bloc d'air (transparent)
    GRASS(1, new ColorRGBA(0.15f, 0.8f, 0.15f, 1)), // Bloc d'herbe
    STONE(2, new ColorRGBA(0.5f, 0.5f, 0.5f, 1)), // Bloc de pierre
    DIRT(3, new ColorRGBA(0.5f, 0.3f, 0.2f, 1)), // Bloc de terre
    SAND(4, new ColorRGBA(0.98f, 0.929f, 0.772f, 1)), // Bloc de sable
    WATER(5, new ColorRGBA(0.2f, 0.2f, 0.7f, 0.5f)), // Bloc d'eau (semi-transparent)
    LOG(6, new ColorRGBA(0.6f, 0.3f, 0.1f, 1)), // Bloc de bois
    LEAVES(7, new ColorRGBA(0.2f, 0.7f, 0.2f, 1)), // Bloc de feuillage
    SAVANNA_GRASS(8, new ColorRGBA(0.741f, 0.717f, 0.333f, 1)), // Bloc d'herbe de savane
    JUNGLE_GRASS(9, new ColorRGBA(0.1f, 0.6f, 0.1f, 1)), // Bloc d'herbe de jungle (vert foncé humide)
    SNOW(10, new ColorRGBA(1.0f, 1.0f, 1.0f, 1)), // Bloc de neige
    CLOUD(11, new ColorRGBA(1.0f, 1.0f, 1.0f, 0.6f)), // Bloc de nuage (semi-transparent)
    
    // Nouveaux blocs pour les biomes avancés
    
    // Terrain glacé/froid
    ICE(12, new ColorRGBA(0.7f, 0.9f, 1.0f, 0.8f)), // Glace (semi-transparente)
    FROZEN_GRASS(13, new ColorRGBA(0.6f, 0.8f, 0.9f, 1)), // Herbe gelée
    PERMAFROST(14, new ColorRGBA(0.843f, 0.91f, 0.929f, 1)), // Permafrost
    
    // Terrain désertique
    RED_SAND(15, new ColorRGBA(0.9f, 0.6f, 0.4f, 1)), // Sable rouge
    CACTUS(16, new ColorRGBA(0.2f, 0.6f, 0.2f, 1)), // Cactus
    SANDSTONE(17, new ColorRGBA(0.8f, 0.7f, 0.5f, 1)), // Grès
    
    // Terrain tropical/humide
    MUD(18, new ColorRGBA(0.2f, 0.15f, 0.1f, 1)), // Boue (plus foncée)
    TROPICAL_GRASS(19, new ColorRGBA(0.1f, 0.9f, 0.3f, 1)), // Herbe tropicale
    SWAMP_WATER(20, new ColorRGBA(0.15f, 0.4f, 0.2f, 0.6f)), // Eau de marécage (verte)
    PEAT(21, new ColorRGBA(0.2f, 0.15f, 0.1f, 1)), // Tourbe
    
    // Terrain volcanique/chaud
    LAVA(22, new ColorRGBA(1.0f, 0.3f, 0.0f, 1)), // Lave
    OBSIDIAN(23, new ColorRGBA(0.1f, 0.05f, 0.1f, 1)), // Obsidienne
    BASALT(24, new ColorRGBA(0.2f, 0.2f, 0.25f, 1)), // Basalte
    ASH(25, new ColorRGBA(0.4f, 0.4f, 0.4f, 1)), // Cendres
    
    // Terrain de haute altitude
    GRANITE(26, new ColorRGBA(0.6f, 0.55f, 0.5f, 1)), // Granit
    SLATE(27, new ColorRGBA(0.3f, 0.35f, 0.4f, 1)), // Ardoise
    ALPINE_GRASS(28, new ColorRGBA(0.4f, 0.7f, 0.4f, 1)), // Herbe alpine
    
    // Terrain de plaines/prairies
    PRAIRIE_GRASS(29, new ColorRGBA(0.5f, 0.8f, 0.3f, 1)), // Herbe de prairie
    CLAY(30, new ColorRGBA(0.7f, 0.5f, 0.4f, 1)), // Argile
    
    // Terrain rocheux/aride
    LIMESTONE(31, new ColorRGBA(0.8f, 0.8f, 0.7f, 1)), // Calcaire
    SHALE(32, new ColorRGBA(0.4f, 0.4f, 0.5f, 1)), // Schiste
    DRY_GRASS(33, new ColorRGBA(0.8f, 0.7f, 0.4f, 1)), // Herbe sèche
    
    // Variations d'eau selon la température et biome
    COLD_WATER(34, new ColorRGBA(0.1f, 0.3f, 0.8f, 0.6f)), // Eau froide
    WARM_WATER(35, new ColorRGBA(0.3f, 0.5f, 0.8f, 0.5f)), // Eau chaude
    JUNGLE_WATER(36, new ColorRGBA(0.1f, 0.2f, 0.5f, 0.6f)), // Eau de jungle (bleu foncé)
    DESERT_WATER(37, new ColorRGBA(0.6f, 0.4f, 0.2f, 0.5f)), // Eau de désert (légèrement orange)
    
    // Végétation variée et nouveaux blocs spéciaux
    DEAD_GRASS(38, new ColorRGBA(0.6f, 0.4f, 0.2f, 1)), // Herbe morte
    MOSS(39, new ColorRGBA(0.2f, 0.5f, 0.2f, 1)), // Mousse
    LICHEN(40, new ColorRGBA(0.5f, 0.6f, 0.3f, 1)), // Lichen
    
    // Nouveaux blocs pour enrichir les biomes
    SWAMP_GRASS(41, new ColorRGBA(0.25f, 0.478f, 0.11f, 1)), // Herbe de marécage
    MANGROVE_ROOTS(42, new ColorRGBA(0.4f, 0.2f, 0.1f, 1)), // Racines de mangrove
    RICH_SOIL(43, new ColorRGBA(0.2f, 0.1f, 0.05f, 1)), // Sol riche (jungle/marécage)
    CORAL_SAND(44, new ColorRGBA(0.95f, 0.85f, 0.7f, 1)), // Sable corallien
    VOLCANIC_SOIL(45, new ColorRGBA(0.3f, 0.2f, 0.1f, 1)), // Sol volcanique
    TUNDRA_GRASS(46, new ColorRGBA(0.5f, 0.6f, 0.4f, 1)), // Herbe de toundra
    QUICKSAND(47, new ColorRGBA(0.8f, 0.7f, 0.5f, 0.8f)), // Sables mouvants (semi-transparent)
    MINERAL_WATER(48, new ColorRGBA(0.4f, 0.6f, 0.7f, 0.7f)); // Eau minérale (sources thermales)

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
     * Vérifie si ce bloc est de l'eau (tous types confondus).
     * 
     * @return true si c'est un type d'eau, false sinon
     */
    public boolean isWater() {
        return this == WATER || this == COLD_WATER || this == WARM_WATER || 
               this == SWAMP_WATER || this == JUNGLE_WATER || this == DESERT_WATER ||
               this == MINERAL_WATER;
    }

    /**
     * Vérifie si ce bloc est transparent ou semi-transparent.
     * 
     * @return true si le bloc est transparent, false sinon
     */
    public boolean isTransparent() {
        return color.a < 1.0f;
    }

    /**
     * Vérifie si ce bloc est solide (non-air, non-eau).
     * 
     * @return true si le bloc est solide, false sinon
     */
    public boolean isSolid() {
        return this != AIR && this != VOID && this != INVISIBLE && !isWater() && this != QUICKSAND;
    }

    /**
     * Vérifie si ce bloc est de la végétation.
     * 
     * @return true si c'est un type de végétation, false sinon
     */
    public boolean isVegetation() {
        return this == GRASS || this == JUNGLE_GRASS || this == SAVANNA_GRASS ||
               this == TROPICAL_GRASS || this == PRAIRIE_GRASS || this == DRY_GRASS ||
               this == DEAD_GRASS || this == ALPINE_GRASS || this == FROZEN_GRASS ||
               this == SWAMP_GRASS || this == TUNDRA_GRASS || this == MOSS || this == LICHEN;
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

    /**
     * Méthode utilitaire statique pour vérifier si un ID correspond à de l'eau.
     * 
     * @param blockId L'identifiant du bloc à vérifier
     * @return true si c'est un type d'eau, false sinon
     */
    public static boolean isWaterBlock(int blockId) {
        BlockType type = fromId(blockId);
        return type.isWater();
    }

    /**
     * Méthode utilitaire statique pour vérifier si un ID correspond à un bloc transparent.
     * 
     * @param blockId L'identifiant du bloc à vérifier
     * @return true si le bloc est transparent, false sinon
     */
    public static boolean isTransparentBlock(int blockId) {
        BlockType type = fromId(blockId);
        return type.isTransparent();
    }
} 