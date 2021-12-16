package ca.artemis.engine.scene2;

import java.io.IOException;

import ca.artemis.engine.scene2.layout.Pane;
import ca.artemis.engine.xml.XMLLoader;

public class Test extends Pane {
    
    public TestController testController = new TestController();

    public Test() {

        XMLLoader xmlLoader = new XMLLoader("src/main/resources/game/test.xml");
        xmlLoader.setRoot(this);
        xmlLoader.setController(testController);

        try {
            xmlLoader.load();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
