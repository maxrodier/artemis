package ca.artemis.game;

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
        if(!engine.getWindow().isResizing()) {
            sessionManager.updatePublicSession();
            if(sessionManager.hasActiveSession()) {
                Session session = sessionManager.getActiveSession();
                session.update();
                session.render();
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
