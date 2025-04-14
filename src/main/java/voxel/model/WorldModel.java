package voxel.model;

import voxel.model.entity.EntityManager;
import java.util.Random;

/**
 * Représente le monde de voxels complet, composé de plusieurs chunks.
 * Cette classe gère uniquement les données du monde sans le rendu.
 */
public class WorldModel {
    /** Taille du monde en nombre de chunks sur les axes X et Z */
    public static final int WORLD_SIZE = 32;
    
    /** Tableau 3D contenant tous les chunks du monde */
    private ChunkModel[][][] chunks;
    
    /** Taille du monde en nombre de chunks sur l'axe X */
    private final int worldSizeX = WORLD_SIZE;
    
    /** Taille du monde en nombre de chunks sur l'axe Y */
    private final int worldSizeY = 8;
    
    /** Taille du monde en nombre de chunks sur l'axe Z */
    private final int worldSizeZ = WORLD_SIZE;
    
    /** Eclairage oui ou non */
    private boolean lightningMode = true;
    
    /** Mode filaire activé ou non */
    private boolean wireframeMode = false;

    private EntityManager entityManager;

    /** Générateur de nombres aléatoires */
    private final Random random = new Random();

    /** Valeurs pour definir l'echelle des montagne et des details dans le bruit de Perlin */
    private final int generation_height = 6; // Hauteur max de la génération avec Perlin
    private final float min_mountain = 0.001f;
    private final float max_mountain = 0.03f;  // Réduit pour des montagnes moins abruptes
    private final float min_detail = 0.02f;
    private final float max_detail = 0.06f;    // Réduit pour des détails moins prononcés

    /** Bruit de Perlin pour le monde entier */
    private PerlinNoise worldPerlinNoise;

    /** Échelles pour la génération du terrain */
    private float mountainScale;
    private float detailScale;

    /** Hauteur maximale en pourcentage de la hauteur totale du monde */
    private final float maxHeightPercent = 0.75f;

    /** Décalage vertical de base pour tout le terrain */
    private final int baseOffset = 10;

    /**
     * Crée un nouveau monde de voxels.
     */
    public WorldModel() {
        chunks = new ChunkModel[worldSizeX][worldSizeY][worldSizeZ];

        // Initialisation du bruit de Perlin pour tout le monde
        worldPerlinNoise = new PerlinNoise(42); // Seed fixe pour reproductibilité

        // Initialisation des échelles (on pourrait les randomiser aussi)
        java.util.Random rand = new java.util.Random();
        mountainScale = min_mountain + rand.nextFloat() * (max_mountain - min_mountain);
        detailScale = min_detail + rand.nextFloat() * (max_detail - min_detail);

        generateWorld();
        entityManager = new EntityManager(this);
    }

