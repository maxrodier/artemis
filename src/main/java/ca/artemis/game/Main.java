package ca.artemis.game;

import ca.artemis.engine.core.CoreEngine;

public class Main {
    
    public static void main(String[] args) {
        CoreEngine coreEngine = new CoreEngine(new TestGame());
        coreEngine.run();
    }
}
