package ca.artemis.engine.xml.elements;

import java.lang.reflect.Field;
import java.util.Collection;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;
import ca.artemis.engine.xml.accessors.ObjectAccessor;
import ca.artemis.engine.xml.annotations.Property;

public class PropertyElement extends Element {

    private final Object value;
    private final Field field;

    public PropertyElement(XMLLoader loader, Element parent, String name) throws LoadException {
        super(loader, parent);

        if(parent == null) {
            throw new LoadException("Invalid root element.");
        }

        if(!ValueElement.class.isAssignableFrom(parent.getClass())) {
            throw new LoadException("Parent must be of ValueElement, but is " + parent.getClass() + ".");
        }

        this.value = ValueElement.class.cast(parent).getValue();
        this.field = fetchField(name, value);
    }

    public Field fetchField(String name, Object value) throws LoadException {
        ObjectAccessor objectAccessor = new ObjectAccessor(value, Property.class);
        Field field  = objectAccessor.getObjectFields().get(name);

        if(field == null) {
            throw new LoadException("Could not find field " + name + " with annotation " + Property.class + " in " + value.getClass() + ".");
        }

        return field;
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(field.getType());
    }

    public void set(Object value) throws LoadException {
        try {
            field.set(this.value, value);
        } catch (IllegalAccessException e) {
            throw new LoadException(e);       
        }
    }

    @SuppressWarnings("unchecked")
    public void add(Object value) throws LoadException {
        Collection<Object> collection;
        try {
            collection = Collection.class.cast(field.get(this.value));
            collection.add(value);
        } catch (IllegalAccessException e) {
            throw new LoadException(e);
        }
    }
}