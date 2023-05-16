package ca.artemis.engine.core.ecs;

import java.util.HashMap;

import ca.artemis.engine.core.EngineSettings;

public abstract class BaseSystem {
    
    protected HashMap<Integer, Entity> entities = new HashMap<>();

    public void init(EngineSettings engineSettings) { }

    public void initComponents() { }

    public void destroy() { }

    public void update(float delta) { }

    public void render(float delta) { }

    public void addEntity(Entity entity) { }
}
