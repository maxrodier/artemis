package ca.artemis.engine.scene3.loader;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import ca.artemis.engine.scene3.Controller;
import ca.artemis.engine.scene3.Node;
import ca.artemis.engine.scene3.View;
import ca.artemis.engine.scene3.annotations.Autowired;

public class ViewLoader<T extends Node> {

    private final String path;
    private final Class<T> cls;

    public ViewLoader(String path, Class<T> cls) {
        this.path = path;
        this.cls = cls;
    }

    public T load() throws IOException {
        T node = new YAMLMapper(new YAMLFactory()).readValue(Files.readString(Paths.get(path)), cls);

        if(View.class.isAssignableFrom(node.getClass())) {
            Controller<?> controller = View.class.cast(node).getController();

            HashMap<String, Field> fields = new HashMap<>();
            for(Field field : controller.getClass().getDeclaredFields()) {
                Annotation[] annotations = field.getDeclaredAnnotations();
                for(Annotation annotation : annotations){
                    if(annotation instanceof Autowired){
                        fields.put(field.getName(), field);
                    }
                }
            }

            for(Node child: node.getChildren()) {
                assignFields(child, controller, fields);
            }
        }

        return node;

    }

    private void assignFields(Node node, Controller<?> controller, HashMap<String, Field> fields) {
        if(!View.class.isAssignableFrom(node.getClass())) {
            for(Node child: node.getChildren()) {
                assignFields(child, controller, fields);
            }
        }

        Field field = fields.get(node.getId());
        if(field == null)
            return;
        try {
            field.set(controller, node);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}