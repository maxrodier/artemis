package ca.artemis.engine.scene3;

import java.io.IOException;
import java.lang.annotation.Annotation;

import ca.artemis.engine.scene3.annotations.YamlView;
import ca.artemis.engine.scene3.loader.ViewLoader;

public abstract class View extends Layout {

    protected View(Builder<?> builder) {
        super(builder);
    }

    public abstract Controller<?> getController();

    public static <T extends View> T createView(Class<T> cls) {
        return createView(null, cls);
    }

    public static <T extends View> T createView(String id, Class<T> cls) {
        try {
            ViewLoader<T> loader = new ViewLoader<T>(getYamlViewPath(cls), cls);
            T view = loader.load();
            view.setId(id);
            return view;
        } catch (IOException e) {
            throw new AssertionError("Could not create view", e.getCause());
        }
    }

    private static String getYamlViewPath(Class<? extends View> cls) {
        Annotation[] annotations = cls.getDeclaredAnnotations();
        for(Annotation annotation : annotations){
            if(annotation instanceof YamlView){
                return YamlView.class.cast(annotation).path();
            }
        }
        throw new RuntimeException("YamlView annotation must be specified for Views");
    }
}
