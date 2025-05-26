package voxel.model;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

public class MiniMap {

    private Camera miniMapCam;
    private ViewPort miniMapView;
    private static final float ZOOM = 40f;
    private static final float HEIGHT = 10f;


    public MiniMap(Camera mainCam, RenderManager renderManager, AssetManager assetManager, Node rootNode) {
        miniMapCam = mainCam.clone();
        miniMapCam.setParallelProjection(true);
        miniMapCam.setFrustum(-1000, 1000, -ZOOM, ZOOM, ZOOM, -ZOOM);

        // Position initiale
        Vector3f startPos = new Vector3f(0, HEIGHT, 0);
        miniMapCam.setLocation(startPos);
        miniMapCam.lookAtDirection(Vector3f.UNIT_Y.negate(), Vector3f.UNIT_Z); // Vue parfaitement verticale

        miniMapCam.setViewPort(0.75f, 1f, 0.75f, 1f); // Haut à droite
        miniMapView = renderManager.createMainView("MiniMapView", miniMapCam);
        miniMapView.setClearFlags(true, true, true);
        miniMapView.setBackgroundColor(ColorRGBA.Black);
        miniMapView.attachScene(rootNode);
    }

    public void update(Vector3f playerPosition) {
        if (playerPosition == null) return;

        Vector3f camPos = new Vector3f(playerPosition.x, HEIGHT, playerPosition.z);
        miniMapCam.setLocation(camPos);

        // Caméra regarde VERS LE BAS (top-down) sans se décaler
        miniMapCam.lookAtDirection(Vector3f.UNIT_Y.negate(), Vector3f.UNIT_Z);


    }
}



