package voxel.view.entity;

import com.jme3.anim.AnimComposer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRenderer {

    protected AssetManager assetManager;
    private Node entityNode;
    protected Entity entity;
    protected Geometry geometry;
    protected Spatial model;
    protected AnimComposer animator;
    protected String playingAnimation = "";
    protected Map<String, List<String>> animations = null;
    
    // Taille de référence des modèles (en unités du modèle original)
    // Ces valeurs peuvent être ajustées selon la taille réelle des modèles
    private static final float DEFAULT_MODEL_HEIGHT = 2.0f; // Hauteur de référence des modèles
    
    // Facteur d'ajustement pour la relation entre modèle visuel et bounding box
    // > 1.0f = modèle plus grand que la bounding box
    // < 1.0f = modèle plus petit que la bounding box
    // = 1.0f = modèle et bounding box de même taille
    private static final float VISUAL_SCALE_FACTOR = 1.5f;

    public EntityRenderer(AssetManager assetManager, Entity entity) {
        this.assetManager = assetManager;
        this.entity = entity;
        this.entityNode = new Node("entity_" + entity.hashCode());
        this.animations = new HashMap<>();

        createEntityGeometry();
    }
    
    /**
     * Calcule le facteur d'échelle approprié pour le modèle basé sur les dimensions de l'entité.
     * @return Le facteur d'échelle à appliquer au modèle
     */
    private float calculateScaleFactor() {
        // Utilise la hauteur de l'entité comme référence principale
        float entityHeight = entity.getHeight();
        
        // Obtient la taille de référence spécifique au type d'entité
        float referenceHeight = getEntityReferenceHeight();
        
        // Calcule le facteur d'échelle basé sur la hauteur
        float scaleFactor = (entityHeight / referenceHeight) * VISUAL_SCALE_FACTOR;
        
        return scaleFactor;
    }
    
    /**
     * Retourne la hauteur de référence du modèle original pour ce type d'entité.
     * Cette méthode peut être ajustée si certains modèles ont des tailles très différentes.
     * @return La hauteur de référence du modèle original
     */
    private float getEntityReferenceHeight() {
        // Pour la plupart des entités, utilise la hauteur par défaut
        // Peut être spécialisé pour certains types si nécessaire
        return switch (entity) {
            case Player player -> 2.5f; // Le modèle de joueur pourrait être différent
            case Lizard lizard -> 1.0f; // Les lézards sont naturellement plus bas
            case Scorpion scorpion -> 1.0f; // Les scorpions aussi
            case Eagle eagle -> 1.5f; // Les aigles peuvent avoir une pose différente
            case Owl owl -> 1.5f; // Les hiboux aussi
            default -> DEFAULT_MODEL_HEIGHT; // Utilise la taille par défaut pour les autres
        };
    }
    
    /**
     * Crée la géométrie pour représenter l'entité.
     * Par défaut, utilise un cube simple, mais peut être surchargé pour des rendus plus complexes.
     */
    protected void createEntityGeometry() {
        try {
            float scaleFactor = calculateScaleFactor();
            
            switch (entity) {
                case Player player ->
                        loadModel(Player.MODEL_PATH, scaleFactor);
                case Cow cow ->
                        loadModel(Cow.MODEL_PATH, scaleFactor);
                case Dromedary dromedary ->
                        loadModel(Dromedary.MODEL_PATH, scaleFactor);
                case Eagle eagle ->
                        loadModel(Eagle.MODEL_PATH, scaleFactor);
                case Fox fox ->
                        loadModel(Fox.MODEL_PATH, scaleFactor);
                case Lizard lizard ->
                        loadModel(Lizard.MODEL_PATH, scaleFactor);
                case Owl owl ->
                        loadModel(Owl.MODEL_PATH, scaleFactor);
                case Scorpion scorpion ->
                        loadModel(Scorpion.MODEL_PATH, scaleFactor);
                case Sheep sheep ->
                        loadModel(Sheep.MODEL_PATH, scaleFactor);
                case Wolf wolf ->
                        loadModel(Wolf.MODEL_PATH, scaleFactor);

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

        // Appliquer l'animation si l'entité remplit les conditions
        if (this.animator != null ){
            if (!this.playingAnimation.equals("idle") && !entity.isMoving()) {
                this.playingAnimation = "idle";
                playAnimation(playingAnimation);
            } else if (!this.playingAnimation.equals("jump") && entity.isJumping()) {
                this.playingAnimation = "jump";
                playAnimation(playingAnimation);
            } else if (!this.playingAnimation.equals("fly") && entity.isFalling()) {
                this.playingAnimation = "fly";
                playAnimation(playingAnimation);
            } else if (!this.playingAnimation.equals("walk") && entity.isWalking()) {
                this.playingAnimation = "walk";
                playAnimation(playingAnimation);
            } else if (!this.playingAnimation.equals("run") && entity.isRunning()) {
                this.playingAnimation = "run";
                playAnimation(playingAnimation);
            }
        }
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
            animator = findAnimComposer(model);

            if (animator != null) {
                // Regroupe les animations par type dans une Map
                for(String animName : animator.getAnimClipsNames()) {
                    String lowerAnimName = animName.toLowerCase();
                    String key;
                    if (lowerAnimName.contains("walk")) {
                        key = "walk";
                    } else if (lowerAnimName.contains("run")) {
                        key = "run";
                    } else if (lowerAnimName.contains("idle") || lowerAnimName.contains("sit")) {
                        key = "idle";
                    } else if (lowerAnimName.contains("jump")) {
                        key = "jump";
                    } else if (lowerAnimName.contains("fly")) {
                        key = "fly";
                    } else {
                        continue;
                    }
                    animations
                            .computeIfAbsent(key, k -> new ArrayList<>())
                            .add(animName);
                }
            }
            else {
                System.err.println("Aucun AnimComposer trouvé dans le modèle (" + modelPath + ")");
            }

            entityNode.attachChild(modelNode);
        }catch (Exception e){
            System.err.println("Erreur lors du chargement du modèle ("+ modelPath + ") : " + e.getMessage());
            e.printStackTrace();
            createBoxEntity();
        }
    }

    /**
     * Finds AnimComposer, checking the spatial and its children.
     * Fonction récupérée sur le forum de JMonkeyEngine
     */
    private AnimComposer findAnimComposer(Spatial spatial) {
        AnimComposer composer = spatial.getControl(AnimComposer.class);
        if (composer != null) {
            return composer;
        }
        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) {
                    return composer;
                }
            }
        }
        return null;
    }

    /**
     * Joue une animation aléatoire parmi un type d'animation sur le modèle.
     */
    private void playAnimation(String type) {
        List<String> differentAnimations = animations.get(type);
        if (differentAnimations != null && !differentAnimations.isEmpty()) {
            differentAnimations.remove("Idle_B");
            differentAnimations.remove("Idle_C");
            String randomAnimation = differentAnimations.get((int) (Math.random() * differentAnimations.size()));
            animator.setCurrentAction(randomAnimation);

        }
        else{
            System.err.println("Aucune animation trouvée pour le type : " + type);
        }
    }

    public Node getNode() {
        return entityNode;
    }
}
