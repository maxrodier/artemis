package ca.artemis.engine.xml.elements;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;

public class IncludeElement extends ValueElement {

    public IncludeElement(XMLLoader loader, Element parent) {
        super(loader, parent);
    }

    @Override
    public Object constructValue() throws LoadException {
        return null;
    }
}
