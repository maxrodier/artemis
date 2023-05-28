package ca.artemis.engine.sessions;

import ca.artemis.game.scenes.MainScene;

public class SessionManager implements AutoCloseable {
    
    private static SessionManager currentInstance;

    private Session sessionInWaiting = null;
    private Session activeSession = null;

    @Override
    public void close() throws Exception {
        if(sessionInWaiting != null) {
            sessionInWaiting.close();
        }
        if(activeSession != null) {
            activeSession.close();
        }
    }

    public void loadInitialSession() {
        createNewSession();
    }

    private void createNewSession() {
        this.sessionInWaiting = new Session(new MainScene()); //TODO: Externalize scene
    }

    public void updatePublicSession() {
        if(sessionInWaiting == null) {
            return;
        }

        Session oldSession = activeSession;
        this.activeSession = sessionInWaiting;
        this.activeSession.init();
        this.sessionInWaiting = null;

        if(oldSession != null) {
            try {
                oldSession.close();
            } catch(Exception e) {
                new RuntimeException("Could not close session.", e);
            }
        }
    }

    public Session getActiveSession() {
        return activeSession;
    }

    public boolean hasActiveSession() {
        return activeSession != null;
    }

    public static SessionManager instance() {
        if(currentInstance == null) {
            return new SessionManager();
        }

        return currentInstance;
    }
}
