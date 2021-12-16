package ca.artemis.engine.xml.accessors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;

import ca.artemis.engine.xml.annotations.Autowired;

public class ControllerAccessor {
    
    private final Object controller;
    private final HashMap<String, Field> fields = new HashMap<>();

    public ControllerAccessor(Object controller) {
        this.controller = controller;
        this.populateControllerFields();
    }

    private void populateControllerFields() {
        Field[] fields = controller.getClass().getFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for(Annotation annotation: annotations) {
                if(Autowired.class.isInstance(annotation)) {
                    this.fields.put(field.getName(), field);
                }
            }
        }
    }

    public Object getController() {
        return controller;
    }

    public HashMap<String, Field> getControllerFields() {
        return fields;
    }
}
