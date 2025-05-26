package voxel.model;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;

/**
 * Mini-map 2D affichée à l'écran, avec un point pour représenter le joueur.
 */
public class MiniMap2D {

    private final Node guiNode;
    private final AssetManager assetManager;

    private final float mapWidth = 200f;
    private final float mapHeight = 200f;

    private final float worldWidth = 100f; // à adapter à ton monde
    private final float worldHeight = 100f;

    private final Geometry playerDot;

    public MiniMap2D(AssetManager assetManager, Node guiNode) {
        this.assetManager = assetManager;
        this.guiNode = guiNode;

        // Fond de mini map (couleur unie ou texture)
        Picture background = new Picture("MiniMapBackground");
        background.setWidth(mapWidth);
        background.setHeight(mapHeight);
        background.setPosition(1140, 520); // position à l'écran (en haut à droite si 1340x720)

        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.DarkGray);
        background.setMaterial(bgMat);

        guiNode.attachChild(background);

        // Point du joueur
        Quad dotShape = new Quad(5, 5);
        playerDot = new Geometry("PlayerDot", dotShape);
        Material dotMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dotMat.setColor("Color", ColorRGBA.Red);
        playerDot.setMaterial(dotMat);
        guiNode.attachChild(playerDot);
    }

    /**
     * Met à jour la position du joueur sur la mini-map.
     * @param playerPosition position 3D du joueur dans le monde
     */
    public void update(Vector3f playerPosition) {
        if (playerPosition == null) return;

        float xRatio = playerPosition.x / worldWidth;
        float zRatio = playerPosition.z / worldHeight;

        float screenX = 1140 + xRatio * mapWidth;
        float screenY = 520 + zRatio * mapHeight;

        playerDot.setLocalTranslation(screenX - 2.5f, screenY - 2.5f, 1); // centrer le point
    }
}