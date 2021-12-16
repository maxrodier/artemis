package ca.artemis.engine.xml.elements;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;

public class InstanceElement extends ValueElement {

    private final Class<?> type;

    public InstanceElement(XMLLoader loader, Element parent, Class<?> type) {
        super(loader, parent);

        this.type = type;
    }

    @Override
    public Object constructValue() throws LoadException {
        if (type == null) {
            throw new LoadException("Invalid type: " + type + ".");
        }

        Object value;
        try {
            value = type.getDeclaredConstructor().newInstance();
        } catch(Exception e) {
            throw new LoadException(e);
        }
        return value;
    }
}

