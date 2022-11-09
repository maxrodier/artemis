package ca.artemis.engine.core.scenes;

import java.util.ArrayList;
import java.util.List;

import ca.artemis.engine.core.Camera;
import ca.artemis.engine.core.GameObject;

public abstract class Scene {
    
    protected Camera camera;
    protected List<GameObject> gameObjects;
    
    private boolean isRunning;

    public Scene() {
        this.gameObjects = new ArrayList<>();
    }

    public void init() {
        
    }

    public void start() {
        if(isRunning) {
            return;
        }

        for(GameObject gameObject: gameObjects) {
            gameObject.start();
        }

        isRunning = true;
    }

    public void addGameObjectToScene(GameObject gameObject) {
        gameObjects.add(gameObject);
        if(isRunning) {
            gameObject.start();
        }
    }

    public abstract void update(float dt, int frameIndex);
}
