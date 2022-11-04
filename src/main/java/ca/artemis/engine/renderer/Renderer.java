package ca.artemis.engine.renderer;

import ca.artemis.engine.core.Assertions;
import ca.artemis.engine.core.Assertions.Level;

public class Renderer {

    private Backend backend;
    
    public void initialize() {

    }

    public void destroy() {

    }

    public void onResized() {

    }

    public boolean drawFrame(FrameInfo frameInfo) {

        if(backend.beginFrame()) {

            //draw

            return Assertions.assertTrue(backend.endFrame(), Level.ERROR, "Failed to end frame.");
        }

        return true;
    }
}
