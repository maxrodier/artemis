package ca.artemis.engine.scene2.layout;

import java.util.ArrayList;
import java.util.List;

import ca.artemis.engine.xml.annotations.Property;

public class Pane {
    
    @Property
    public List<Object> children = new ArrayList<>();
}
