package ca.artemis.engine.xml.accessors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;

public class ObjectAccessor {
        
    private final Object object;
    private final Class<? extends Annotation> cls;
    private final HashMap<String, Field> fields = new HashMap<>();

    public ObjectAccessor(Object object) {
        this(object, null);
    }

    public ObjectAccessor(Object object, Class<? extends Annotation> cls) {
        this.object = object;
        this.cls = cls;
        if(cls == null) {
            this.populateObjectFields();
        } else {
            this.populateObjectFieldsFromAnnotation();
        }
    }

    private void populateObjectFields() {
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            this.fields.put(field.getName(), field);
        }
    }

    private void populateObjectFieldsFromAnnotation() {
        Field[] fields = object.getClass().getFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for(Annotation annotation: annotations) {
                if(cls.isInstance(annotation)) {
                    this.fields.put(field.getName(), field);
                }
            }
        }
    }

    public Object getObject() {
        return object;
    }

    public HashMap<String, Field> getObjectFields() {
        return fields;
    }
}
