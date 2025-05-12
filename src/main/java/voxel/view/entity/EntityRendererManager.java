package voxel.view.entity;

import java.util.HashMap;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

import voxel.model.entity.Entity;
import voxel.model.entity.EntityManager;

/**
 * Classe responsable de gérer tous les renderers d'entités.
 * Synchronise les entités du modèle avec leurs renderers dans la vue.
 */
public class EntityRendererManager {
    /** Nœud racine contenant toutes les entités */
    private Node entitiesNode;

    /** Référence au gestionnaire d'entités du modèle */
    private EntityManager entityManager;

    /** AssetManager pour accéder aux ressources */
    private AssetManager assetManager;

    /** Map associant chaque entité à son renderer */
    private Map<Entity, EntityRenderer> entityRenderers;

    /**
     * Crée un nouveau gestionnaire de renderers d'entités.
     *
     * @param entityManager Le gestionnaire d'entités du modèle
     * @param assetManager AssetManager pour accéder aux ressources
     */
    public EntityRendererManager(EntityManager entityManager, AssetManager assetManager) {
        this.entityManager = entityManager;
        this.assetManager = assetManager;
        this.entitiesNode = new Node("entities");
        this.entityRenderers = new HashMap<>();
    }

    /**
     * Ajoute une entité au gestionnaire et crée son renderer.
     *
     * @param entity L'entité à ajouter
     */
    public void addEntity(Entity entity) {
        // Vérifier si l'entité a déjà un renderer
        if (entityRenderers.containsKey(entity)) {
            return;
        }

        // Créer un nouveau renderer pour cette entité
        EntityRenderer renderer = createRendererForEntity(entity);

        // Ajouter le renderer à la map
        entityRenderers.put(entity, renderer);

        // Attacher le nœud de l'entité au nœud principal
        entitiesNode.attachChild(renderer.getNode());
    }

    /**
     * Crée un renderer approprié pour le type d'entité.
     * Peut être étendu pour créer des renderers spécifiques selon le type d'entité.
     *
     * @param entity L'entité pour laquelle créer un renderer
     * @return Le renderer créé
     */
    protected EntityRenderer createRendererForEntity(Entity entity) {
        // Par défaut, utilise un EntityRenderer standard
        // Cette méthode peut être surchargée pour créer des renderers spécifiques
        return new EntityRenderer(assetManager, entity);
    }

    /**
     * Supprime une entité du gestionnaire et son renderer.
     *
     * @param entity L'entité à supprimer
     */
    public void removeEntity(Entity entity) {
        // Récupérer le renderer de l'entité
        EntityRenderer renderer = entityRenderers.get(entity);

        if (renderer != null) {
            // Détacher le nœud de l'entité
            entitiesNode.detachChild(renderer.getNode());

            // Supprimer l'entrée de la map
            entityRenderers.remove(entity);
        }
    }

    /**
     * Met à jour tous les renderers pour les entités existantes.
     * Synchronise également avec le gestionnaire d'entités pour ajouter/supprimer des renderers.
     */
    public void update() {
        // Ajouter des renderers pour les nouvelles entités
        for (Entity entity : entityManager.getEntities()) {
            if (!entityRenderers.containsKey(entity)) {
                addEntity(entity);
            }
        }

        // Mettre à jour tous les renderers existants
        for (Map.Entry<Entity, EntityRenderer> entry : new HashMap<>(entityRenderers).entrySet()) {
            Entity entity = entry.getKey();
            EntityRenderer renderer = entry.getValue();
            
            if (entityManager.getEntities().contains(entity)) {
                renderer.update();
            } else {
                // L'entité n'existe plus dans le modèle, supprimer son renderer
                removeEntity(entity);
            }
        }
    }

    /**
     * Retourne le nœud contenant toutes les entités.
     *
     * @return Le nœud des entités
     */
    public Node getNode() {
        return entitiesNode;
    }
}