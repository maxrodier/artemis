package ca.artemis.engine.scene3;

public abstract class Controller<T extends View> {
    
    protected final T view;

    protected Controller(T view) {
        this.view = view;
    }

    public T getView() {
        return view;
    };
}
