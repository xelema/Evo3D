package voxel.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import voxel.model.entity.Entity;

public class EntityRenderer {

    private AssetManager assetManager;
    private Node entityNode;
    private Entity entity;
    private Geometry geometry;

    public EntityRenderer(AssetManager assetManager, Entity entity) {
        this.assetManager = assetManager;
        this.entity = entity;
        this.entityNode = new Node("entity_" + entity.hashCode());
        createEntityGeometry();
    }
    
    /**
     * Crée la géométrie pour représenter l'entité.
     * Par défaut, utilise un cube simple, mais peut être surchargé pour des rendus plus complexes.
     */
    protected void createEntityGeometry() {
        try {
            // Par défaut, une entité est représentée par un cube simple
            Box box = new Box(entity.getSize(), entity.getSize(), entity.getSize());
            geometry = new Geometry("entity_geom", box);

            // Créer un matériau rouge pour l'entité
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Red);

            geometry.setMaterial(material);
            entityNode.attachChild(geometry);
            System.out.println("Géométrie d'entité créée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la géométrie d'entité: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Met à jour la position de l'entité dans le monde.
     */
    public void update(){
        entityNode.setLocalTranslation((float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
    }

    public Node getNode() {
        return entityNode;
    }
}
