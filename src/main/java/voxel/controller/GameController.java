package voxel.controller;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

import voxel.model.BiomeType;
import voxel.model.WorldModel;
import voxel.model.entity.Entity;
import voxel.model.entity.Player;
import voxel.model.entity.animals.AnimalRegistry;
import voxel.view.WorldRenderer;
import voxel.model.ChunkModel;
import java.util.List;
import java.util.Random;

/**
 * Contrôleur principal du jeu qui coordonne tous les autres contrôleurs.
 * Gère la logique globale de l'application.
 */
public class GameController {
    /** Référence au modèle du monde */
    private final WorldModel worldModel;
    
    /** Référence au renderer du monde */
    private final WorldRenderer worldRenderer;
    
    /** Référence au contrôleur d'entrées */
    private final InputController inputController;
    
    /** Référence au contrôleur du monde */
    private final WorldController worldController;

    /** Référence au contrôleur d'entités */
    private final EntityController entityController;

    /** Référence au contrôleur du joueur */
    private final PlayerController playerController;
    
    /** Caméra pour la position du joueur */
    private final Camera camera;

    /** Référence au gestionnaire d'états du jeu pour accéder à environmentTimeSpeed */
    private GameStateManager gameStateManager;

    /** Générateur de nombres aléatoires */
    private final Random random = new Random();

    /** Timer pour l'apparition d'animaux */
    private float animalSpawnTimer = 0f;

    /** Timer pour la disparition d'animaux */
    private float animalDespawnTimer = 0f;

    /** Intervalle de base pour l'apparition d'animaux (en secondes) */
    private static final float BASE_SPAWN_INTERVAL = 10f;

    /** Intervalle de base pour la disparition d'animaux (en secondes) */
    private static final float BASE_DESPAWN_INTERVAL = 30f;

    /** Nombre d'animaux par chunk (surface) - ratio de base */
    private static final float ANIMALS_PER_CHUNK_AREA = 1.8f; // 1.8 animaux par chunk en moyenne
    
    /** Nombre minimum d'animaux autorisés dans un monde */
    private static final int MIN_ANIMALS = 3;
    
    /** Nombre maximum calculé d'animaux pour ce monde */
    private final int maxAnimals;

    /** Permet d'initialiser le monde après un certain temps */
    boolean readyToInit = true;

    /** Compteur de temps */
    float timeElapsed = 0;

    /** Indique si les entités ont été initialisées */
    private boolean entitiesInitialized = false;

    /**
     * Crée un nouveau contrôleur de jeu.
     * 
     * @param worldModel Le modèle du monde
     * @param worldRenderer Le renderer du monde
     * @param inputController Le contrôleur d'entrées
     * @param worldController Le contrôleur du monde
     * @param camera La caméra du joueur
     */
    public GameController(WorldModel worldModel, WorldRenderer worldRenderer, 
                         InputController inputController, WorldController worldController,
                         EntityController entityController, Camera camera) {
        this.worldModel = worldModel;
        this.worldRenderer = worldRenderer;
        this.inputController = inputController;
        this.worldController = worldController;
        this.entityController = entityController;
        this.playerController = inputController.getPlayerController();
        this.camera = camera;

        // Calculer le nombre maximum d'animaux pour ce monde
        // On calcule basé sur le nombre de chunks, pas de voxels
        int worldSizeX = worldModel.getWorldSizeX();
        int worldSizeZ = worldModel.getWorldSizeZ();
        int chunkArea = worldSizeX * worldSizeZ;
        this.maxAnimals = Math.max(MIN_ANIMALS, (int)(chunkArea * ANIMALS_PER_CHUNK_AREA));
    }

