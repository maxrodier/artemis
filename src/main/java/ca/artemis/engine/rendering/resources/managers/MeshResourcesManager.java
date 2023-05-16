package ca.artemis.engine.rendering.resources.managers;

import java.util.HashMap;

import ca.artemis.engine.rendering.resources.Mesh;

public class MeshResourcesManager extends ResourcesManager<Mesh> {
    
    public static MeshResourcesManager instance = new MeshResourcesManager();

    public MeshResourcesManager() {
        super();
    }
    
    public void addMesh(String name, Mesh mesh) {
        addResource(name, mesh);
    }
    
    public Mesh getMesh(String name) {
        return getResource(name);
    }

    public HashMap<String, Mesh> getMeshes() {
        return getResources();
    }
}
