package voxel.model.entity.animals;

import voxel.model.entity.Entity;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Registre des animaux disponibles pour l'apparition dans le monde.
 * Centralise la gestion des types d'animaux et fournit des méthodes utilitaires.
 */
public class AnimalRegistry {
    
    /** Liste des classes d'animaux disponibles */
    private static final List<Class<? extends Entity>> ANIMAL_CLASSES = Arrays.asList(
        Cow.class,
        Sheep.class,
        Fox.class,
        Wolf.class,
        Eagle.class,
        Owl.class,
        Dromedary.class,
        Scorpion.class,
        Lizard.class
    );
    
    /** Générateur de nombres aléatoires */
    private static final Random RANDOM = new Random();
    
    /**
     * Récupère une classe d'animal aléatoire.
     * 
     * @return Une classe d'animal choisie aléatoirement
     */
    public static Class<? extends Entity> getRandomAnimalClass() {
        return ANIMAL_CLASSES.get(RANDOM.nextInt(ANIMAL_CLASSES.size()));
    }
    
    /**
     * Récupère la liste complète des classes d'animaux.
     * 
     * @return Liste non modifiable des classes d'animaux
     */
    public static List<Class<? extends Entity>> getAllAnimalClasses() {
        return ANIMAL_CLASSES;
    }
    
    /**
     * Vérifie si une classe donnée est un animal enregistré.
     * 
     * @param entityClass La classe à vérifier
     * @return true si c'est un animal enregistré, false sinon
     */
    public static boolean isRegisteredAnimal(Class<? extends Entity> entityClass) {
        return ANIMAL_CLASSES.contains(entityClass);
    }
    
    /**
     * Récupère le nombre total d'animaux enregistrés.
     * 
     * @return Le nombre d'animaux disponibles
     */
    public static int getAnimalCount() {
        return ANIMAL_CLASSES.size();
    }
} 