    /**
     * Génère le monde complet avec tous ses chunks.
     */
    private void generateWorld() {
        // Créer tous les chunks vides
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cy = 0; cy < worldSizeY; cy++) {
                for (int cz = 0; cz < worldSizeZ; cz++) {
                    chunks[cx][cy][cz] = new ChunkModel(true); // Créer des chunks vides
                }
            }
        }

        // Génération du terrain uniquement sur le plan X-Z
        for (int cx = 0; cx < worldSizeX; cx++) {
            for (int cz = 0; cz < worldSizeZ; cz++) {
                generateTerrainPerlin(cx, cz);
            }
        }

        createFloatingIsland();
    }

    /**
     * Génère un terrain avec un bruit de Perlin pour une colonne de chunks
     * @param chunkX coordonnée X du chunk
     * @param chunkZ coordonnée Z du chunk
     */
    private void generateTerrainPerlin(int chunkX, int chunkZ) {
        // Niveau de l'eau
        int waterLevel = 50;

        // Coordonnées globales du chunk
        float worldXStart = chunkX * ChunkModel.SIZE - (float) (worldSizeX *
                ChunkModel.SIZE) / 2;
        float worldZStart = chunkZ * ChunkModel.SIZE - (float) (worldSizeZ *
                ChunkModel.SIZE) / 2;

        // Hauteur totale disponible
        int totalHeight = generation_height * ChunkModel.SIZE;
        // Hauteur maximale pour le terrain (avec marge pour éviter les coupures)
        int maxTerrainHeight = (int)(totalHeight * maxHeightPercent);

        // Générer chaque colonne de blocs
        for (int x = 0; x < ChunkModel.SIZE; x++) {
            for (int z = 0; z < ChunkModel.SIZE; z++) {
                // Coordonnées globales pour ce bloc spécifique
                float worldX = worldXStart + x;
                float worldZ = worldZStart + z;

                // Appliquer un bruit Perlin 2D pour obtenir la hauteur globale des montagnes
                float mountainNoise = worldPerlinNoise.Noise2D(worldX * mountainScale, worldZ * mountainScale);

                // Ajouter des détails avec un autre bruit Perlin
                float detailNoise = worldPerlinNoise.Noise2D(worldX * detailScale, worldZ * detailScale);

                // Combiner les bruits pour avoir une hauteur réaliste
                float heightFactor = mountainNoise * 0.7f + detailNoise * 0.3f;

                // Appliquer une courbe d'élévation pour accentuer les différences de hauteur
                heightFactor = (float)Math.pow(heightFactor, 1.2);

                // Calculer la hauteur du terrain pour cette colonne
                // Limiter à maxTerrainHeight pour éviter que le terrain touche le haut du monde
                int baseTerrainHeight = baseOffset + (int)(heightFactor * (maxTerrainHeight - baseOffset));

                // Assurer une hauteur minimale
                int terrainHeight = Math.max(waterLevel - 5, baseTerrainHeight);

                // Limiter la hauteur maximale
                terrainHeight = Math.min(maxTerrainHeight, terrainHeight);

                // Générer les blocs pour chaque coordonnée Y
                for (int y = 0; y < totalHeight; y++) {
                    // Déterminer le type de bloc en fonction de la hauteur
                    int blockType;

                    if (y <= waterLevel) {
                        if (y < terrainHeight) {
                            // Sous le terrain et sous l'eau
                            if (y < terrainHeight - 3) {
                                blockType = BlockType.STONE.getId();
                            } else {
                                blockType = BlockType.DIRT.getId();
                            }

                            // Si on est près de la surface du terrain et sous l'eau, mettre du sable
                            if (y > terrainHeight - 4 && y == terrainHeight - 1) {
                                blockType = BlockType.SAND.getId();
                            }
                        } else {
                            // Au-dessus du terrain mais sous l'eau -> eau
                            blockType = BlockType.WATER.getId();

                            // Le fond de l'eau est du sable
                            if (y == terrainHeight) {
                                blockType = BlockType.SAND.getId();
                            }
                        }
                    } else if (y < terrainHeight) {
                        // Au-dessus du niveau d'eau, sous la surface du terrain
                        if (y < terrainHeight - 3) {
                            blockType = BlockType.STONE.getId();
                        } else if (y < terrainHeight - 1) {
                            blockType = BlockType.DIRT.getId();
                        } else {
                            // Surface : herbe
                            blockType = BlockType.GRASS.getId();
                        }
                    } else {
                        // Au-dessus du terrain, air
                        blockType = BlockType.AIR.getId();
                    }

                    // Placer le bloc au bon endroit
                    setBlockAt((int) worldX, y, (int) worldZ, blockType);
                }
            }
        }
    }

    /**
     * Crée une île flottante au centre du monde (0,0,0).
     */
    private void createFloatingIsland() {

        int centerX = 0;
        int centerY = 150;
        int centerZ = 0;

        // Rayon de l'île
        int radiusXZ = 30; // Rayon horizontal
        int radiusY = 16;   // Hauteur/épaisseur

        // Générer la base de l'île
        for (int x = centerX - radiusXZ; x <= centerX + radiusXZ; x++) {
            for (int z = centerZ - radiusXZ; z <= centerZ + radiusXZ; z++) {

                double distanceSquared = Math.pow((x - centerX) / (radiusXZ * 0.8), 2)
                                      + Math.pow((z - centerZ) / (radiusXZ * 0.8), 2);

                // Si dans le rayon de l'île
                if (distanceSquared <= 1.0) {
                    // Déterminer la hauteur à cette position
                    double heightFactor = 1 - Math.sqrt(distanceSquared);
                    int height = (int) (radiusY * heightFactor);

                    // Ajouter une variation aléatoire aux bords
                    if (distanceSquared > 0.6) {
                        height += random.nextInt(3) - 1;
                    }

                    for (int y = centerY - height; y <= centerY; y++) {

                        if (y == centerY) {
                            setBlockAt(x, y, z, BlockType.GRASS.getId());
                        } else if (y > centerY - 3) {
                            setBlockAt(x, y, z, BlockType.DIRT.getId());
                        } else {
                            setBlockAt(x, y, z, BlockType.STONE.getId());
                        }
                    }
                }
            }
        }

        // Ajouter quelques arbres
        addTrees(centerX, centerY, centerZ, radiusXZ);

        // Ajouter des nuages dans le ciel
        addClouds(centerX, centerY + 30, centerZ);
    }

    /**
     * Ajoute des nuages dans le ciel du monde.
     *
     * @param centerX Le centre du monde en X
     * @param cloudY L'altitude des nuages
     * @param centerZ Le centre du monde en Z
     */
    private void addClouds(int centerX, int cloudY, int centerZ) {
        // Positions fixes des nuages (X, Z, altitude relative, taille)
        int[][] cloudPositions = {
            {0, 0, 0, 12},
            {30, 30, -5, 10},
            {-40, 25, 8, 8},
            {40, -35, -3, 9},
            {-30, -40, 5, 7},
            {50, 0, 10, 11},
            {-60, 0, -6, 10},
            {0, 50, 4, 9}
        };

        // Créer des nuages aux positions fixes
        for (int[] position : cloudPositions) {
            int cloudX = centerX + position[0];
            int cloudZ = centerZ + position[1];
            int thisCloudY = cloudY + position[2];  // Ajout de la variation d'altitude
            int cloudSize = position[3];            // Taille du nuage

            // Dimensions du nuage basées sur sa taille
            int cloudSizeX = cloudSize;
            int cloudSizeY = 2 + (cloudSize / 5);
            int cloudSizeZ = cloudSize + random.nextInt(3) - 1; // Légère variation

            createCloud(cloudX, thisCloudY, cloudZ, cloudSizeX, cloudSizeY, cloudSizeZ);
        }
    }

    /**
     * Crée un nuage à la position et aux dimensions spécifiées.
     */
    private void createCloud(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {

        // Créer le nuage avec une forme arrondie
        for (int dx = 0; dx < sizeX; dx++) {
            for (int dy = 0; dy < sizeY; dy++) {
                for (int dz = 0; dz < sizeZ; dz++) {
                    // Calculer la distance normalisée au centre (forme ellipsoïdale)
                    double distanceSquared =
                        Math.pow((dx - sizeX/2.0) / (sizeX * 0.5), 2) +
                        Math.pow((dy - sizeY/2.0) / (sizeY * 0.7), 2) +
                        Math.pow((dz - sizeZ/2.0) / (sizeZ * 0.5), 2);

                    double noise = random.nextDouble() * 0.3;

                    // Si le point est dans l'ellipsoïde du nuage
                    if (distanceSquared + noise <= 1.0) {
                        if (distanceSquared > 0.7 && random.nextDouble() > 0.7) {
                            continue;
                        }

                        // Convertir les coordonnées centrées en coordonnées de grille
                        int gridX = x + dx;
                        int gridY = y + dy;
                        int gridZ = z + dz;

                        // Placer le bloc de nuage
                        setBlockAt(gridX, gridY, gridZ, BlockType.CLOUD.getId());
                    }
                }
            }
        }
    }

    /**
     * Ajoute quelques arbres sur l'île.
     */
    private void addTrees(int centerX, int centerY, int centerZ, int radiusXZ) {

        // Positions fixes des arbres (angles en degrés, distance en pourcentage du radius)
        int[][] treePositions = {
            {45, 40},
            {225, 45},
            {315, 55},
            {0, 0},
            {90, 35},
            {180, 55},
            {270, 45}
        };

        // Créer les arbres aux positions fixes
        for (int[] position : treePositions) {
            int angleDegrees = position[0];
            int distancePercent = position[1];

            // Convertir l'angle en radians
            double angleRadians = Math.toRadians(angleDegrees);
            double distance = (distancePercent / 100.0) * radiusXZ;

            int treeX = centerX + (int)(Math.cos(angleRadians) * distance);
            int treeZ = centerZ + (int)(Math.sin(angleRadians) * distance);

            // Vérifier que l'emplacement est de l'herbe (utilise maintenant les coordonnées de grille)
            if (getBlockAt(treeX, centerY, treeZ) == BlockType.GRASS.getId()) {
                createTree(treeX, centerY + 1, treeZ);
            }
        }
    }

    /**
     * Crée un arbre à la position spécifiée.
     */
    private void createTree(int x, int y, int z) {

        int height = 4 + random.nextInt(6); // Hauteur du tronc entre 4 et 10

        // Créer le tronc
        for (int i = 0; i < height; i++) {
            // Convertir en coordonnées de grille
            setBlockAt(x, y + i, z, BlockType.LOG.getId());
        }

        // Créer le feuillage
        int leavesRadius = 3;
        for (int dx = -leavesRadius; dx <= leavesRadius; dx++) {
            for (int dy = -leavesRadius; dy <= leavesRadius + 1; dy++) {
                for (int dz = -leavesRadius; dz <= leavesRadius; dz++) {
                    // Distance au centre du feuillage
                    double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);

                    // Si dans le rayon des feuilles et pas trop près du sol
                    if (distance <= leavesRadius + 0.5 && y + height + dy > y) {
                        int blockX = x + dx;
                        int blockY = y + height + dy;
                        int blockZ = z + dz;

                        // Ne pas remplacer le tronc
                        if (!(dx == 0 && dz == 0 && dy < 0)) {
                            if (distance < leavesRadius || random.nextDouble() < 0.6) {
                                setBlockAt(blockX, blockY, blockZ, BlockType.LEAVES.getId());
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Récupère le type de bloc à partir de coordonnées globales.
     * Convertit les coordonnées globales en coordonnées de chunk et locales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @return L'identifiant du type de bloc, ou 0 (AIR) si en dehors du monde
     */
    public int getBlockAt(int globalX, int globalY, int globalZ) {
        // Calcul des coordonnées du chunk
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            return BlockType.AIR.getId(); // AIR pour tout ce qui est en dehors du monde
        }

        // Récupération du type de bloc dans le chunk
        return chunks[cx][cy][cz].getBlock(localX, localY, localZ);
    }
    
    /**
     * Modifie le type de bloc à partir de coordonnées globales.
     * 
     * @param globalX Coordonnée globale X
     * @param globalY Coordonnée globale Y
     * @param globalZ Coordonnée globale Z
     * @param blockType Identifiant du type de bloc
     * @return true si le bloc a été modifié, false si hors des limites
     */
    public boolean setBlockAt(int globalX, int globalY, int globalZ, int blockType) {
        // Calcul des coordonnées du chunk sans décalage
        int chunkX = Math.floorDiv(globalX, ChunkModel.SIZE);
        int chunkY = Math.floorDiv(globalY, ChunkModel.SIZE);
        int chunkZ = Math.floorDiv(globalZ, ChunkModel.SIZE);

        // Calcul des coordonnées locales à l'intérieur du chunk
        int localX = globalX - chunkX * ChunkModel.SIZE;
        int localY = globalY - chunkY * ChunkModel.SIZE;
        int localZ = globalZ - chunkZ * ChunkModel.SIZE;

        // Appliquer le décalage pour le stockage dans le tableau de chunks
        int cx = chunkX + worldSizeX / 2;
        int cy = chunkY;
        int cz = chunkZ + worldSizeZ / 2;

        // Vérification que les coordonnées sont dans les limites du monde
        if (cx < 0 || cx >= worldSizeX || cy < 0 || cy >= worldSizeY || cz < 0 || cz >= worldSizeZ) {
            System.out.println("Bloc : " + BlockType.fromId(blockType) + " hors des limites du monde, globalX: " + globalX + ", globalY: " + globalY + ", globalZ: " + globalZ);
            return false;
        }

        // Modification du bloc dans le chunk
        chunks[cx][cy][cz].setBlock(localX, localY, localZ, blockType);
        return true;
    }

    /**
     * Active ou désactive le mode filaire.
     * 
     * @return Le nouvel état du mode filaire
     */
    public boolean toggleWireframe() {
        wireframeMode = !wireframeMode;
        return wireframeMode;
    }

    /**
     * Active ou désactive l'éclairage.
     * 
     * @return Le nouvel état de l'éclairage
     */
    public boolean toggleLightning() {
        lightningMode = !lightningMode;
        return lightningMode;
    }

    /**
     * Récupère l'état actuel du mode d'éclairage.
     * 
     * @return true si l'éclairage est activé, false sinon
     */
    public boolean getLightningMode() {
        return lightningMode;
    }

    /**
     * Récupère l'état actuel du mode filaire.
     * 
     * @return true si le mode filaire est activé, false sinon
     */
    public boolean getWireframeMode() {
        return wireframeMode;
    }

    /**
     * Récupère le chunk aux coordonnées spécifiées.
     * 
     * @param chunkX Position X du chunk
     * @param chunkY Position Y du chunk
     * @param chunkZ Position Z du chunk
     * @return Le chunk à cette position, ou null si hors limites
     */
    public ChunkModel getChunk(int chunkX, int chunkY, int chunkZ) {
        if (chunkX >= 0 && chunkX < worldSizeX && 
            chunkY >= 0 && chunkY < worldSizeY && 
            chunkZ >= 0 && chunkZ < worldSizeZ) {
            return chunks[chunkX][chunkY][chunkZ];
        }
        return null;
    }

    /**
     * Récupère la taille du monde en X.
     */
    public int getWorldSizeX() {
        return worldSizeX;
    }

    /**
     * Récupère la taille du monde en Y.
     */
    public int getWorldSizeY() {
        return worldSizeY;
    }

    /**
     * Récupère la taille du monde en Z.
     */
    public int getWorldSizeZ() {
        return worldSizeZ;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void update(float tpf) {
        // Mettre à jour toutes les entités
        if (entityManager != null) {
            entityManager.updateAll(tpf);
        }
    }
}