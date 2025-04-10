package voxel.model.entity;

import voxel.model.WorldModel;
import java.util.*;

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
        for (Entity entity : new ArrayList<>(entities)) {
            entity.update(tpf);
        }
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }
}
