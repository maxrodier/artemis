package ca.artemis.engine.vulkan.core.mesh;

import ca.artemis.engine.scenes.Component;

public class MeshComponent extends Component {

    private Mesh mesh;

    public MeshComponent(Mesh mesh) {
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }
}
