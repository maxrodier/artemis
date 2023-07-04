package ca.artemis.engine.scenes;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameObject implements Closeable {
    
    private List<Component> components = new ArrayList<>();

    public void init() {
        components.stream().forEach(c -> c.init());
    }

    public void addComponent(Component component) {
        components.add(component);
        component.setParent(this);
    }

    public List<Component> getComponents() {
        return components;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for(Component component : components) {
            if(component.getClass().isAssignableFrom(componentClass)) {
                return componentClass.cast(component);
            }
        }
        throw new RuntimeException("Could not get component " + componentClass.getName() + " from game object.");
    }

    @Override
    public void close() throws IOException {
        for(Component component : components) {
            component.close();
        }
    }
}
