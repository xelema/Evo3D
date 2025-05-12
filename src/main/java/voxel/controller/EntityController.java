package voxel.controller;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import voxel.model.WorldModel;
import voxel.model.entity.Entity;
import voxel.model.entity.EntityManager;
import voxel.view.entity.EntityRendererManager;
import voxel.view.WorldRenderer;

/**
 * Contrôleur qui gère les entités du monde.
 * Coordonne les interactions entre le modèle d'entités et leur représentation visuelle.
 */
public class EntityController {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;

    /** Référence au renderer du monde */
    private final WorldRenderer worldRenderer;

    /** Référence au gestionnaire d'entités */
    private final EntityManager entityManager;

    /** Référence au gestionnaire de renderers d'entités */
    private final EntityRendererManager entityRendererManager;

    /** Caméra pour la position du joueur */
    private final Camera camera;

    /**
     * Crée un nouveau contrôleur d'entités.
     *
     * @param worldModel Le modèle du monde
     * @param worldRenderer Le renderer du monde
     * @param camera La caméra du joueur
     */
    public EntityController(WorldModel worldModel, WorldRenderer worldRenderer, Camera camera) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
        this.camera = camera;
        this.entityManager = worldModel.getEntityManager();
        this.entityRendererManager = worldRenderer.getEntityRendererManager();
    }

    /**
     * Crée et ajoute une entité au monde à la position spécifiée.
     *
     * @param entityClass Classe de l'entité à créer
     * @param position Position initiale de l'entité
     * @return L'entité créée
     */
    public Entity createEntity(Class<? extends Entity> entityClass, Vector3f position) {
        try {
            // Créer une nouvelle instance de l'entité
            Entity entity = entityClass.getConstructor(double.class, double.class, double.class)
                    .newInstance(position.x, position.y, position.z);

            // Ajouter l'entité au gestionnaire
            entityManager.addEntity(entity);

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprime une entité du monde.
     *
     * @param entity L'entité à supprimer
     */
    public void removeEntity(Entity entity) {
        entityManager.removeEntity(entity);
    }

    /**
     * Crée une entité à la position actuelle de la caméra.
     *
     * @param entityClass Classe de l'entité à créer
     * @return L'entité créée
     */
    public Entity createEntityAtCamera(Class<? extends Entity> entityClass) {
        Vector3f cameraPos = camera.getLocation();
        System.out.println("Création d'une entité " + entityClass.getSimpleName() + " à la position " + cameraPos);
        Entity entity = createEntity(entityClass, cameraPos);
        System.out.println("Entités après création: " + entityManager.getEntities().size());
        return entity;
    }

    /**
     * Met à jour toutes les entités.
     * Pour gérer la physique, les collisions et les interactions.
     *
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf) {
        // Déléguer la mise à jour des entités à l'EntityManager
        entityManager.updateAll(tpf);
        
        // Nettoyer les renderers des entités supprimées
        // Cette opération est désormais gérée par EntityRendererManager.update()
    }

    public void printEntitiesList() {
        System.out.println("Liste des entités:");
        for (Entity entity : entityManager.getEntities()) {
            System.out.print(entity);
            System.out.println(" : position: " + entity.getX() + ", " + entity.getY() + ", " + entity.getZ());
        }
    }
}