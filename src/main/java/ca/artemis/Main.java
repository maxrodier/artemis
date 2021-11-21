package ca.artemis;

import ca.artemis.game.TestGame;
import ca.artemis.vulkan.rendering.RenderingEngine;

public class Main {
    
    public static void main(String[] args) {
        RenderingEngine renderingEngine = new RenderingEngine();
        TestGame game = new TestGame(renderingEngine);
        renderingEngine.mainLoop();
        game.destroy(renderingEngine);
        renderingEngine.destroy();
    }
}
