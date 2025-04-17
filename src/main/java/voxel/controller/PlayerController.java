package voxel.controller;

import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import voxel.model.entity.Player;

/**
 * Gère le joueur, ses mouvements et son interaction avec le monde.
 */
public class PlayerController {
    
    private final WorldController worldController;
    private final EntityController entityController;
    private final Camera camera;
    private final InputManager inputManager;
    
    private Player currentPlayer = null;
    private boolean inPlayerMode = false;
    private boolean thirdPersonView = false;
    
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean movingUp = false;
    private boolean speedFly = false;
    
    private double autoStepOffset = 0.0;
    private final double autoStepSpeed = 0.2;
    
    /**
     * Crée un nouveau contrôleur de joueur.
     *
     * @param worldController Référence au contrôleur de monde
     * @param entityController Référence au contrôleur d'entités
     * @param camera Référence à la caméra
     * @param inputManager Référence au gestionnaire d'entrées
     */
    public PlayerController(WorldController worldController, EntityController entityController, 
                           Camera camera, InputManager inputManager) {
        this.worldController = worldController;
        this.entityController = entityController;
        this.camera = camera;
        this.inputManager = inputManager;
    }
    
    /**
     * Met à jour la position du joueur en fonction des touches enfoncées.
     * 
     * @param tpf Temps écoulé depuis la dernière image
     */
    public void updatePlayerMovement(float tpf) {
        if (currentPlayer == null) return;
        
        // Vitesse de déplacement du joueur
        float speed = speedFly ? 12f : 5f;
        
        // Vecteurs de direction basés sur l'orientation de la caméra
        Vector3f camDir = camera.getDirection().clone();
        camDir.y = 0; // Maintenir le déplacement sur le plan horizontal
        camDir.normalizeLocal();
        
        Vector3f camLeft = camera.getLeft().clone();
        camLeft.y = 0; // Maintenir le déplacement sur le plan horizontal
        camLeft.normalizeLocal();
        
        // Synchroniser la rotation du joueur avec celle de la caméra
        // Obtenir les angles d'Euler de la caméra
        float[] angles = new float[3];
        camera.getRotation().toAngles(angles);
        
        // Mettre à jour la rotation du joueur (rotation horizontale uniquement)
        currentPlayer.setRotation(angles[1]); // angles[1] est le yaw (rotation horizontale)
        
        // Calcul du vecteur de mouvement
        Vector3f moveDir = new Vector3f(0, 0, 0);
        
        if (movingForward) moveDir.addLocal(camDir);
        if (movingBackward) moveDir.addLocal(camDir.negate());
        if (movingLeft) moveDir.addLocal(camLeft);
        if (movingRight) moveDir.addLocal(camLeft.negate());
        
        // Normaliser le vecteur de mouvement s'il n'est pas nul
        if (moveDir.length() > 0) {
            moveDir.normalizeLocal();
            moveDir.multLocal(speed);
        }
        
        // Saut
        if (movingUp && currentPlayer.isOnGround()) {
            currentPlayer.jump();
        }

        // Auto-step
        // Si une montée fluide est en cours, on l'applique
        if (autoStepOffset > 0) {
            double step = Math.min(autoStepSpeed, autoStepOffset);
            currentPlayer.setY(currentPlayer.getY() + step);
            autoStepOffset -= step;
        } else {
            // Détection d'auto-step uniquement si pas déjà en montée
            if (moveDir.length() > 0 && currentPlayer.isOnGround()) {
                double px = currentPlayer.getX();
                double py = currentPlayer.getY();
                double pz = currentPlayer.getZ();
                float playerWidth = currentPlayer.getWidth();
                float playerHeight = currentPlayer.getHeight();

                // Direction de déplacement (normalisée)
                Vector3f dir = moveDir.clone().normalizeLocal();
                // Position devant le joueur (légèrement avancée)
                double frontX = px + dir.x * (playerWidth/2 + 0.3);
                double frontZ = pz + dir.z * (playerWidth/2 + 0.3);
                int baseY = (int)Math.floor(py - playerHeight/2 + 0.01); // Pieds du joueur

                // Vérifier le bloc juste devant les pieds
                int blockFront = worldController.getWorldModel().getBlockAt((int)Math.floor(frontX), baseY, (int)Math.floor(frontZ));
                int blockFrontAbove = worldController.getWorldModel().getBlockAt((int)Math.floor(frontX), baseY+1, (int)Math.floor(frontZ));
                int blockFrontAbove2 = worldController.getWorldModel().getBlockAt((int)Math.floor(frontX), baseY+2, (int)Math.floor(frontZ));
                boolean isSolidFront = blockFront != 0; // 0 = AIR
                boolean isSolidFrontAbove = blockFrontAbove != 0;
                boolean isSolidFrontAbove2 = blockFrontAbove2 != 0;

                // Si bloc devant les pieds mais espace libre juste au-dessus (step de 1 bloc)
                if (isSolidFront && !isSolidFrontAbove && !isSolidFrontAbove2) {
                    // Initialiser la montée fluide
                    autoStepOffset = 1.0;
                }
            }
        }
        
        // Appliquer la vitesse horizontale
        currentPlayer.setVelocity(moveDir.x, currentPlayer.getVy(), moveDir.z);
    }
    
