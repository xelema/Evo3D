package voxel.model.structure;

public abstract class Structure {
    /** ID unique de la structure */
    private static int nextId = 1; // Commence à 1, 0 sera réservé pour "aucune structure"
    private final int structureId;
    
    protected int [][][]blocks;
    protected int height;
    protected int width;
    protected int depth;
    
    // Position dans le monde
    protected int worldX;
    protected int worldY;
    protected int worldZ;
    
    // Système de croissance
    protected float timeSinceLastGrowth = 0f;
    protected float growthInterval = 30f; // Temps entre les tentatives de croissance (en secondes)
    protected float growthProbability = 0.1f; // Probabilité de croissance (10%)
    protected boolean canGrow = true;
    protected int maxWidth = 15;
    protected int maxHeight = 15;

    public Structure(int width, int height, int depth) {
        this.structureId = nextId++; // Assigner un ID unique
        this.height = height;
        this.width = width;
        this.depth = depth;
        blocks = new int[width][height][depth];
        fillWithVoid();
    }

    public int[][][] getBlocks() {
        return blocks;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }
    
    // Getters et setters pour la position
    public int getWorldX() { return worldX; }
    public int getWorldY() { return worldY; }
    public int getWorldZ() { return worldZ; }
    
    public void setWorldPosition(int x, int y, int z) {
        this.worldX = x;
        this.worldY = y;
        this.worldZ = z;
    }
    
    // Getters et setters pour la croissance
    public float getTimeSinceLastGrowth() { return timeSinceLastGrowth; }
    public void setTimeSinceLastGrowth(float time) { this.timeSinceLastGrowth = time; }
    
    public float getGrowthInterval() { return growthInterval; }
    public void setGrowthInterval(float interval) { this.growthInterval = interval; }
    
    public float getGrowthProbability() { return growthProbability; }
    public void setGrowthProbability(float probability) { this.growthProbability = probability; }
    
    public boolean canGrow() { return canGrow; }
    public void setCanGrow(boolean canGrow) { this.canGrow = canGrow; }
    
    public int getMaxWidth() { return maxWidth; }
    public int getMaxHeight() { return maxHeight; }
    
    /**
     * Vérifie si la structure peut grandir (n'a pas atteint sa taille maximale).
     */
    public boolean canGrowInSize() {
        return canGrow && (width < maxWidth || height < maxHeight);
    }
    
    /**
     * Fait grandir la structure (augmente ses dimensions).
     * @return true si la croissance a eu lieu, false sinon
     */
    public abstract boolean grow();

    private void fillWithVoid(){
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                for (int z = 0; z < depth; z++){
                    blocks[x][y][z] = -1;
                }
            }
        }
    }

    /**
     * Pose un bloc à une position donnée dans la structure (centrée en 0).
     */
    protected void setBlock(int x, int y, int z, int blockType) {
        blocks[x+(width/2)][y][z+(depth/2)] = blockType;
    }

    public void deleteStructure() {
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                for (int z = 0; z < depth; z++){
                    if(blocks[x][y][z] != -1){
                        blocks[x][y][z] = 0;
                    }
                }
            }
        }
    }
    
    /**
     * Met à jour la structure. Méthode abstraite à implémenter par les sous-classes.
     * @param tpf Temps écoulé depuis la dernière frame (time per frame)
     */
    public abstract void update(float tpf);

    /**
     * Récupère l'ID unique de cette structure.
     * @return L'ID de la structure
     */
    public int getStructureId() {
        return structureId;
    }
}
