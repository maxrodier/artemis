package ca.artemis.engine.scene2.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.artemis.engine.xml.annotations.Property;
import ca.artemis.engine.xml.annotations.StaticProperty;

public class GridPane {

    @StaticProperty
    public HashMap<Object, Integer> rowIndex;
    @StaticProperty
    public HashMap<Object, Integer> columnIndex;

    @Property
    public List<ColumnConstraints> columnConstraints = new ArrayList<>();
    @Property
    public List<RowConstraints> rowConstraints = new ArrayList<>();

    @Property
    public List<Object> children = new ArrayList<>();
}
