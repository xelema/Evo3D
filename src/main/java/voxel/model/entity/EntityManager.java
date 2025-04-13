package voxel.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.jme3.math.Vector3f;
import voxel.model.WorldModel;
import voxel.model.physics.CollisionManager;
import voxel.model.physics.PhysicsManager;

public class EntityManager {
    private List<Entity> entities = new ArrayList<>();
    private WorldModel world;
    private CollisionManager collisionManager;
    private PhysicsManager physicsManager;

    public EntityManager(WorldModel world) {
        this.world = world;
        this.collisionManager = new CollisionManager(world);
        this.physicsManager = new PhysicsManager(world, collisionManager);
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public void updateAll(float tpf) {
        // Appliquer les effets physiques (gravité, friction) sur toutes les entités
        physicsManager.applyPhysics(entities, tpf);
        
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            
            // Mise à jour de l'entité (logique, IA, etc.)
            entity.update(tpf);
            
            // Calculer la nouvelle position prévue
            double newX = entity.getX() + entity.getVx() * tpf;
            double newY = entity.getY() + entity.getVy() * tpf;
            double newZ = entity.getZ() + entity.getVz() * tpf;
            
            // Vérifier et résoudre les collisions avec le monde
            Vector3f correctedPosition = collisionManager.checkAndResolveWorldCollision(entity, newX, newY, newZ);
            
            // Appliquer le mouvement avec la position corrigée
            entity.moveWithCollision(tpf, correctedPosition.x, correctedPosition.y, correctedPosition.z);
            
            // Vérifier si l'entité doit être supprimée
            if (entity.isMarkedForRemoval()) {
                iterator.remove(); // Suppression sécurisée avec l'itérateur
            }
        }
        
        // Vérifier les collisions entre entités
        checkEntityCollisions();
    }
    
    /**
     * Vérifie et résout les collisions entre les entités.
     */
    private void checkEntityCollisions() {
        int size = entities.size();
        for (int i = 0; i < size - 1; i++) {
            Entity entity1 = entities.get(i);
            
            for (int j = i + 1; j < size; j++) {
                Entity entity2 = entities.get(j);
                
                // Vérifier et résoudre la collision entre les deux entités
                collisionManager.checkAndResolveEntityCollision(entity1, entity2);
            }
        }
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }
    
    /**
     * Récupère le gestionnaire de collisions.
     * @return Le gestionnaire de collisions
     */
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
    
    /**
     * Récupère le gestionnaire de physique.
     * @return Le gestionnaire de physique
     */
    public PhysicsManager getPhysicsManager() {
        return physicsManager;
    }
}
