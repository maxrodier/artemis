package ca.artemis.engine.util;

import ca.artemis.engine.rendering.resources.Material;

public class MaterialsUtils {
    
    public static Material loadMaterial(String materialName) {
        return new Material(materialName);
    }
}
