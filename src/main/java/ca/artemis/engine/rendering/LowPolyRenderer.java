package ca.artemis.engine.rendering;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.scenes.Scene;

public class LowPolyRenderer implements AutoCloseable {
    
    public LowPolyRenderer() {
        LowPolyEngine engine = LowPolyEngine.instance();

    }

    public void renderScene(Scene scene, LowPolyRenderSystem renderSystem) {
        //RenderCalls
    }

    @Override
    public void close() throws Exception {

    }
}
