package voxel.model.physics;

import voxel.model.WorldModel;
import voxel.model.entity.Entity;
import voxel.model.entity.Player;

import java.util.List;

/**
 * Gère la physique globale du monde comme la gravité, friction, etc.
 */
public class PhysicsManager {
    /** Constante de gravité */
    public static final double GRAVITY = -9.81;
    
    /** Facteur de friction au sol */
    public static final double GROUND_FRICTION = 0.7;
    
    /** Facteur de friction dans l'air */
    public static final double AIR_FRICTION = 0.95;
    
    private WorldModel world;
    private CollisionManager collisionManager;
    
    public PhysicsManager(WorldModel world, CollisionManager collisionManager) {
        this.world = world;
        this.collisionManager = collisionManager;
    }
    
    /**
     * Applique les forces physiques à toutes les entités.
     * 
     * @param entities Liste des entités à mettre à jour
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void applyPhysics(List<Entity> entities, float tpf) {
        for (Entity entity : entities) {
            // Applique la gravité aux entités
            applyGravity(entity, tpf);

            // Applique la friction
            applyFriction(entity, tpf);

            // Vérifie si les entités sont au sol
            checkOnGround(entity);
        }
    }
    
    /**
     * Applique la gravité à une entité.
     *
     * @param entity L'entité à laquelle appliquer la gravité
     * @param tpf Temps écoulé depuis la dernière frame
     */
    private void applyGravity(Entity entity, float tpf) {
        entity.addVelocity(0, 1.5*GRAVITY * tpf, 0);
    }
    
    /**
     * Applique la friction au mouvement d'une entité.
     * 
     * @param entity L'entité à laquelle appliquer la friction
     * @param tpf Temps écoulé depuis la dernière frame
     */
    private void applyFriction(Entity entity, float tpf) {
        boolean isOnGround = isEntityOnGround(entity);
        double frictionFactor = isOnGround ? GROUND_FRICTION : AIR_FRICTION;
        
        // Application de la friction horizontale
        entity.setVx(entity.getVx() * Math.pow(frictionFactor, tpf));
        entity.setVz(entity.getVz() * Math.pow(frictionFactor, tpf));
        
        // Arrête complètement les mouvements très lents
        if (Math.abs(entity.getVx()) < 0.01) {
            entity.setVx(0);
        }
        if (Math.abs(entity.getVz()) < 0.01) {
            entity.setVz(0);
        }
    }
    
    /**
     * Vérifie si une entité est au sol.
     * 
     * @param entity L'entité à vérifier
     * @return true si l'entité est au sol, false sinon
     */
    private boolean isEntityOnGround(Entity entity) {
        // Crée une boîte de collision un peu en dessous de l'entité
        BoundingBox entityBox = entity.getBoundingBox();
        double groundCheckOffset = 0.1; // Petite distance sous l'entité
        
        double groundCheckX = entity.getX();
        double groundCheckY = entity.getY() - (entityBox.getHeight() / 2) - groundCheckOffset;
        double groundCheckZ = entity.getZ();
        
        // Vérifie si il y a un bloc solide juste en dessous
        int blockX = (int) Math.floor(groundCheckX);
        int blockY = (int) Math.floor(groundCheckY);
        int blockZ = (int) Math.floor(groundCheckZ);
        
        int blockType = world.getBlockAt(blockX, blockY, blockZ);
        return collisionManager.isSolidBlock(blockType);
    }
    
    /**
     * Vérifie si une entité est au sol et met à jour son état.
     * 
     * @param entity L'entité à vérifier
     */
    private void checkOnGround(Entity entity) {
        boolean onGround = isEntityOnGround(entity);
        entity.setOnGround(onGround);
    }
} 