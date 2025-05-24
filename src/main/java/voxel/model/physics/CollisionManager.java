package voxel.model.physics;

import com.jme3.math.Vector3f;
import voxel.model.BlockType;
import voxel.model.WorldModel;
import voxel.model.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Gère les collisions entre les entités et avec le monde de blocs.
 */
public class CollisionManager {
    private WorldModel world;
    
    public CollisionManager(WorldModel world) {
        this.world = world;
    }
    
    /**
     * Vérifie et résout les collisions entre une entité et le monde.
     * 
     * @param entity L'entité dont on vérifie les collisions
     * @param newX Nouvelle position X prévue
     * @param newY Nouvelle position Y prévue
     * @param newZ Nouvelle position Z prévue
     * @return Un vecteur contenant la position corrigée après résolution des collisions
     */
    public Vector3f checkAndResolveWorldCollision(Entity entity, double newX, double newY, double newZ) {
        // Position originale
        double originalX = entity.getX();
        double originalY = entity.getY();
        double originalZ = entity.getZ();
        
        // Créer une boîte de collision à la nouvelle position
        BoundingBox entityBox = new BoundingBox(newX, newY, newZ, entity.getWidth(), entity.getHeight(), entity.getDepth());
        
        // Vérifier les collisions avec les blocs environnants
        int minBlockX = (int) Math.floor(entityBox.getMinX());
        int maxBlockX = (int) Math.ceil(entityBox.getMaxX());
        int minBlockY = (int) Math.floor(entityBox.getMinY());
        int maxBlockY = (int) Math.ceil(entityBox.getMaxY());
        int minBlockZ = (int) Math.floor(entityBox.getMinZ());
        int maxBlockZ = (int) Math.ceil(entityBox.getMaxZ());
        
        // Résultat final (position après résolution des collisions)
        Vector3f result = new Vector3f((float)newX, (float)newY, (float)newZ);
        boolean collisionDetected = false;
        
        // Vérifier chaque bloc potentiellement en collision
        for (int x = minBlockX; x <= maxBlockX; x++) {
            for (int y = minBlockY; y <= maxBlockY; y++) {
                for (int z = minBlockZ; z <= maxBlockZ; z++) {
                    int blockType = world.getBlockAt(x, y, z);
                    
                    // Si le bloc est solide et l'entité est en collision avec lui
                    if (isSolidBlock(blockType)) {
                        BoundingBox blockBox = new BoundingBox(x + 0.5, y + 0.5, z + 0.5, 1.0, 1.0, 1.0);
                        
                        if (entityBox.intersects(blockBox)) {

                            boolean isAirAbove1 = isTraversableBlock(world.getBlockAt(x, y+1, z));

                            collisionDetected = true;
                            
                            // Calculer le vecteur de pénétration
                            Vector3f penetration = entityBox.getPenetrationVector(blockBox);
                            
                            // Appliquer le vecteur de pénétration pour résoudre la collision
                            result.addLocal(penetration);
                            
                            // Mettre à jour la boîte de collision avec la nouvelle position
                            entityBox.update(result.x, result.y, result.z);
                            
                            // Ajuster la vitesse de l'entité en fonction de la collision
                            // Si collision sur l'axe Y (sol ou plafond)
                            if (penetration.y != 0) {
                                entity.setVy(0);
                            }
                            
                            // Si collision sur l'axe X
                            if (penetration.x != 0) {
                                // auto-step
                                if (isAirAbove1) {
                                    entity.setVerticalVelocity(8.0);
                                } else {
                                    entity.setVx(0);
                                }
                            }
                            
                            // Si collision sur l'axe Z
                            if (penetration.z != 0) {
                                // auto-step
                                if (isAirAbove1) {
                                    entity.setVerticalVelocity(8.0);
                                } else {
                                    entity.setVz(0);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Vérifie et résout les collisions entre deux entités.
     * 
     * @param entity1 Première entité
     * @param entity2 Deuxième entité
     */
    public void checkAndResolveEntityCollision(Entity entity1, Entity entity2) {
        // Créer des boîtes de collision pour les deux entités
        BoundingBox box1 = new BoundingBox(entity1.getX(), entity1.getY(), entity1.getZ(),
                                          entity1.getWidth(), entity1.getHeight(), entity1.getDepth());
        BoundingBox box2 = new BoundingBox(entity2.getX(), entity2.getY(), entity2.getZ(),
                                          entity2.getWidth(), entity2.getHeight(), entity2.getDepth());

        // Vérifier s'il y a collision
        if (box1.intersects(box2)) {
            // Calculer le vecteur de pénétration
            Vector3f penetration = box1.getPenetrationVector(box2);

            // Calculer le volume de chaque entité pour déterminer leur "taille"
            double volume1 = entity1.getWidth() * entity1.getHeight() * entity1.getDepth();
            double volume2 = entity2.getWidth() * entity2.getHeight() * entity2.getDepth();
            double totalVolume = volume1 + volume2;

            // Calculer les ratios basés sur les volumes
            double ratio1 = volume2 / totalVolume; // Plus l'entité 2 est volumineuse, plus l'entité 1 se déplace
            double ratio2 = volume1 / totalVolume; // Plus l'entité 1 est volumineuse, plus l'entité 2 se déplace

            // Appliquer le vecteur de pénétration aux deux entités
            entity1.setX(entity1.getX() + penetration.x * ratio1);
            entity1.setY(entity1.getY() + penetration.y * ratio1);
            entity1.setZ(entity1.getZ() + penetration.z * ratio1);

            entity2.setX(entity2.getX() - penetration.x * ratio2);
            entity2.setY(entity2.getY() - penetration.y * ratio2);
            entity2.setZ(entity2.getZ() - penetration.z * ratio2);

            // Ajuster les vitesses (rebond simplifié)
            if (penetration.x != 0) {
                double vx1 = entity1.getVx();
                double vx2 = entity2.getVx();
                entity1.setVx(vx2 * 0.5);
                entity2.setVx(vx1 * 0.5);
            }

            if (penetration.y != 0) {
                double vy1 = entity1.getVy();
                double vy2 = entity2.getVy();
                entity1.setVy(vy2 * 0.5);
                entity2.setVy(vy1 * 0.5);
            }

            if (penetration.z != 0) {
                double vz1 = entity1.getVz();
                double vz2 = entity2.getVz();
                entity1.setVz(vz2 * 0.5);
                entity2.setVz(vz1 * 0.5);
            }
        }
    }
    
    /**
     * Vérifie si un type de bloc est considéré comme solide.
     * Utilise les propriétés définies dans BlockType pour déterminer la solidité.
     * 
     * @param blockType L'identifiant du type de bloc
     * @return true si le bloc est solide, false sinon
     */
    public boolean isSolidBlock(int blockType) {
        BlockType type = BlockType.fromId(blockType);
        return type.isSolid();
    }
    
    /**
     * Vérifie si un type de bloc est traversable (non-solide).
     * Inclut l'air, tous les types d'eau, les nuages, les sables mouvants, etc.
     * 
     * @param blockType L'identifiant du type de bloc
     * @return true si le bloc est traversable, false sinon
     */
    public boolean isTraversableBlock(int blockType) {
        BlockType type = BlockType.fromId(blockType);
        
        // Un bloc est traversable s'il n'est pas solide
        // Cela inclut automatiquement :
        // - AIR, VOID, INVISIBLE
        // - Tous les types d'eau (WATER, COLD_WATER, WARM_WATER, JUNGLE_WATER, DESERT_WATER, SWAMP_WATER, MINERAL_WATER)
        // - CLOUD
        // - QUICKSAND (défini comme non-solide dans BlockType)
        return !type.isSolid();
    }
    
    /**
     * Vérifie si un bloc est de l'eau (tous types confondus).
     * 
     * @param blockType L'identifiant du type de bloc
     * @return true si c'est un type d'eau, false sinon
     */
    public boolean isWaterBlock(int blockType) {
        BlockType type = BlockType.fromId(blockType);
        return type.isWater();
    }
    
    /**
     * Vérifie si un bloc ralentit le mouvement (comme les sables mouvants ou l'eau dense).
     * 
     * @param blockType L'identifiant du type de bloc
     * @return true si le bloc ralentit le mouvement, false sinon
     */
    public boolean isSlowingBlock(int blockType) {
        BlockType type = BlockType.fromId(blockType);
        
        // Les liquides et les sables mouvants ralentissent le mouvement
        return type.isWater() || type == BlockType.QUICKSAND || type == BlockType.MUD;
    }
} 