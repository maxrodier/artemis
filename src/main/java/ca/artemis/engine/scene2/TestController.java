package ca.artemis.engine.scene2;

import ca.artemis.engine.scene2.control.Button;
import ca.artemis.engine.scene2.layout.GridPane;
import ca.artemis.engine.xml.annotations.Autowired;

public class TestController {
    
    @Autowired
    public Test root;

    @Autowired
    public Button button1;
    @Autowired
    public Button button2;
    @Autowired
    public Button button3;
    @Autowired
    public Button button4;

    @Autowired
    public GridPane rootGridPane;
}
