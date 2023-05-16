package ca.artemis.engine.rendering;

public class LowPolyRenderingEngine implements AutoCloseable {

    private static LowPolyRenderingEngine currentInstance;

    private final LowPolyRenderer renderer;

    private LowPolyRenderingEngine() {
        this.renderer = new LowPolyRenderer(); 
    }

    @Override
    public void close() throws Exception {
    }

    public static LowPolyRenderingEngine instance() {
        if(currentInstance == null) {
            init();
        }

        return currentInstance;
    }

    private static void init() {
        currentInstance = new LowPolyRenderingEngine();
    }
}
