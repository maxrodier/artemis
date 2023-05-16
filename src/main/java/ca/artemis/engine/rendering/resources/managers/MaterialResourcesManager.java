package ca.artemis.engine.rendering.resources.managers;

import java.util.HashMap;

import ca.artemis.engine.rendering.resources.Material;

public class MaterialResourcesManager extends ResourcesManager<Material> {
    
    public static MaterialResourcesManager instance = new MaterialResourcesManager();

    public MaterialResourcesManager() {
        super();
    }
    
    public void addMaterial(String name, Material material) {
        addResource(name, material);
    }
    
    public Material getMaterial(String name) {
        return getResource(name);
    }

    public HashMap<String, Material> getMaterials() {
        return getResources();
    }
}
