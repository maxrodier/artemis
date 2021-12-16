package ca.artemis;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import ca.artemis.engine.scene2.Test;
import ca.artemis.engine.scene2.layout.ColumnConstraints;

public class Main {
    
    public static void main(String[] args) throws IOException, XMLStreamException {


        Test test = new Test();
        for(ColumnConstraints columnConstraints : test.testController.rootGridPane.columnConstraints) {
            System.out.println(columnConstraints.hgrow + ": " + columnConstraints.minWidth + ": " + columnConstraints.maxWidth + ": " + columnConstraints.prefWidth);
        }

        /*
        RenderingEngine renderingEngine = new RenderingEngine();
        TestGame game = new TestGame(renderingEngine);
        renderingEngine.mainLoop();
        game.destroy(renderingEngine);
        renderingEngine.destroy();
        */
        
    }
}
