package voxel.model.structure;

public abstract class Structure {
    protected int [][][]blocks;
    protected int height;
    protected int width;
    protected int depth;

    public Structure(int width, int height, int depth) {
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

    private void fillWithVoid(){
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                for (int z = 0; z < depth; z++){
                    blocks[x][y][z] = 0;
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
}
