package voxel.model;

/**
 * Classe gérant les biomes basés sur trois paramètres environnementaux.
 * Chaque biome est défini par une combinaison unique de température, humidité et complexité du relief.
 */
public class BiomeType {
    
    /** Paramètres du biome */
    private final int temperature;     // 0-4 : Glacial, Froid, Tempéré, Chaud, Torride
    private final int humidity;        // 0-4 : Aride, Sec, Modéré, Humide, Saturé
    private final int reliefComplexity; // 0-4 : Plat, Doux, Modéré, Accidenté, Montagneux
    
    /** Nom descriptif du biome */
    private final String name;
    
    /** Types de blocs caractéristiques de ce biome */
    private final BlockType surfaceBlock;
    private final BlockType subSurfaceBlock;
    private final BlockType deepBlock;
    private final BlockType waterBlock;
    
    /** Constantes pour les biomes spéciaux */
    public static final BiomeType FLOATING_ISLAND = new BiomeType(-1, -1, -1, "Île Flottante", 
        BlockType.GRASS, BlockType.DIRT, BlockType.STONE, BlockType.WATER);
    
    /**
     * Constructeur pour un biome personnalisé.
     */
    private BiomeType(int temperature, int humidity, int reliefComplexity, String name,
                     BlockType surfaceBlock, BlockType subSurfaceBlock, BlockType deepBlock, BlockType waterBlock) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.reliefComplexity = reliefComplexity;
        this.name = name;
        this.surfaceBlock = surfaceBlock;
        this.subSurfaceBlock = subSurfaceBlock;
        this.deepBlock = deepBlock;
        this.waterBlock = waterBlock;
    }
    
    /**
     * Crée un biome basé sur les paramètres environnementaux.
     */
    public static BiomeType createBiome(int temperature, int humidity, int reliefComplexity) {
        // Validation des paramètres
        temperature = Math.max(0, Math.min(4, temperature));
        humidity = Math.max(0, Math.min(4, humidity));
        reliefComplexity = Math.max(0, Math.min(4, reliefComplexity));
        
        // Détection des biomes spéciaux
        String specialBiomeName = detectSpecialBiome(temperature, humidity, reliefComplexity);
        
        // Génération du nom du biome
        String biomeName;
        if (specialBiomeName != null) {
            biomeName = specialBiomeName;
        } else {
            String tempName = getTemperatureName(temperature);
            String humidityName = getHumidityName(humidity);
            String reliefName = getReliefName(reliefComplexity);
            biomeName = tempName + " " + humidityName + " " + reliefName;
        }
        
        // Détermination des types de blocs selon les paramètres
        BlockType surfaceBlock = determineSurfaceBlock(temperature, humidity, reliefComplexity);
        BlockType subSurfaceBlock = determineSubSurfaceBlock(temperature, humidity, reliefComplexity);
        BlockType deepBlock = determineDeepBlock(temperature, reliefComplexity);
        BlockType waterBlock = determineWaterBlock(temperature, humidity, reliefComplexity);
        
        return new BiomeType(temperature, humidity, reliefComplexity, biomeName,
                           surfaceBlock, subSurfaceBlock, deepBlock, waterBlock);
    }
    
    /**
     * Détecte si les paramètres correspondent à un biome spécial.
     */
    private static String detectSpecialBiome(int temperature, int humidity, int reliefComplexity) {
        // Arctique : température glaciale + humidité faible + relief plat
        if (temperature == 0 && humidity == 1 && reliefComplexity == 0) {
            return "Arctique";
        }
        
        // Jungle : température chaude + humidité saturée
        if (temperature >= 3 && humidity >= 4) {
            return "Jungle Tropicale";
        }
        
        // Marécage : température tempérée/chaude + humidité saturée + relief plat/doux
        if (temperature >= 2 && temperature <= 3 && humidity >= 4 && reliefComplexity <= 1) {
            return "Marécage";
        }
        
        // Mangrove : température chaude + humidité saturée + relief plat
        if (temperature >= 3 && humidity >= 4 && reliefComplexity == 0) {
            return "Mangrove";
        }
        
        // Désert brûlant : température torride + humidité aride/sec
        if (temperature >= 4 && humidity <= 1) {
            return "Désert Brûlant";
        }
        
        // Toundra : température glaciale + humidité faible/modérée + relief plat/doux
        if (temperature <= 1 && humidity >= 1 && humidity <= 2 && reliefComplexity <= 1) {
            return "Toundra";
        }
        
        // Volcanique : température torride + relief accidenté/montagneux
        if (temperature >= 4 && reliefComplexity >= 3) {
            return "Région Volcanique";
        }
        
        // Oasis : température chaude/torride + humidité élevée + relief plat (rare)
        if (temperature >= 3 && humidity >= 3 && reliefComplexity == 0) {
            return "Oasis";
        }
        
        return null; // Pas de biome spécial détecté
    }
    
    /**
     * Détermine le bloc de surface selon la température, l'humidité et le relief.
     */
    private static BlockType determineSurfaceBlock(int temperature, int humidity, int reliefComplexity) {
        // Biomes spéciaux en priorité
        
        // Arctique spécial : température glaciale + humidité faible + relief plat = NEIGE
        if (temperature == 0 && humidity == 1 && reliefComplexity == 0) {
            return BlockType.SNOW;
        }
        
        // Jungle
        if (temperature >= 3 && humidity >= 4) {
            return BlockType.JUNGLE_GRASS;
        }
        
        // Marécage
        if (temperature >= 2 && temperature <= 3 && humidity >= 4 && reliefComplexity <= 1) {
            return BlockType.SWAMP_GRASS;
        }
        
        // Désert brûlant
        if (temperature >= 4 && humidity <= 1) {
            if (reliefComplexity <= 1) {
                return BlockType.RED_SAND;
            } else {
                return BlockType.ASH; // Désert volcanique
            }
        }
        
        // Toundra
        if (temperature <= 1 && humidity >= 1 && humidity <= 2 && reliefComplexity <= 1) {
            return BlockType.TUNDRA_GRASS;
        }
        
        // Oasis
        if (temperature >= 3 && humidity >= 3 && reliefComplexity == 0) {
            return BlockType.TROPICAL_GRASS;
        }
        
        // Logique standard par température
        if (temperature == 0) {
            if (humidity <= 1) return BlockType.PERMAFROST;
            else if (humidity <= 3) return BlockType.FROZEN_GRASS;
            else return BlockType.SNOW;
        }
        else if (temperature == 1) {
            if (humidity <= 1) return BlockType.FROZEN_GRASS;
            else if (humidity <= 2) return BlockType.ALPINE_GRASS;
            else if (humidity <= 3) return BlockType.MOSS;
            else return BlockType.PEAT;
        }
        else if (temperature == 2) {
            if (humidity <= 1) return BlockType.DRY_GRASS;
            else if (humidity <= 2) return BlockType.GRASS;
            else if (humidity <= 3) return BlockType.PRAIRIE_GRASS;
            else return BlockType.MUD;
        }
        else if (temperature == 3) {
            if (humidity <= 1) return BlockType.SAND;
            else if (humidity <= 2) return BlockType.SAVANNA_GRASS;
            else if (humidity <= 3) return BlockType.TROPICAL_GRASS;
            else return BlockType.JUNGLE_GRASS;
        }
        else { // temperature == 4
            if (humidity <= 1) return BlockType.RED_SAND;
            else if (humidity <= 2) return BlockType.ASH;
            else if (humidity <= 3) return BlockType.VOLCANIC_SOIL;
            else return BlockType.MUD;
        }
    }
    
    /**
     * Détermine le bloc de sous-surface selon la température, l'humidité et le relief.
     */
    private static BlockType determineSubSurfaceBlock(int temperature, int humidity, int reliefComplexity) {
        // Biomes spéciaux
        if (temperature >= 3 && humidity >= 4) {
            // Jungle ou marécage
            return BlockType.RICH_SOIL;
        }
        
        if (temperature >= 4 && humidity <= 1) {
            // Désert brûlant
            return BlockType.SANDSTONE;
        }
        
        if (temperature >= 4 && reliefComplexity >= 3) {
            // Volcanique
            return BlockType.VOLCANIC_SOIL;
        }
        
        // Logique standard
        if (temperature <= 1 && humidity >= 3) return BlockType.PERMAFROST;
        else if (temperature >= 4 && humidity <= 1) return BlockType.SANDSTONE;
        else if (humidity >= 4) return BlockType.PEAT;
        else if (humidity <= 1) return BlockType.CLAY;
        else return BlockType.DIRT;
    }
    
    /**
     * Détermine le bloc profond selon la température et le relief.
     */
    private static BlockType determineDeepBlock(int temperature, int reliefComplexity) {
        if (temperature >= 4 && reliefComplexity >= 3) return BlockType.BASALT;
        else if (temperature <= 1 && reliefComplexity >= 3) return BlockType.GRANITE;
        else if (reliefComplexity >= 4) return BlockType.SLATE;
        else if (reliefComplexity >= 2) return BlockType.LIMESTONE;
        else return BlockType.STONE;
    }
    
    /**
     * Détermine le type d'eau selon la température, l'humidité et le relief.
     */
    private static BlockType determineWaterBlock(int temperature, int humidity, int reliefComplexity) {
        // Jungle : eau bleu foncé
        if (temperature >= 3 && humidity >= 4) {
            return BlockType.JUNGLE_WATER;
        }
        
        // Marécage : eau verte
        if (temperature >= 2 && temperature <= 3 && humidity >= 4 && reliefComplexity <= 1) {
            return BlockType.SWAMP_WATER;
        }
        
        // Désert brûlant : eau orange
        if (temperature >= 4 && humidity <= 1) {
            return BlockType.DESERT_WATER;
        }
        
        // Sources thermales (zones volcaniques)
        if (temperature >= 4 && reliefComplexity >= 3) {
            return BlockType.MINERAL_WATER;
        }
        
        // Eau selon la température standard
        if (temperature == 0) return BlockType.ICE;
        else if (temperature == 1) return BlockType.COLD_WATER;
        else if (temperature >= 4) return BlockType.WARM_WATER;
        else return BlockType.WATER;
    }
    
    /**
     * Retourne le nom de la température.
     */
    private static String getTemperatureName(int temperature) {
        switch (temperature) {
            case 0: return "Glacial";
            case 1: return "Froid";
            case 2: return "Tempéré";
            case 3: return "Chaud";
            case 4: return "Torride";
            default: return "Inconnu";
        }
    }
    
    /**
     * Retourne le nom de l'humidité.
     */
    private static String getHumidityName(int humidity) {
        switch (humidity) {
            case 0: return "Aride";
            case 1: return "Sec";
            case 2: return "Modéré";
            case 3: return "Humide";
            case 4: return "Saturé";
            default: return "Inconnu";
        }
    }
    
    /**
     * Retourne le nom du relief.
     */
    private static String getReliefName(int relief) {
        switch (relief) {
            case 0: return "Plat";
            case 1: return "Doux";
            case 2: return "Vallonné";
            case 3: return "Accidenté";
            case 4: return "Montagneux";
            default: return "Inconnu";
        }
    }
    
    /**
     * Vérifie si ce biome est une jungle.
     */
    public boolean isJungle() {
        return temperature >= 3 && humidity >= 4;
    }
    
    /**
     * Vérifie si ce biome est un marécage.
     */
    public boolean isSwamp() {
        return temperature >= 2 && temperature <= 3 && humidity >= 4 && reliefComplexity <= 1;
    }
    
    /**
     * Vérifie si ce biome est un désert brûlant.
     */
    public boolean isHotDesert() {
        return temperature >= 4 && humidity <= 1;
    }
    
    /**
     * Vérifie si ce biome est volcanique.
     */
    public boolean isVolcanic() {
        return temperature >= 4 && reliefComplexity >= 3;
    }
    
    /**
     * Vérifie si ce biome est une toundra.
     */
    public boolean isTundra() {
        return temperature <= 1 && humidity >= 1 && humidity <= 2 && reliefComplexity <= 1;
    }
    
    /**
     * Vérifie si ce biome est arctique.
     */
    public boolean isArctic() {
        return temperature == 0 && humidity == 1 && reliefComplexity == 0;
    }
    
    // Getters
    public int getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public int getReliefComplexity() { return reliefComplexity; }
    public String getName() { return name; }
    public BlockType getSurfaceBlock() { return surfaceBlock; }
    public BlockType getSubSurfaceBlock() { return subSurfaceBlock; }
    public BlockType getDeepBlock() { return deepBlock; }
    public BlockType getWaterBlock() { return waterBlock; }
    
    /**
     * Vérifie si c'est le biome île flottante.
     */
    public boolean isFloatingIsland() {
        return this == FLOATING_ISLAND;
    }
    
    @Override
    public String toString() {
        return name + " (T:" + temperature + ", H:" + humidity + ", R:" + reliefComplexity + ")";
    }
}
