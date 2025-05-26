package voxel.controller;

import com.jme3.renderer.ViewPort;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import voxel.model.BiomeType;
import voxel.model.BlockType;
import voxel.model.ChunkModel;
import voxel.model.WorldModel;
import voxel.model.structure.Structure;
import voxel.model.structure.StructureManager;
import voxel.model.structure.plant.BasicTree;
import voxel.view.WorldRenderer;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import voxel.controller.GameStateManager;

/**
 * Contrôleur qui gère les interactions entre le modèle de monde et sa vue.
 * Coordonne les modifications et met à jour le rendu en conséquence.
 */
public class WorldController {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Référence au renderer du monde */
    private final WorldRenderer worldRenderer;
    
    /** Gestionnaire des structures */
    private final StructureManager structureManager;
    
    /** Générateur de nombres aléatoires pour la placement des structures */
    private final Random random;
    
    /** Référence au gestionnaire d'états pour la vitesse du temps */
    private GameStateManager gameStateManager;
    
    /** Timer pour attendre 10 secondes avant de générer les premiers arbres */
    private float initialTreeTimer = 0f;
    private boolean initialTreesGenerated = false;
    private final float INITIAL_TREE_DELAY = 10f; // 10 secondes
    
    /** Nombre maximum d'arbres pouvant coexister */
    private final int MAX_TREES = 20;
    
    /** Timer pour la génération continue d'arbres */
    private float treeGenerationTimer = 0f;
    private final float TREE_GENERATION_INTERVAL = 300f; // Essayer de générer un arbre toutes les 5 minutes (très rare)

    /**
     * Crée un nouveau contrôleur pour le monde.
     * 
     * @param worldModel Le modèle du monde à contrôler
     * @param worldRenderer Le renderer du monde
     */
    public WorldController(WorldModel worldModel, WorldRenderer worldRenderer) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
        this.structureManager = new StructureManager();
        this.random = new Random();
        
