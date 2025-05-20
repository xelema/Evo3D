package voxel.view.entity;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import voxel.model.entity.Entity;
import voxel.model.entity.Player;
import voxel.model.entity.animals.*;

import java.util.List;

public class EntityRenderer {

    protected AssetManager assetManager;
    private Node entityNode;
    protected Entity entity;
    protected Geometry geometry;
    protected Spatial model;

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
            switch (entity) {
                case Player player ->
                        loadModel(Player.MODEL_PATH,0.002f);
                case Cow cow ->
                        loadModel(Cow.MODEL_PATH,1f);
                case Dromedary dromedary ->
                        loadModel(Dromedary.MODEL_PATH,1f);
                case Eagle eagle ->
                        loadModel(Eagle.MODEL_PATH,1f);
                case Fox fox ->
                        loadModel(Fox.MODEL_PATH,1f);
                case Lizard lizard ->
                        loadModel(Lizard.MODEL_PATH,1f);
                case Owl owl ->
                        loadModel(Owl.MODEL_PATH,1f);
                case Scorpion scorpion ->
                        loadModel(Scorpion.MODEL_PATH,1f);
                case Sheep sheep ->
                        loadModel(Sheep.MODEL_PATH,1f);
                case Wolf wolf ->
                        loadModel(Wolf.MODEL_PATH,1f);

                default -> {
                    createBoxEntity();
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la géométrie d'entité: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Crée un cube basique pour représenter l'entité.
     */
    private void createBoxEntity(){
        // Par défaut, une entité est représentée par un cube simple
        Box box = new Box(entity.getWidth() / 2, entity.getHeight() / 2, entity.getDepth() / 2);
        geometry = new Geometry("entity_geom", box);

        // Créer un matériau selon le type d'entité
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        // Matériau rouge pour les autres entités
        material.setColor("Color", ColorRGBA.Red);
        geometry.setMaterial(material);
        entityNode.attachChild(geometry);
        System.out.println("Géométrie d'entité créée avec succès");
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

    /**
     * Applique un matériau non ombré à tous les géométries du modèle
     * pour qu'il soit visible sans éclairage
     */
    private void applyUnshadedMaterials(Spatial spatial) {
        if (spatial instanceof Geometry) {
            Geometry geom = (Geometry) spatial;
            Material unshadedMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

            // Si le matériau original a une texture, essayer de la réutiliser
            Material originalMat = geom.getMaterial();
            if (originalMat != null && originalMat.getParam("DiffuseMap") != null) {
                unshadedMat.setTexture("ColorMap", originalMat.getTextureParam("DiffuseMap").getTextureValue());
            } else if (originalMat != null && originalMat.getParam("BaseColorMap") != null) {
                unshadedMat.setTexture("ColorMap", originalMat.getTextureParam("BaseColorMap").getTextureValue());
            } else {
                // Utiliser une couleur par défaut si pas de texture
                unshadedMat.setColor("Color", ColorRGBA.White);
            }

            // Désactiver la transparence qui peut causer des problèmes de rendu
            unshadedMat.getAdditionalRenderState().setBlendMode(com.jme3.material.RenderState.BlendMode.Off);
            unshadedMat.getAdditionalRenderState().setDepthTest(true);
            unshadedMat.getAdditionalRenderState().setDepthWrite(true);

            // Fixer la queue de rendu pour éviter les conflits avec d'autres éléments
            geom.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Opaque);

            geom.setMaterial(unshadedMat);
        } else if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                applyUnshadedMaterials(child);
            }
        }
    }

    /**
     * Charge un modèle 3D à partir d'un fichier et l'applique à l'entité.
     */
    private void loadModel(String modelPath, float scaleFactor) {
        try{
            model = assetManager.loadModel(modelPath);
            Node modelNode = new Node("model_node");
            modelNode.attachChild(model);

            modelNode.setQueueBucket(com.jme3.renderer.queue.RenderQueue.Bucket.Opaque);

            modelNode.setLocalScale(scaleFactor);

            applyUnshadedMaterials(model);

            modelNode.setLocalTranslation(0, -entity.getHeight()/2, 0);

            // Charger les animation TO-DO

            entityNode.attachChild(modelNode);
        }catch (Exception e){
            System.err.println("Erreur lors du chargement du modèle ("+ modelPath + ") : " + e.getMessage());
            e.printStackTrace();
            createBoxEntity();
        }
    }

    public Node getNode() {
        return entityNode;
    }
}
