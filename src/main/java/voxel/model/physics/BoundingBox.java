package voxel.model.physics;

import com.jme3.math.Vector3f;

/**
 * Représente une boîte de collision pour une entité.
 * Cette boîte est alignée sur les axes (AABB - Axis-Aligned Bounding Box).
 */
public class BoundingBox {
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    
    /**
     * Crée une boîte de collision avec les dimensions spécifiées.
     * 
     * @param centerX Position X du centre de la boîte
     * @param centerY Position Y du centre de la boîte
     * @param centerZ Position Z du centre de la boîte
     * @param width Largeur de la boîte (axe X)
     * @param height Hauteur de la boîte (axe Y)
     * @param depth Profondeur de la boîte (axe Z)
     */
    public BoundingBox(double centerX, double centerY, double centerZ, 
                      double width, double height, double depth) {
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfDepth = depth / 2;
        
        this.minX = centerX - halfWidth;
        this.maxX = centerX + halfWidth;
        this.minY = centerY - halfHeight;
        this.maxY = centerY + halfHeight;
        this.minZ = centerZ - halfDepth;
        this.maxZ = centerZ + halfDepth;
    }
    
    /**
     * Met à jour la position de la boîte de collision.
     * 
     * @param centerX Nouvelle position X du centre
     * @param centerY Nouvelle position Y du centre
     * @param centerZ Nouvelle position Z du centre
     */
    public void update(double centerX, double centerY, double centerZ) {
        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;
        
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfDepth = depth / 2;
        
        this.minX = centerX - halfWidth;
        this.maxX = centerX + halfWidth;
        this.minY = centerY - halfHeight;
        this.maxY = centerY + halfHeight;
        this.minZ = centerZ - halfDepth;
        this.maxZ = centerZ + halfDepth;
    }
    
    /**
     * Vérifie si cette boîte est en collision avec une autre boîte.
     * 
     * @param other L'autre boîte de collision à tester
     * @return true si les boîtes sont en collision, false sinon
     */
    public boolean intersects(BoundingBox other) {
        return this.minX <= other.maxX && this.maxX >= other.minX &&
               this.minY <= other.maxY && this.maxY >= other.minY &&
               this.minZ <= other.maxZ && this.maxZ >= other.minZ;
    }
    
    /**
     * Vérifie si la boîte contient un point donné.
     * 
     * @param x Coordonnée X du point
     * @param y Coordonnée Y du point
     * @param z Coordonnée Z du point
     * @return true si le point est dans la boîte, false sinon
     */
    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    /**
     * Calcule le vecteur de pénétration minimal pour résoudre une collision.
     * 
     * @param other L'autre boîte de collision
     * @return Un Vector3f représentant le vecteur de pénétration
     */
    public Vector3f getPenetrationVector(BoundingBox other) {
        Vector3f vector = new Vector3f();
        
        // Calcul des pénétrations sur chaque axe
        double penX = Math.min(this.maxX - other.minX, other.maxX - this.minX);
        double penY = Math.min(this.maxY - other.minY, other.maxY - this.minY);
        double penZ = Math.min(this.maxZ - other.minZ, other.maxZ - this.minZ);
        
        // Choisir l'axe avec la pénétration minimale
        if (penX <= penY && penX <= penZ) {
            // Collision sur l'axe X
            vector.x = (float)((this.minX + this.maxX < other.minX + other.maxX) ? -penX : penX);
            vector.y = 0;
            vector.z = 0;
        } else if (penY <= penX && penY <= penZ) {
            // Collision sur l'axe Y
            vector.x = 0;
            vector.y = (float)((this.minY + this.maxY < other.minY + other.maxY) ? -penY : penY);
            vector.z = 0;
        } else {
            // Collision sur l'axe Z
            vector.x = 0;
            vector.y = 0;
            vector.z = (float)((this.minZ + this.maxZ < other.minZ + other.maxZ) ? -penZ : penZ);
        }
        
        return vector;
    }
    
    // Getters et setters
    
    public double getMinX() {
        return minX;
    }
    
    public double getMinY() {
        return minY;
    }
    
    public double getMinZ() {
        return minZ;
    }
    
    public double getMaxX() {
        return maxX;
    }
    
    public double getMaxY() {
        return maxY;
    }
    
    public double getMaxZ() {
        return maxZ;
    }
    
    public double getWidth() {
        return maxX - minX;
    }
    
    public double getHeight() {
        return maxY - minY;
    }
    
    public double getDepth() {
        return maxZ - minZ;
    }
} 