    /**
     * Définit le gestionnaire d'états du jeu
     * 
     * @param gameStateManager Le gestionnaire d'états du jeu
     */
    public void setGameStateManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }

    /**
     * Initialise le jeu. Appelé une seule fois au démarrage.
     */
    public void initialize() {

        // Définir les coordonnées de spawn X et Z
        int spawnX = 8;
        int spawnZ = -2;
        
        // Trouver la hauteur du sol à ces coordonnées
        int groundHeight = worldModel.getGroundHeightAt(spawnX, spawnZ);
        
        // Si aucun sol n'est trouvé, utiliser une hauteur par défaut
        float spawnY = (groundHeight != -1) ? groundHeight + 2f : 155f; // +2 pour que le joueur soit légèrement au-dessus du sol
        
        // Créer automatiquement un joueur à la position de spawn calculée
        Vector3f spawnPosition = new Vector3f(spawnX, spawnY, spawnZ);
        Player player = (Player) entityController.createEntity(Player.class, spawnPosition);

        // Définir ce joueur comme le joueur actuel et activer le mode joueur
        playerController.setCurrentPlayer(player);

        // Activer le mode joueur (simule un appui sur la touche V)
        inputController.onAction("ToggleCameraMode", true, 0);
    }

    /**
     * Met à jour l'état du jeu à chaque frame.
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     */
    public void update(float tpf, ViewPort mainViewport) {
        // Mise à jour des mouvements de la caméra
        inputController.updateCameraMovement(tpf);
        
        // Mise à jour du monde voxel avec la position actuelle de la caméra
        worldController.update(tpf, mainViewport);

        // Permet d'initialiser proprement le monde
        if (timeElapsed > 0.1 && readyToInit){
            initialize();
            readyToInit = false;
        }

        // Obtenir la vitesse du temps de l'environnement
        float environmentSpeed = (gameStateManager != null) ? gameStateManager.getEnvironmentTimeSpeed() : 1.0f;
        
        // Mettre à jour les entités avec la vitesse de l'environnement pour les animaux
        entityController.update(tpf, environmentSpeed);
        
        // Gestion de l'apparition et disparition d'animaux avec environmentTimeSpeed
        if (worldModel.getActiveBiome() != BiomeType.FLOATING_ISLAND){
            updateAnimalSpawning(tpf, environmentSpeed);
            updateAnimalDespawning(tpf, environmentSpeed);
        }

        
        timeElapsed += tpf;
    }

    /**
     * Gère l'apparition aléatoire d'animaux
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     * @param environmentSpeed Vitesse du temps de l'environnement
     */
    private void updateAnimalSpawning(float tpf, float environmentSpeed) {
        // Mise à jour du timer avec la vitesse de l'environnement
        animalSpawnTimer += tpf * environmentSpeed;
        
        // Calculer l'intervalle actuel basé sur la vitesse
        float currentSpawnInterval = BASE_SPAWN_INTERVAL / environmentSpeed;
        
        // Vérifier si il est temps de faire apparaître un animal
        if (animalSpawnTimer >= currentSpawnInterval) {
            spawnRandomAnimal();
            
            // Réinitialiser le timer avec une variation aléatoire
            animalSpawnTimer = random.nextFloat() * currentSpawnInterval * 0.5f;
        }
    }

    /**
     * Gère la disparition aléatoire d'animaux (sauf le joueur)
     * 
     * @param tpf Temps écoulé depuis la dernière frame
     * @param environmentSpeed Vitesse du temps de l'environnement
     */
    private void updateAnimalDespawning(float tpf, float environmentSpeed) {
        // Mise à jour du timer avec la vitesse de l'environnement
        animalDespawnTimer += tpf * environmentSpeed;
        
        // Calculer l'intervalle actuel basé sur la vitesse
        float currentDespawnInterval = BASE_DESPAWN_INTERVAL / environmentSpeed;
        
        // Vérifier si il est temps de faire disparaître un animal
        if (animalDespawnTimer >= currentDespawnInterval) {
            despawnRandomAnimal();
            
            // Réinitialiser le timer avec une variation aléatoire
            animalDespawnTimer = random.nextFloat() * currentDespawnInterval * 0.5f;
        }
    }

    /**
     * Compte le nombre d'animaux actuellement présents dans le monde (excluant le joueur)
     * 
     * @return Le nombre d'animaux présents
     */
    private int getCurrentAnimalCount() {
        List<Entity> entities = worldModel.getEntityManager().getEntities();
        return (int) entities.stream()
            .filter(entity -> !(entity instanceof Player))
            .count();
    }

    /**
     * Fait apparaître un animal aléatoire à une position aléatoire
     */
    private void spawnRandomAnimal() {
        try {
            // Vérifier d'abord si on n'a pas déjà atteint le maximum d'animaux
            int currentAnimalCount = getCurrentAnimalCount();
            if (currentAnimalCount >= maxAnimals) {
                // System.out.println("Nombre maximum d'animaux atteint (" + maxAnimals + "). Pas de nouveau spawn.");
                return;
            }
            
            // Choisir une classe d'animal aléatoire
            Class<? extends Entity> animalClass = AnimalRegistry.getRandomAnimalClass();
            
            // Générer une position aléatoire dans le monde
            Vector3f spawnPosition = generateRandomSpawnPosition();
            
            if (spawnPosition != null) {
                // Créer l'animal
                Entity animal = entityController.createEntity(animalClass, spawnPosition);
                
                if (animal != null) {
                    System.out.println("Animal " + animalClass.getSimpleName() + " apparu à la position " + spawnPosition + 
                                     " (" + (currentAnimalCount + 1) + "/" + maxAnimals + " animaux)");
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'apparition d'un animal: " + e.getMessage());
        }
    }

    /**
     * Fait disparaître un animal aléatoire (sauf le joueur)
     */
    private void despawnRandomAnimal() {
        try {
            List<Entity> entities = worldModel.getEntityManager().getEntities();
            
            // Filtrer pour ne garder que les animaux (pas le joueur)
            List<Entity> animals = entities.stream()
                .filter(entity -> !(entity instanceof Player))
                .collect(java.util.stream.Collectors.toList());
            
            if (!animals.isEmpty()) {
                // Choisir un animal aléatoire à faire disparaître
                Entity animalToRemove = animals.get(random.nextInt(animals.size()));
                
                // Supprimer l'animal
                entityController.removeEntity(animalToRemove);
                
                System.out.println("Animal " + animalToRemove.getClass().getSimpleName() + " a disparu");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la disparition d'un animal: " + e.getMessage());
        }
    }

    /**
     * Génère une position aléatoire valide pour l'apparition d'un animal
     * 
     * @return Position aléatoire ou null si aucune position valide n'est trouvée
     */
    private Vector3f generateRandomSpawnPosition() {
        int attempts = 0;
        int maxAttempts = 20;
        
        while (attempts < maxAttempts) {
            // Calculer la taille totale du monde en voxels
            int worldSizeX = worldModel.getWorldSizeX() * ChunkModel.SIZE;
            int worldSizeZ = worldModel.getWorldSizeZ() * ChunkModel.SIZE;
            
            // Calculer les décalages pour centrer le monde sur (0,0,0)
            int offsetX = worldSizeX / 2;
            int offsetZ = worldSizeZ / 2;
            
            // Générer des coordonnées X et Z aléatoires dans la plage centrée
            // De -offsetX à +offsetX et de -offsetZ à +offsetZ
            float x = (random.nextFloat() * worldSizeX) - offsetX;
            float z = (random.nextFloat() * worldSizeZ) - offsetZ;
            
            // getGroundHeightAt accepte directement les coordonnées centrées (peut être négatives)
            int groundHeight = worldModel.getGroundHeightAt((int)x, (int)z);
            
            if (groundHeight != -1) {
                // Position valide trouvée
                float y = groundHeight + 2f; // Légèrement au-dessus du sol
                return new Vector3f(x, y, z);
            }
            
            attempts++;
        }
        
        // Aucune position valide trouvée après maxAttempts
        return null;
    }

    /**
     * Récupère le nombre maximum d'animaux autorisés dans ce monde
     * 
     * @return Le nombre maximum d'animaux
     */
    public int getMaxAnimals() {
        return maxAnimals;
    }

    /**
     * Récupère le nombre actuel d'animaux dans le monde
     * 
     * @return Le nombre actuel d'animaux (excluant le joueur)
     */
    public int getAnimalCount() {
        return getCurrentAnimalCount();
    }
} 