    /**
     * Met à jour la position de la caméra en fonction du joueur.
     */
    public void updateCameraPosition() {
        if (currentPlayer == null) return;
        
        double playerX = currentPlayer.getX();
        double playerY = currentPlayer.getY();
        double playerZ = currentPlayer.getZ();
        
        if (thirdPersonView) {
            // Vue à la 3ème personne
            // Récupère la direction horizontale vers laquelle le joueur fait face (lacet/yaw)
            float yaw = currentPlayer.getRotation();
            
            // Calcule la position fixe directement derrière le joueur
            float distanceBehind = 6.0f; // Distance derrière le joueur (augmentée de 4.0f à 6.0f)
            float heightAbove = 2.5f;    // Hauteur au-dessus du joueur (augmentée de 1.5f à 2.5f)
            
            // Calcule la position derrière le joueur en fonction de sa rotation
            float dx = (float) Math.sin(yaw);
            float dz = (float) Math.cos(yaw);
            
            Vector3f cameraPosition = new Vector3f(
                (float)playerX - dx * distanceBehind,
                (float)playerY + heightAbove,
                (float)playerZ - dz * distanceBehind
            );
            
            // Positionne la caméra
            camera.setLocation(cameraPosition);
            
            // Calcule la direction pour regarder la tête du joueur
            Vector3f lookAtPoint = new Vector3f(
                (float)playerX,
                (float)playerY + 1.0f, // Vise au niveau de la tête
                (float)playerZ
            );
            
            // Définit la caméra pour regarder le joueur
            camera.lookAt(lookAtPoint, Vector3f.UNIT_Y);
        } else {
            // Vue à la 1ère personne
            // Calculer la position des yeux (90% de la hauteur du joueur depuis le bas)
            // Sachant que le joueur a une hauteur de 1.9, et le point de référence est au centre du joueur
            // La caméra doit être à playerY + (hauteur/2 - 0.1) pour être à 10% du haut
            float eyeHeight = (float)(playerY + currentPlayer.getHeight() * 0.4);
            
            Vector3f cameraPosition = new Vector3f(
                (float)playerX,
                eyeHeight, // À la hauteur des yeux
                (float)playerZ
            );
            
            camera.setLocation(cameraPosition);
        }
    }
    
    /**
     * Bascule entre la vue à la première personne et la vue à la troisième personne.
     */
    public void toggleThirdPersonView() {
        thirdPersonView = !thirdPersonView;
        System.out.println("Vue " + (thirdPersonView ? "à la 3ème personne" : "à la 1ère personne") + " activée");
        
        // Ajuster le frustum de la caméra en fonction du mode de vue
        updateCameraFrustum();
    }
    
    /**
     * Met à jour le frustum de la caméra en fonction du mode actuel.
     * En mode première personne, le plan near est plus proche pour éviter le clipping.
     */
    public void updateCameraFrustum() {
        float aspectRatio = (float) camera.getWidth() / camera.getHeight();
        
        if (inPlayerMode) {
            if (!thirdPersonView) {
                // En mode première personne, utiliser un plan near plus proche
                camera.setFrustumPerspective(70f, aspectRatio, 0.1f, 1000f);
            } else {
                // En mode troisième personne, ajuster le FOV pour une meilleure vue
                camera.setFrustumPerspective(65f, aspectRatio, 0.5f, 1000f);
            }
        } else {
            // En mode libre, utiliser le plan near par défaut
            camera.setFrustumPerspective(80f, aspectRatio, 1f, 1000f);
        }
        
        // Appliquer les changements à la caméra
        camera.update();
    }
    
    /**
     * Crée un joueur à la position actuelle de la caméra.
     */
    public void spawnPlayerAtCamera() {
        System.out.println("Spawn player");
        currentPlayer = (Player) entityController.createEntityAtCamera(Player.class);
    }
    
    /**
     * Bascule entre le mode caméra libre et le mode joueur.
     */
    public void toggleCameraMode() {
        if (currentPlayer == null) {
            // Si pas de joueur actuel, en créer un à la position de la caméra
            spawnPlayerAtCamera();
        }
        
        inPlayerMode = !inPlayerMode;
        System.out.println("Mode " + (inPlayerMode ? "joueur" : "caméra libre") + " activé");
        
        // Ajuster le frustum de la caméra en fonction du mode
        updateCameraFrustum();
    }
    
    /**
     * @return le joueur actuel, ou null s'il n'existe pas
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * @return true si en mode joueur, false si en mode caméra libre
     */
    public boolean isInPlayerMode() {
        return inPlayerMode;
    }
    
    /**
     * @return true si en vue à la troisième personne, false si en vue à la première personne
     */
    public boolean isThirdPersonView() {
        return thirdPersonView;
    }
    
    /**
     * Définit l'entité joueur actuelle.
     * 
     * @param player l'entité joueur
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }
    
    // Méthodes de contrôle du mouvement
    
    public void setMovingForward(boolean moving) {
        this.movingForward = moving;
    }
    
    public void setMovingBackward(boolean moving) {
        this.movingBackward = moving;
    }
    
    public void setMovingLeft(boolean moving) {
        this.movingLeft = moving;
    }
    
    public void setMovingRight(boolean moving) {
        this.movingRight = moving;
    }
    
    public void setMovingUp(boolean moving) {
        this.movingUp = moving;
    }
    
    public void setSpeedFly(boolean speedFly) {
        this.speedFly = speedFly;
    }
} 