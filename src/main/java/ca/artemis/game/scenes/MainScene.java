package ca.artemis.game.scenes;

import ca.artemis.engine.scenes.Scene;

public class MainScene extends Scene {

    @Override
    public void update() {
        System.out.println("Scene Update");
    }

    @Override
    public void render() {
        System.out.println("Scene render");
    }
}
