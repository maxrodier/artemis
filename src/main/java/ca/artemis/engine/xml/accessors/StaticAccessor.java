package ca.artemis.engine.xml.accessors;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;

public class StaticAccessor {
    
    private final Class<? extends Annotation> annotationCls;
    private final HashMap<String, Field> fields = new HashMap<>();

    public StaticAccessor(Class<?> cls) {
        this(cls, null);
    }

    public StaticAccessor(Class<?> cls, Class<? extends Annotation> annotationCls) {
        this.annotationCls = annotationCls;
        if(cls == null) {
            this.populateFields(cls);
        } else {
            this.populateFieldsFromAnnotation(cls);
        }
    }

    private void populateFields(Class<?> cls) {
        Field[] fields = cls.getFields();
        for (Field field : fields) {
            this.fields.put(field.getName(), field);
        }
    }

    private void populateFieldsFromAnnotation(Class<?> cls) {
        Field[] fields = cls.getFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for(Annotation annotation: annotations) {
                if(annotationCls.isInstance(annotation)) {
                    this.fields.put(field.getName(), field);
                }
            }
        }
    }

    public HashMap<String, Field> getFields() {
        return fields;
    }
}
