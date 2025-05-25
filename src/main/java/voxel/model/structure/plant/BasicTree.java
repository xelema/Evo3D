package voxel.model.structure.plant;

import voxel.model.BlockType;
import voxel.model.structure.Structure;
import java.util.Random;

public class BasicTree extends Structure {
    private Random random;
    
    // Système de maturité et disparition
    private float maturityTimer = -1f; // -1 signifie pas encore mature
    private boolean hasMaturityTimer = false;
    
    public BasicTree(int width, int height) {
        super(width, height, width);
        random = new Random();
        // Configurer les paramètres de croissance pour les arbres
        this.growthInterval = 3.0f; // 3.0 seconde entre les tentatives
        this.growthProbability = 0.3f; // 30% de chance de grandir
        this.maxWidth = 40;
        this.maxHeight = 35;
        createBasicTree();
    }

    public void createBasicTree() {
        fillWithVoid();
        // Utiliser une approche différente selon la taille
        if (width <= 5 || height <= 5) {
            createTinyTree();
        } else if (width <= 10 || height <= 10) {
            createSmallTree();
        } else {
            createLargeTree();
        }
    }
    
    /**
     * Crée un arbre minuscule avec une structure très simple mais élégante
     */
    private void createTinyTree() {
        // Pour les arbres miniatures: un tronc d'un bloc et une couronne de feuilles élégante
        int tinyTrunkHeight = Math.max(1, height - 2);
        
        // Créer un tronc simple
        for (int y = 0; y < tinyTrunkHeight; y++) {
            setBlockSafe(0, y, 0, BlockType.LOG.getId());
        }
        
        // Créer une couronne de feuilles en forme de diamant
        int leafY = tinyTrunkHeight;
        
        // Feuilles niveau inférieur (plus large)
        if (leafY < height) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (Math.abs(x) + Math.abs(z) <= 2) {
                        setBlockSafe(x, leafY, z, BlockType.LEAVES.getId());
                    }
                }
            }
            leafY++;
        }
        
        // Feuilles niveau supérieur (plus étroit)
        if (leafY < height) {
            setBlockSafe(0, leafY, 0, BlockType.LEAVES.getId());
            setBlockSafe(1, leafY, 0, BlockType.LEAVES.getId());
            setBlockSafe(-1, leafY, 0, BlockType.LEAVES.getId());
            setBlockSafe(0, leafY, 1, BlockType.LEAVES.getId());
            setBlockSafe(0, leafY, -1, BlockType.LEAVES.getId());
        }
    }
    
    /**
     * Crée un arbre de petite taille avec une forme plus élaborée
     */
    private void createSmallTree() {
        // Tronc
        int trunkHeight = Math.max(2, (int)(height * 0.6));
        
        for (int y = 0; y < trunkHeight; y++) {
            setBlockSafe(0, y, 0, BlockType.LOG.getId());
        }
        
        // Feuillage en forme de ballon
        int leafRadius = Math.max(2, width / 3);
        
        for (int y = trunkHeight - 1; y < height; y++) {
            // Forme ovale plus large au milieu
            double heightPercent = (double)(y - (trunkHeight - 1)) / (height - (trunkHeight - 1));
            double radiusFactor = 1.0 - Math.pow(2 * heightPercent - 1, 2);
            int currentRadius = Math.max(1, (int)(leafRadius * radiusFactor));
            
            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    double distance = Math.sqrt(x*x + z*z);
                    
                    // Forme arrondie avec bords aléatoires
                    if (distance <= currentRadius) {
                        if (distance < currentRadius - 0.5 || random.nextDouble() < 0.6) {
                            // Éviter d'écraser le tronc
                            if (!(x == 0 && z == 0 && y < trunkHeight)) {
                                setBlockSafe(x, y, z, BlockType.LEAVES.getId());
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Crée un grand arbre avec du détail
     */
    private void createLargeTree() {
        // Calculer les proportions basées sur la hauteur et la largeur
        int trunkHeight = Math.max(2, (int)(height * 0.7));
        int trunkRadius = Math.max(1, width / 10);
        int canopyRadius = Math.max(2, width / 3);
        
        // S'assurer que le canopy n'est pas trop grand pour la structure
        canopyRadius = Math.min(canopyRadius, (width / 2) - 1);
        
        // Créer le tronc
        createTrunk(trunkHeight, trunkRadius);
        
        // Créer des branches seulement si l'arbre est assez grand
        if (height > 7) {
            createBranches(trunkHeight, trunkRadius, canopyRadius);
        }
        
        // Créer le feuillage au sommet
        createCanopy(trunkHeight, canopyRadius);
    }
    
    private void createTrunk(int trunkHeight, int trunkRadius) {
        // Limite la taille du tronc pour éviter les out of bounds
        trunkRadius = Math.min(trunkRadius, (width / 2) - 1);
        
        // Générer le tronc avec une épaisseur variable
        for (int y = 0; y < trunkHeight; y++) {
            // Plus épais en bas, progressivement plus mince en haut
            int currentRadius = Math.max(1, (trunkRadius * (trunkHeight - y)) / Math.max(1, trunkHeight));
            
            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    // Placer uniquement les blocs dans un cercle approximatif
                    if (x*x + z*z <= currentRadius*currentRadius + 1) {
                        // Faire la partie intérieure du tronc
                        if (y < trunkHeight - 1 || (x*x + z*z <= (currentRadius-1)*(currentRadius-1) + 1)) {
                            setBlockSafe(x, y, z, BlockType.LOG.getId());
                        }
                    }
                }
            }
        }
    }
    
    private void createBranches(int trunkHeight, int trunkRadius, int canopyRadius) {
        // Le nombre de branches augmente avec la hauteur de l'arbre, minimum 1
        int numBranches = Math.max(1, height / 5);
        
        // Commencer les branches à environ 40% de la hauteur du tronc
        int startHeight = Math.max(1, trunkHeight * 2 / 5);
        
        for (int i = 0; i < numBranches; i++) {
            // Distribuer les branches le long du tronc
            int branchY = startHeight + ((trunkHeight - startHeight) * i) / Math.max(1, numBranches);
            
            // Angle aléatoire pour la branche
            double angle = random.nextDouble() * Math.PI * 2;
            
            // La longueur de la branche est proportionnelle au rayon de la canopée, mais limitée pour éviter les dépassements
            int maxBranchLength = (width / 2) - 2; // laisser de l'espace depuis le bord
            int branchLength = Math.min(maxBranchLength, Math.max(1, canopyRadius - 1));
            
            // Créer la branche
            for (int j = 0; j < branchLength; j++) {
                int x = (int)(Math.cos(angle) * j);
                int z = (int)(Math.sin(angle) * j);
                
                // La branche s'incline légèrement vers le haut
                int y = branchY + j / 3;
                
                // Vérifier que y reste dans les limites
                if (y < height) {
                    setBlockSafe(x, y, z, BlockType.LOG.getId());
                    
                    // Ajouter de petits groupes de feuilles le long des branches
                    if (j > branchLength / 2) {
                        addLeafCluster(x, y, z, 1);
                    }
                }
            }
        }
    }
    
    private void createCanopy(int trunkHeight, int canopyRadius) {
        // Limiter le rayon du feuillage pour éviter out of bounds
        canopyRadius = Math.min(canopyRadius, (width / 2) - 1);
        
        // Créer une grande boule de feuillage au sommet du tronc
        int foliageStart = Math.max(1, (int)(trunkHeight * 0.6)); // Commencer le feuillage à 60% de la hauteur du tronc
        
        for (int y = foliageStart; y < height; y++) {
            // Déterminer le rayon du feuillage à cette hauteur
            // Forme d'ellipsoïde - plus large au milieu, plus étroite en haut et en bas
            double heightPercent = (double)(y - foliageStart) / Math.max(1, (height - foliageStart));
            double radiusFactor = 1.0 - Math.pow(2 * heightPercent - 1, 2);
            int currentRadius = Math.max(1, (int)(canopyRadius * radiusFactor));
            
            // S'assurer que le rayon ne dépasse pas les limites
            currentRadius = Math.min(currentRadius, (width / 2) - 1);
            
            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    double distance = Math.sqrt(x*x + z*z);
                    
                    // Créer une sphère approximative de feuilles
                    if (distance <= currentRadius) {
                        // Ajouter un peu d'aléatoire sur les bords
                        if (distance <= currentRadius - 1 || random.nextDouble() < 0.7) {
                            // Ne pas écraser le tronc
                            if (!(Math.abs(x) <= 1 && Math.abs(z) <= 1 && y < trunkHeight)) {
                                setBlockSafe(x, y, z, BlockType.LEAVES.getId());
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void addLeafCluster(int centerX, int centerY, int centerZ, int radius) {
        // Limiter le rayon du cluster pour éviter les out of bounds
        radius = Math.min(radius, Math.min((width / 2) - Math.abs(centerX) - 1, (width / 2) - Math.abs(centerZ) - 1));
        radius = Math.max(radius, 1); // Au moins 1
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    // Vérifier que y reste dans les limites
                    if (y >= 0 && y < height) {
                        // Calculer la distance depuis le centre
                        double distance = Math.sqrt(
                            Math.pow(x - centerX, 2) + 
                            Math.pow(y - centerY, 2) + 
                            Math.pow(z - centerZ, 2)
                        );
                        
                        // Créer une sphère approximative de feuilles
                        if (distance <= radius && random.nextDouble() < 0.8) {
                            setBlockSafe(x, y, z, BlockType.LEAVES.getId());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Méthode sécurisée pour placer un bloc, vérifie si les coordonnées sont valides
     */
    private void setBlockSafe(int x, int y, int z, int blockType) {
        // Calculer les coordonnées réelles en tenant compte du décalage
        int realX = x + (width / 2);
        int realZ = z + (depth / 2);
        
        // Vérifier que les coordonnées sont dans les limites
        if (realX >= 0 && realX < width && y >= 0 && y < height && realZ >= 0 && realZ < depth) {
            blocks[realX][y][realZ] = blockType;
        }
    }
    
    /**
     * Met à jour l'arbre. Gère la croissance automatique dans le temps.
     * @param tpf Temps écoulé depuis la dernière frame
     */
    @Override
    public void update(float tpf) {
        if (!canGrow || !canGrowInSize()) {
            return; // L'arbre ne peut pas grandir
        }
        
        // Incrémenter le temps depuis la dernière croissance
        timeSinceLastGrowth += tpf;
        
        // Vérifier s'il est temps d'essayer de faire grandir l'arbre
        if (timeSinceLastGrowth >= growthInterval) {
            // Réinitialiser le timer
            timeSinceLastGrowth = 0f;
            
            // Tenter de faire grandir l'arbre selon la probabilité
            if (random.nextDouble() < growthProbability) {
                grow();
            }
        }
    }
    
    /**
     * Fait grandir l'arbre en augmentant ses dimensions et en régénérant sa structure.
     * @return true si la croissance a eu lieu, false sinon
     */
    @Override
    public boolean grow() {
        if (!canGrowInSize()) {
            return false; // L'arbre a atteint sa taille maximale
        }
        
        // Calculer les nouvelles dimensions (augmentation de 1-3 blocs)
        int newWidth = Math.min(maxWidth, width + 1 + random.nextInt(3));
        int newHeight = Math.min(maxHeight, height + 1 + random.nextInt(3));
        
        // Vérifier si les dimensions ont vraiment changé
        if (newWidth == width && newHeight == height) {
            return false; // Pas de croissance
        }
        
        // Sauvegarder les anciennes dimensions
        int oldWidth = width;
        int oldHeight = height;
        
        // Mettre à jour les dimensions
        width = newWidth;
        height = newHeight;
        depth = newWidth; // L'arbre est carré
        
        // Créer un nouveau tableau de blocs avec les nouvelles dimensions
        blocks = new int[width][height][depth];
        fillWithVoid();
        
        // Régénérer l'arbre avec les nouvelles dimensions
        createBasicTree();
        
        System.out.println("Arbre grandi de " + oldWidth + "x" + oldHeight + " à " + width + "x" + height + " à la position (" + worldX + ", " + worldY + ", " + worldZ + ")");
        
        return true;
    }
    
    /**
     * Remplit le tableau de blocs avec des blocs vides (0).
     */
    private void fillWithVoid() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    blocks[x][y][z] = 0;
                }
            }
        }
    }
    
    // Méthodes pour la gestion de la maturité et disparition
    public boolean hasMaturityTimer() {
        return hasMaturityTimer;
    }
    
    public void setMaturityTimer(float timer) {
        this.maturityTimer = timer;
        this.hasMaturityTimer = true;
    }
    
    public void updateMaturityTimer(float tpf) {
        if (hasMaturityTimer) {
            maturityTimer -= tpf;
        }
    }
    
    public boolean isReadyToDisappear() {
        return hasMaturityTimer && maturityTimer <= 0f;
    }
} 