package voxel.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

import voxel.model.entity.Entity;
import voxel.model.entity.Player;
import voxel.model.entity.animals.Cow;

public class EntityRenderer {

    protected AssetManager assetManager;
    private Node entityNode;
    protected Entity entity;
    protected Geometry geometry;

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
            Box box = new Box(entity.getWidth()/2, entity.getHeight()/2, entity.getDepth()/2);
            geometry = new Geometry("entity_geom", box);

            // Créer un matériau selon le type d'entité
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            if (entity instanceof Player) {
                // Matériau bleu pour les joueurs
                material.setColor("Color", ColorRGBA.Blue);
            } else if (entity instanceof Cow) {
                // Matériau rouge pour les vaches
                material.setColor("Color", ColorRGBA.Brown);
            } else {
                // Matériau rouge pour les autres entités
                material.setColor("Color", ColorRGBA.Red);
            }

            geometry.setMaterial(material);
            entityNode.attachChild(geometry);
            System.out.println("Géométrie d'entité créée avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la géométrie d'entité: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Met à jour la position et la rotation de l'entité dans le monde.
     */
    public void update(){
        // Mettre à jour la position
        entityNode.setLocalTranslation((float) entity.getX(), (float) entity.getY(), (float) entity.getZ());
        
        // Appliquer la rotation horizontale (autour de l'axe Y) pour toutes les entités
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(entity.getRotation(), new Vector3f(0, 1, 0));
        entityNode.setLocalRotation(rotation);
    }

    public Node getNode() {
        return entityNode;
    }
}
