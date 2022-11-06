package ca.artemis.engine.core;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    
    private String name;
    private Transform transform;
    private List<Component> components;

    public GameObject(String name) {
        this(name, new Transform());
    }

    public GameObject(String name, Transform transform) {
        this.name = name;
        this.transform = transform;
        this.components = new ArrayList<>();
    }

    public <T extends Component> T getComponent(Class<T> componenClass) {
        for(Component component: components) {
            if(componenClass.isAssignableFrom(component.getClass())) {
                try {
                    return componenClass.cast(component);
                } catch(Exception e) {
                    throw new AssertionError(e);
                }
            }
        }
        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componenClass) {
        for(int i = 0; i < components.size(); i++) {
            if(componenClass.isAssignableFrom(components.get(i).getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component component) {
        this.components.add(component);
        component.setParent(this);
    }

    public void update(float dt) {
        for(int i = 0; i < components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    public void start() {
        for(int i = 0; i < components.size(); i++) {
            components.get(i).start();
        }
    }
    
    public String getName() {
        return name;
    }
}