        // Ne plus générer les arbres immédiatement, attendre 10 secondes
        System.out.println("Attente de " + INITIAL_TREE_DELAY + " secondes avant la génération des premiers arbres...");
    }
    
    /**
     * Gère la croissance d'une structure.
     * @param structure La structure qui a grandi
     */
    private void handleStructureGrowth(Structure structure) {
        if (structure instanceof BasicTree) {
            BasicTree tree = (BasicTree) structure;
//            System.out.println("Régénération de l'arbre grandi à la position (" +
//                             tree.getWorldX() + ", " + tree.getWorldY() + ", " + tree.getWorldZ() + ")");
            
            // Régénérer le nouvel arbre plus grand
            generateTree(tree, tree.getWorldX(), tree.getWorldY(), tree.getWorldZ());
        }
    }
    
    /**
     * Fait disparaître un arbre mature et plante 0 à 3 nouveaux arbres proches.
     * @param matureTree L'arbre mature à faire disparaître
     */
    private void handleMatureTreeDisappearance(BasicTree matureTree) {
        int worldX = matureTree.getWorldX();
        int worldY = matureTree.getWorldY();
        int worldZ = matureTree.getWorldZ();
        
//        System.out.println("Arbre mature disparaît à la position (" + worldX + ", " + worldY + ", " + worldZ + ")");
        
        // Effacer l'arbre du monde
        clearTreeFromWorld(matureTree);
        
        // Supprimer l'arbre du gestionnaire de structures
        structureManager.removeStructure(matureTree);
        
        // Planter 1 à 4 nouveaux arbres proches (favoriser la propagation)
        int numberOfNewTrees = 1 + random.nextInt(4); // 1 à 4
//        System.out.println("Plantation de " + numberOfNewTrees + " nouveaux arbres proches...");
        
        for (int i = 0; i < numberOfNewTrees; i++) {
            plantNearbyTree(worldX, worldY, worldZ);
        }
    }
    
    /**
     * Efface un arbre du monde en remplaçant ses blocs par de l'air.
     * @param tree L'arbre à effacer
     */
    private void clearTreeFromWorld(BasicTree tree) {
        int[][][] treeBlocks = tree.getBlocks();
        int width = tree.getWidth();
        int height = tree.getHeight();
        int worldX = tree.getWorldX();
        int worldY = tree.getWorldY();
        int worldZ = tree.getWorldZ();
        
        Set<Vector3f> chunksToUpdate = new HashSet<>();
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    int blockType = treeBlocks[x][y][z];
                    if (blockType != -1) {
                        int blockX = worldX - (width / 2) + x;
                        int blockY = worldY + y;
                        int blockZ = worldZ - (width / 2) + z;
                        
                        // Remplacer par de l'air si c'est un bloc de l'arbre
                        int currentBlock = worldModel.getBlockAt(blockX, blockY, blockZ);
                        int currentStructureId = worldModel.getStructureIdAt(blockX, blockY, blockZ);
                        
                        if (currentStructureId == tree.getStructureId() && 
                            (currentBlock == BlockType.LOG.getId() || currentBlock == BlockType.LEAVES.getId())) {
                            worldModel.setBlockAt(blockX, blockY, blockZ, BlockType.AIR.getId(), 0);
                            
                            // Marquer le chunk pour mise à jour
                            Vector3f chunkCoords = worldModel.getChunkCoordAt(blockX, blockY, blockZ);
                            chunksToUpdate.add(chunkCoords);
                        }
                    }
                }
            }
        }
        
        // Marquer tous les chunks affectés pour mise à jour
        for (Vector3f chunkCoord : chunksToUpdate) {
            ChunkModel chunk = worldModel.getChunk((int)chunkCoord.x, (int)chunkCoord.y, (int)chunkCoord.z);
            if (chunk != null) {
                chunk.setNeedsUpdate(true);
            }
        }
    }
    
    /**
     * Plante un nouvel arbre proche d'une position donnée.
     * @param centerX Position X de référence
     * @param centerY Position Y de référence
     * @param centerZ Position Z de référence
     */
    private void plantNearbyTree(int centerX, int centerY, int centerZ) {
        int maxAttempts = 20;
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            attempts++;
            
            // Générer une position proche (dans un rayon de 10 à 40 blocs)
            int radius = 10 + random.nextInt(30); // 10 à 40
            double angle = random.nextDouble() * Math.PI * 2;
            
            int worldX = centerX + (int)(Math.cos(angle) * radius);
            int worldZ = centerZ + (int)(Math.sin(angle) * radius);
            
            // Trouver la hauteur du sol à cette position
            int groundHeight = worldModel.getGroundHeightAt(worldX, worldZ);
            
            if (groundHeight == -1) continue;
            
            // Vérifier que ce n'est pas de l'eau
            int surfaceBlockId = worldModel.getBlockAt(worldX, groundHeight - 1, worldZ);
            if (BlockType.isWaterBlock(surfaceBlockId)) continue;
            
            // Vérifier que l'arbre peut pousser sur ce type de bloc (pas sur feuilles ou logs)
            if (surfaceBlockId == BlockType.LOG.getId() || surfaceBlockId == BlockType.LEAVES.getId()) {
                continue; // Ne peut pas pousser sur des feuilles ou des logs
            }
            
            // Vérifier l'espace disponible
            boolean hasSpace = true;
            for (int y = groundHeight; y < groundHeight + 12; y++) {
                int blockAbove = worldModel.getBlockAt(worldX, y, worldZ);
                if (blockAbove != BlockType.AIR.getId() && !BlockType.isWaterBlock(blockAbove)) {
                    hasSpace = false;
                    break;
                }
            }
            
            if (!hasSpace) continue;
            
            // Créer et planter le nouvel arbre
            int treeWidth = 5 + random.nextInt(6);
            int treeHeight = 4 + random.nextInt(8);
            
            BasicTree newTree = new BasicTree(treeWidth, treeHeight);
            newTree.setWorldPosition(worldX, groundHeight, worldZ);
            
            structureManager.addStructure(newTree);
            generateTree(newTree, worldX, groundHeight, worldZ);
            
//            System.out.println("Nouvel arbre planté à la position (" + worldX + ", " + groundHeight + ", " + worldZ + ")");
            return;
        }
        
        System.out.println("Impossible de planter un nouvel arbre près de (" + centerX + ", " + centerY + ", " + centerZ + ") après " + maxAttempts + " tentatives");
    }

    /**
     * Génère des arbres à des positions aléatoires dans le monde, en évitant l'eau.
     * @param numberOfTrees Nombre d'arbres à générer
     */
    private void generateRandomTrees(int numberOfTrees) {
        int treesGenerated = 0;
        int maxAttempts = 100; // Limiter les tentatives pour éviter une boucle infinie
        int attempts = 0;
        
        // Calculer les limites du monde en blocs
        int worldSizeXBlocks = worldModel.getWorldSizeX() * ChunkModel.SIZE;
        int worldSizeZBlocks = worldModel.getWorldSizeZ() * ChunkModel.SIZE;
        int centerOffsetX = worldSizeXBlocks / 2;
        int centerOffsetZ = worldSizeZBlocks / 2;
        
        System.out.println("Génération de " + numberOfTrees + " arbres aléatoires dans le monde...");
        
        while (treesGenerated < numberOfTrees && attempts < maxAttempts) {
            attempts++;
            
            // Générer des coordonnées aléatoires dans le monde
            int worldX = random.nextInt(worldSizeXBlocks) - centerOffsetX;
            int worldZ = random.nextInt(worldSizeZBlocks) - centerOffsetZ;
            
            // Trouver la hauteur du sol à cette position
            int groundHeight = worldModel.getGroundHeightAt(worldX, worldZ);
            
            // Vérifier si une position valide a été trouvée
            if (groundHeight == -1) {
                continue; // Pas de sol trouvé, essayer une autre position
            }
            
            // Vérifier que ce n'est pas de l'eau à la surface
            int surfaceBlockId = worldModel.getBlockAt(worldX, groundHeight - 1, worldZ);
            if (BlockType.isWaterBlock(surfaceBlockId)) {
                continue; // C'est de l'eau, essayer une autre position
            }
            
            // Vérifier que l'arbre peut pousser sur ce type de bloc (pas sur feuilles ou logs)
            if (surfaceBlockId == BlockType.LOG.getId() || surfaceBlockId == BlockType.LEAVES.getId()) {
                continue; // Ne peut pas pousser sur des feuilles ou des logs
            }
            
            // Vérifier qu'il y a assez d'espace au-dessus pour l'arbre
            int maxTreeHeight = 12; // Hauteur maximale possible d'un arbre
            boolean hasSpace = true;
            for (int y = groundHeight; y < groundHeight + maxTreeHeight; y++) {
                int blockAbove = worldModel.getBlockAt(worldX, y, worldZ);
                if (blockAbove != BlockType.AIR.getId() && !BlockType.isWaterBlock(blockAbove)) {
                    hasSpace = false;
                    break;
                }
            }
            
            if (!hasSpace) {
                continue; // Pas assez d'espace, essayer une autre position
            }
            
            // Générer une taille aléatoire pour l'arbre
            int treeWidth = 5 + random.nextInt(6); // Entre 5 et 10
            int treeHeight = 4 + random.nextInt(8); // Entre 4 et 11
            
            // Créer l'arbre et définir sa position
            BasicTree tree = new BasicTree(treeWidth, treeHeight);
            tree.setWorldPosition(worldX, groundHeight, worldZ);
            
            // Ajouter l'arbre au gestionnaire de structures
            structureManager.addStructure(tree);
            
            // Générer l'arbre dans le monde
            generateTree(tree, worldX, groundHeight, worldZ);
            
            treesGenerated++;
//            System.out.println("Arbre " + treesGenerated + " généré à la position (" + worldX + ", " + groundHeight + ", " + worldZ + ") - Taille: " + treeWidth + "x" + treeHeight);
        }
        
        if (treesGenerated < numberOfTrees) {
            System.out.println("Attention: Seulement " + treesGenerated + " arbres ont pu être générés après " + attempts + " tentatives.");
        } else {
            System.out.println("Génération des arbres terminée : " + treesGenerated + " arbres créés.");
        }
    }

    /**
     * Active ou désactive le mode filaire pour tous les chunks.
     */
    public void toggleWireframe() {
        boolean newMode = worldModel.toggleWireframe();
        System.out.println("Wireframe: " + (newMode ? "activé" : "désactivé"));
        worldRenderer.applyWireframeModeToMaterials(); 
    }

    /**
     * Active ou désactive l'éclairage pour tous les chunks.
     */
    public void toggleLightning() {
        boolean newMode = worldModel.toggleLightning();
        System.out.println("Lightning: " + (newMode ? "activé" : "désactivé"));
        worldRenderer.updateAllMeshes();
    }

    /**
     * Active ou désactive l'affichage des coordonnées.
     * 
     * @param display true pour afficher les coordonnées, false pour les masquer
     */
    public void toggleCoordinatesDisplay(boolean display) {
        worldRenderer.setDisplayCoordinates(display);
    }

    public void generateTree(BasicTree tree, int worldX, int worldY, int worldZ){
        int[][][] treeBlocks = tree.getBlocks();
        int width = tree.getWidth();
        int height = tree.getHeight();
        int treeId = tree.getStructureId(); // Récupérer l'ID de cette structure

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < width; z++) {
                    int blockType = treeBlocks[x][y][z];
                    if (blockType != -1) {

                        int blockX = worldX-(width/2) + x;
                        int blockY = worldY + y;
                        int blockZ = worldZ-(width/2) + z;

                        // Pose le nouveau bloc pour construire l'arbre
                        int blockBefore = worldModel.getBlockAt(blockX, blockY, blockZ);
                        int structureIdBefore = worldModel.getStructureIdAt(blockX, blockY, blockZ);

                        if (blockType != blockBefore) {
                            // Vérifier que nous pouvons placer le bloc
                            boolean canPlace = false;
                            
                            if (blockBefore == BlockType.AIR.getId()) {
                                // On peut toujours placer dans l'air
                                canPlace = true;
                            } else if ((blockBefore == BlockType.LOG.getId() || blockBefore == BlockType.LEAVES.getId())) {
                                // On peut seulement remplacer LOG/LEAVES si c'est de la même structure ou pas de structure
                                canPlace = (structureIdBefore == 0 || structureIdBefore == treeId);
                            }
                            
                            if (canPlace) {
                                boolean modified = worldModel.setBlockAt(blockX, blockY, blockZ, blockType, treeId);

                                if (modified){
                                    try {
                                        Vector3f chunkCoords = worldModel.getChunkCoordAt(blockX, blockY, blockZ);
                                        int cx = (int) chunkCoords.x;
                                        int cy = (int) chunkCoords.y;
                                        int cz = (int) chunkCoords.z;

                                        // Indique que le chunk doit être rechargé (vérification sécurisée)
                                        ChunkModel currentChunk = worldModel.getChunk(cx, cy, cz);
                                        if (currentChunk != null) {
                                            currentChunk.setNeedsUpdate(true);

                                            int localX = worldX - (cx - worldModel.getWorldSizeX() / 2) * ChunkModel.SIZE;
                                            int localY = worldY - cy * ChunkModel.SIZE;
                                            int localZ = worldZ - (cz - worldModel.getWorldSizeZ() / 2) * ChunkModel.SIZE;

                                            // Si on est en bordure d'un chunk, mettre à jour les chunks voisins
                                            if (localX == 0) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx-1, cy, cz);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                            if (localX == 15) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx+1, cy, cz);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                            if (localY == 0) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx, cy-1, cz);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                            if (localY == 15) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx, cy+1, cz);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                            if (localZ == 0) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx, cy, cz-1);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                            if (localZ == 15) {
                                                ChunkModel neighborChunk = worldModel.getChunk(cx, cy, cz+1);
                                                if (neighborChunk != null) neighborChunk.setNeedsUpdate(true);
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.err.println("Erreur lors de la génération d'arbre à la position (" + blockX + ", " + blockY + ", " + blockZ + "): " + e.getMessage());
                                        // Continue sans planter le jeu
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateNeededChunks() {
        int compteurChunkUpdated = 0;
        for (int cx = 0; cx < worldModel.getWorldSizeX(); cx++) {
            for (int cy = 0; cy < worldModel.getWorldSizeY(); cy++) {
                for (int cz = 0; cz < worldModel.getWorldSizeZ(); cz++) {
                    ChunkModel chunk = worldModel.getChunk(cx, cy, cz);
                    if (chunk.getNeedsUpdate()) {
                        worldRenderer.updateChunkMesh(cx, cy, cz);
                        chunk.setNeedsUpdate(false);
                        compteurChunkUpdated++;
                    }
                }
            }
        }
        if (compteurChunkUpdated > 0) {
            System.out.println("Nombre de chunks mis à jour: " + compteurChunkUpdated);
        }
    }

    /**
     * Modifie un bloc à une position donnée.
     * 
     * @param x Coordonnée X du bloc
     * @param y Coordonnée Y du bloc
     * @param z Coordonnée Z du bloc
     * @param blockType Type de bloc à placer
     * @return true si le bloc a été modifié, false sinon
     */
    public boolean modifyBlock(int x, int y, int z, BlockType blockType) {
        boolean modified = worldModel.setBlockAt(x, y, z, blockType.getId());

        if (modified) {
            // Calcul des coordonnées du chunk sans décalage
            int chunkX = Math.floorDiv(x, ChunkModel.SIZE);
            int chunkY = Math.floorDiv(y, ChunkModel.SIZE);
            int chunkZ = Math.floorDiv(z, ChunkModel.SIZE);

            // Calcul des coordonnées locales à l'intérieur du chunk
            int localX = x - chunkX * ChunkModel.SIZE;
            int localY = y - chunkY * ChunkModel.SIZE;
            int localZ = z - chunkZ * ChunkModel.SIZE;

            // Appliquer le décalage pour le stockage dans le tableau de chunks
            int cx = chunkX + worldModel.getWorldSizeX() / 2;
            int cy = chunkY;
            int cz = chunkZ + worldModel.getWorldSizeZ() / 2;

            System.out.println("Chunk modifié: " + cx + ", " + cy + ", " + cz);

            // Mettre à jour le maillage du chunk
            worldRenderer.updateChunkMesh(cx, cy, cz);

            // Si on est en bordure d'un chunk, mettre à jour les chunks voisins
            if (localX == 0) worldRenderer.updateChunkMesh(chunkX - 1, chunkY, chunkZ);
            if (localX == 15) worldRenderer.updateChunkMesh(chunkX + 1, chunkY, chunkZ);
            if (localY == 0) worldRenderer.updateChunkMesh(chunkX, chunkY - 1, chunkZ);
            if (localY == 15) worldRenderer.updateChunkMesh(chunkX, chunkY + 1, chunkZ);
            if (localZ == 0) worldRenderer.updateChunkMesh(chunkX, chunkY, chunkZ - 1);
            if (localZ == 15) worldRenderer.updateChunkMesh(chunkX, chunkY, chunkZ + 1);
        }
        
        return modified;
    }

    /**
     * Définit le gestionnaire d'états du jeu pour accéder à la vitesse du temps de l'environnement
     * 
     * @param gameStateManager Le gestionnaire d'états du jeu
     */
    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Met à jour le contrôleur et le modèle à chaque frame.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf, ViewPort mainViewport) {
        worldRenderer.update(tpf, mainViewport);
        updateNeededChunks();
        
        // Utiliser la vitesse de l'environnement pour la croissance des arbres
        float environmentSpeed = (gameStateManager != null) ? gameStateManager.getEnvironmentTimeSpeed() : 1.0f;
        float adjustedTpf = tpf * environmentSpeed;
        
        // Gérer la génération initiale des arbres après 10 secondes
        if (!initialTreesGenerated) {
            initialTreeTimer += adjustedTpf;
            if (initialTreeTimer >= INITIAL_TREE_DELAY) {
                initialTreesGenerated = true;
                if (worldModel.getActiveBiome() != BiomeType.FLOATING_ISLAND) {
                    generateRandomTrees(3); // Générer 3 arbres initialement
                }
            }
        }
        
        // Génération spontanée très rare d'arbres (seulement si très peu d'arbres restants)
        if (initialTreesGenerated && structureManager.getStructureCount() < 2) {
            treeGenerationTimer += adjustedTpf;
            if (treeGenerationTimer >= TREE_GENERATION_INTERVAL) {
                treeGenerationTimer = 0f;
                // Chance très faible de génération spontanée (5%)
                if (random.nextFloat() < 0.05f && worldModel.getActiveBiome() != BiomeType.FLOATING_ISLAND) {
                    generateRandomTrees(1);
                    System.out.println("Génération spontanée rare d'un arbre (moins de 2 arbres restants)");
                }
            }
        }
        
        // Mettre à jour toutes les structures avec le temps ajusté et récupérer celles qui ont grandi
        List<Structure> grownStructures = structureManager.updateAll(adjustedTpf);
        
        // Gérer la croissance des structures
        for (Structure structure : grownStructures) {
            handleStructureGrowth(structure);
        }
        
        // Gérer la disparition des arbres matures
        List<BasicTree> treesToRemove = new ArrayList<>();
        for (Structure structure : structureManager.getStructures()) {
            if (structure instanceof BasicTree) {
                BasicTree tree = (BasicTree) structure;
                
                // Vérifier si l'arbre est mature (a atteint sa taille maximale)
                if (!tree.canGrowInSize()) {
                    // L'arbre attend un temps aléatoire avant de disparaître (10-30 secondes)
                    if (!tree.hasMaturityTimer()) {
                        tree.setMaturityTimer(10f + random.nextFloat() * 20f); // 10 à 30 secondes
                    }
                    
                    tree.updateMaturityTimer(adjustedTpf);
                    
                    if (tree.isReadyToDisappear()) {
                        treesToRemove.add(tree);
                    }
                }
            }
        }
        
        // Traiter tous les arbres qui doivent disparaître (séparément de l'itération)
        for (BasicTree tree : treesToRemove) {
            handleMatureTreeDisappearance(tree);
        }
    }

    /**
     * Retourne le modèle du monde (WorldModel).
     */
    public WorldModel getWorldModel() {
        return worldModel;
    }
    
    /**
     * Retourne le gestionnaire de structures.
     * @return Le StructureManager
     */
    public StructureManager getStructureManager() {
        return structureManager;
    }
} 