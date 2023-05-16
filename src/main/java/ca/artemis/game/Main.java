package ca.artemis.game;

import org.lwjgl.glfw.GLFW;

import ca.artemis.engine.LowPolyEngine;

public class Main {
    
    public static void main(String[] args) throws Exception {
        try(GameManager gameManager = GameManager.instance()) {
            while(!gameManager.isCloseRequested()) {
                tempStuff();
                gameManager.update();
            }
        }
    }

    public static void tempStuff() {
        if(LowPolyEngine.instance().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_F11)) {
            LowPolyEngine.instance().getWindow().toggleFullscreen();
        }
    }
}
