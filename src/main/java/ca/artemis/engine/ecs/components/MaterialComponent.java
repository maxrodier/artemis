package ca.artemis.engine.ecs.components;

import ca.artemis.engine.core.ecs.Component;
import ca.artemis.engine.rendering.resources.Material;

public class MaterialComponent extends Component {
    
    private String name;
    private Material material;

    public MaterialComponent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
