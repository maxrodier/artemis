package ca.artemis.engine.core.ecs;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    
    private final int id;
    private final Map<Class<? extends Component>, Component> components;

    public Entity(int id) {
        this.id = id;
        this.components = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    public void addComponent(Component component) {
        components.put(component.getClass(), component);
    }

    /* 
    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
    }
    */

    public boolean hasComponent(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }
}
