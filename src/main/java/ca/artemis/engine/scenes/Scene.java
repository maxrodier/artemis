package ca.artemis.engine.scenes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Scene implements AutoCloseable {
    
    private List<GameObject> gameObjects = new ArrayList<>();

    public void init() {
        gameObjects.stream().forEach(go -> go.init());
    }

    public abstract void update();
    public abstract void render();

    public void addGameObjectToScene(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    @Override
    public void close() throws IOException {
        for(GameObject gameObject : gameObjects) {
            gameObject.close();
        }
    }
}
