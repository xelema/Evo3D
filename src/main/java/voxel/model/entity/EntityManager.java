package voxel.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import voxel.model.WorldModel;

public class EntityManager {
    private List<Entity> entities = new ArrayList<>();
    private WorldModel world;

    public EntityManager(WorldModel world) {
        this.world = world;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public void updateAll(float tpf) {
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            entity.update(tpf);
            
            // Vérifier si l'entité est tombée en dessous du niveau minimum
            if (entity.getY() < 2) {
                iterator.remove(); // Suppression sécurisée avec l'itérateur
            }
        }
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }
}
