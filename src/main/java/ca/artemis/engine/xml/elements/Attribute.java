package ca.artemis.engine.xml.elements;

public class Attribute {

    public final String name;
    public final Class<?> sourceType;
    public final String value;

    public Attribute(String name, Class<?> sourceType, String value) {
        this.name = name;
        this.sourceType = sourceType;
        this.value = value;
    }
}