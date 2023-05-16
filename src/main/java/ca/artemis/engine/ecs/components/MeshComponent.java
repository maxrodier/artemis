package ca.artemis.engine.ecs.components;

import ca.artemis.engine.core.ecs.Component;
import ca.artemis.engine.rendering.resources.Mesh;

public class MeshComponent extends Component {
    
    private String name;
    private Mesh mesh;

    public MeshComponent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }
}
