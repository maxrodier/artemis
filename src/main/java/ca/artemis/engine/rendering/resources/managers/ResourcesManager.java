package ca.artemis.engine.rendering.resources.managers;

import java.util.HashMap;

import ca.artemis.engine.rendering.resources.Resource;

public abstract class ResourcesManager<T extends Resource> {
    
    protected HashMap<String, T> resources = new HashMap<>();

    protected void addResource(String name, T resource) {
        resources.put(name, resource);
    }

    protected T getResource(String name) {
        return resources.get(name);
    }

    protected HashMap<String, T> getResources() {
        return resources;
    }
}
