package ca.artemis.engine.xml.elements;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;

public class RootElement extends ValueElement {

    private String type;

    public RootElement(XMLLoader loader, Element parent) {
        super(loader, parent);
    }

    @Override
    public void processAttribute(String prefix, String localName, String value) throws LoadException {
        if ((prefix == null || prefix.isEmpty()) && localName.equals(XMLLoader.ROOT_TYPE_ATTRIBUTE)) {
            type = value;
            return;
        }
        super.processAttribute(prefix, localName, value);
    }

    @Override
    public Object constructValue() throws LoadException {
        if (type == null) {
            throw new LoadException("Attribute " + XMLLoader.ROOT_TYPE_ATTRIBUTE + " is required.");
        }
        
        Class<?> type = loader.getType(this.type);

        if (type == null) {
            throw new LoadException(this.type + " is not a valid type.");
        }

        Object root = loader.getRoot();

        if(root == null) {
            throw new LoadException("Root as not been set in XMLLoader.");
        }
        
        if (!type.isAssignableFrom(root.getClass())) {
            throw new LoadException("Root is not an instance of " + type.getName() + ".");
        }

        return root;
    }
}