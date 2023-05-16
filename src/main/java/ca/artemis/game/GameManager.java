package ca.artemis.game;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.LowPolyRenderingEngine;
import ca.artemis.engine.sessions.Session;
import ca.artemis.engine.sessions.SessionManager;

public class GameManager implements AutoCloseable {
    
    private static GameManager currentInstance;

    private final LowPolyEngine engine;
    private final LowPolyRenderingEngine renderingEngine;
    private final SessionManager sessionManager;

    public GameManager() {
        this.engine = LowPolyEngine.instance();
        this.renderingEngine = LowPolyRenderingEngine.instance();
        this.sessionManager = SessionManager.instance();

        sessionManager.loadInitialSession();
    }

    @Override
    public void close() throws Exception {
        renderingEngine.close();
        engine.close();
    }

    public void update() {
        sessionManager.updatePublicSession();
        if (sessionManager.hasActiveSession()) {
			Session session = sessionManager.getActiveSession();
			session.update();
			session.getActiveScene().render();
		}
        engine.update();
    }

    public boolean isCloseRequested() {
        return engine.getWindow().isCloseRequested();
    }

    public static GameManager instance() {
        if(currentInstance == null) {
            init();
        }

        return currentInstance;
    }

    private static void init() {
        currentInstance = new GameManager();
    }
}
