package ca.artemis.engine.xml.elements;

import java.util.LinkedList;

import javax.xml.stream.XMLStreamReader;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;

public abstract class Element {

    protected final XMLLoader loader;
    protected final Element parent;

    protected final LinkedList<Attribute> instancePropertyAttributes = new LinkedList<Attribute>();
    protected final LinkedList<Attribute> staticPropertyAttributes = new LinkedList<Attribute>();

    public Element(XMLLoader loader, Element parent) {
        this.loader = loader;
        this.parent = parent;
    }

    public void processStartElement(XMLStreamReader xmlStreamReader) throws LoadException {
        for (int i = 0, n = xmlStreamReader.getAttributeCount(); i < n; i++) {
            String prefix = xmlStreamReader.getAttributePrefix(i);
            String localName = xmlStreamReader.getAttributeLocalName(i);
            String value = xmlStreamReader.getAttributeValue(i);

            processAttribute(prefix, localName, value);
        }
    }

    public void processEndElement(XMLStreamReader xmlStreamReader) throws LoadException { }

    public void processCharacters(XMLStreamReader xmlStreamReader) throws LoadException {
        throw new LoadException("Unexpected characters in input stream.");
    }

    protected void processAttribute(String prefix, String localName, String value) throws LoadException {
        if(prefix == null || prefix.isEmpty()) {
            int i = localName.lastIndexOf('.');

            if(i == -1) { //This is an instance attribute
                instancePropertyAttributes.add(new Attribute(localName, null, value));
            } else { //This is a static attribute
                String name = localName.substring(i+1);
                Class<?> sourceType = loader.getType(localName.substring(0, i));
                staticPropertyAttributes.add(new Attribute(name, sourceType, value));
            }
        } else {

        }
    }

    public Element getParent() {
        return parent;
    }
}