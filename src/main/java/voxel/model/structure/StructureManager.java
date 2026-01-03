package voxel.model.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StructureManager {
    private List<Structure> structures = new ArrayList<>();
    
    public StructureManager(){
        
    }
    
    /**
     * Ajoute une structure au gestionnaire.
     * @param structure La structure à ajouter
     */
    public void addStructure(Structure structure) {
        structures.add(structure);
    }
    
    /**
     * Supprime une structure du gestionnaire.
     * @param structure La structure à supprimer
     */
    public void removeStructure(Structure structure) {
        structures.remove(structure);
    }
    
    /**
     * Détruit une structure (vide ses blocs) et la supprime du gestionnaire.
     * @param structure La structure à détruire
     */
    public void destroyStructure(Structure structure) {
        structure.deleteStructure();
        removeStructure(structure);
    }
    
    /**
     * Met à jour toutes les structures gérées.
     * @param tpf Temps écoulé depuis la dernière frame (time per frame)
     * @return Liste des structures qui ont grandi pendant cette mise à jour
     */
    public List<Structure> updateAll(float tpf) {
        List<Structure> grownStructures = new ArrayList<>();
        
        Iterator<Structure> iterator = structures.iterator();
        while (iterator.hasNext()) {
            Structure structure = iterator.next();
            
            // Sauvegarder les dimensions avant mise à jour
            int oldWidth = structure.getWidth();
            int oldHeight = structure.getHeight();
            
            // Mise à jour de la structure
            structure.update(tpf);
            
            // Vérifier si la structure a grandi
            if (structure.getWidth() != oldWidth || structure.getHeight() != oldHeight) {
                grownStructures.add(structure);
            }
        }
        
        return grownStructures;
    }
    
    /**
     * Retourne une vue non-modifiable de la liste des structures.
     * @return Liste des structures en lecture seule
     */
    public List<Structure> getStructures() {
        return Collections.unmodifiableList(structures);
    }
    
    /**
     * Retourne le nombre de structures gérées.
     * @return Le nombre de structures
     */
    public int getStructureCount() {
        return structures.size();
    }
    
    /**
     * Supprime toutes les structures du gestionnaire.
     */
    public void clear() {
        structures.clear();
    }
    
    /**
     * Détruit toutes les structures (vide leurs blocs) et les supprime du gestionnaire.
     */
    public void destroyAll() {
        for (Structure structure : structures) {
            structure.deleteStructure();
        }
        clear();
    }
}
