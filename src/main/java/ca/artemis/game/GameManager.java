package ca.artemis.game;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.sessions.Session;
import ca.artemis.engine.sessions.SessionManager;
import ca.artemis.game.rendering.LowPolyRenderingEngine;

public class GameManager implements AutoCloseable {

    private static GameManager currentInstance;

    private final LowPolyEngine engine;
    private final LowPolyRenderingEngine renderingEngine;
    private final SessionManager sessionManager;

    private long lastTime = System.nanoTime();
    private float frameCounter = 0;
    private int frames;

    private GameManager() {
        this.engine = LowPolyEngine.instance();
        this.renderingEngine = LowPolyRenderingEngine.instance();
        this.sessionManager = SessionManager.instance();

        sessionManager.loadInitialSession();
    }

    @Override
    public void close() throws Exception {
        VK11.vkDeviceWaitIdle(engine.getContext().getDevice().getHandle());

        sessionManager.close();
        renderingEngine.close();
        engine.close();
    }

    public void update() {
        if(sessionManager.updatePublicSession()) {
            lastTime = System.nanoTime();
        }
        if(sessionManager.hasActiveSession()) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                if(renderingEngine.update(stack)) {
                    long currentTime = System.nanoTime();
                    float deltaTime = (currentTime - lastTime) / 1_000_000_000f;
                    lastTime = currentTime;
                    frameCounter += deltaTime;


                    Session session = sessionManager.getActiveSession();
                    session.update(stack, deltaTime);

                    if(frameCounter >= 1) {
                        System.out.println(frames + " fps");
                        frames = 0;
                        frameCounter -= 1;
                    }

                    renderingEngine.render(stack);
                    frames++;
                }
            }
        }
        engine.update();
    }

    public boolean isCloseRequested() {
        return engine.getWindow().isCloseRequested();
    }

    public static GameManager instance() {
        if(currentInstance == null) {
            currentInstance = new GameManager();
        }
        return currentInstance;
    }
}
