package ca.artemis.engine.core.scenes;

import ca.artemis.game.Constants;

public class LevelScene extends Scene {

    public LevelScene() {
        System.out.println("Inside LevelScene");
        Constants.rClearColor = 1.0f;
        Constants.gClearColor = 1.0f;
        Constants.bClearColor = 1.0f;
    }

    @Override
    public void update(float dt, int frameIndex) {
        // TODO Auto-generated method stub
        
    }
}
