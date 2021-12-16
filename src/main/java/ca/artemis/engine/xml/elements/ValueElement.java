package ca.artemis.engine.xml.elements;

import java.lang.reflect.Field;

import javax.xml.stream.XMLStreamReader;

import ca.artemis.engine.xml.LoadException;
import ca.artemis.engine.xml.XMLLoader;
import ca.artemis.engine.xml.accessors.ObjectAccessor;
import ca.artemis.engine.xml.accessors.StaticAccessor;
import ca.artemis.engine.xml.adpters.ValueAdapter;
import ca.artemis.engine.xml.annotations.IDProperty;
import ca.artemis.engine.xml.annotations.Property;
import ca.artemis.engine.xml.annotations.StaticProperty;

public abstract class ValueElement extends Element {
        
    private String id;
    private Object value;

    public ValueElement(XMLLoader loader, Element parent) {
        super(loader, parent);
    }
    
    @Override
    public void processStartElement(XMLStreamReader xmlStreamReader) throws LoadException {
        super.processStartElement(xmlStreamReader);
        updateValue(constructValue());
        processValue();
    }

    @Override
    public void processEndElement(XMLStreamReader xmlStreamReader) throws LoadException {
        super.processEndElement(xmlStreamReader);
        processInstancePropertyAttributes();
        processStaticPropertyAttributes();

        if(parent != null && PropertyElement.class.isAssignableFrom(parent.getClass())) {
            PropertyElement parentPropertyElement = PropertyElement.class.cast(parent);
            if (parentPropertyElement.isCollection()) {
                parentPropertyElement.add(value);
            } else {
                parentPropertyElement.set(value);
            }
        }
    }

    @Override
    protected void processAttribute(String prefix, String localName, String value) throws LoadException {
        if(prefix != null && prefix.equals(XMLLoader.NAMESPACE_PREFIX) && localName.equals(XMLLoader.ID_TAG)) {
            for(int i = 0, n = value.length(); i < n; i++) {
                if(!Character.isJavaIdentifierPart(value.charAt(i))) {
                    throw new LoadException("Invalid identifier.");
                }
            }
            id = value;
        } else {
            super.processAttribute(prefix, localName, value);
        }
    }

    public void processInstancePropertyAttributes() throws LoadException {
        if (instancePropertyAttributes.size() > 0) {
            for (Attribute attribute : instancePropertyAttributes) {
                Field field = new ObjectAccessor(value, Property.class).getObjectFields().get(attribute.name);
                if(field != null) {
                    try {
                        field.set(value, ValueAdapter.createAdpapter(field.getType(), attribute.value).getValue());
                    } catch (IllegalAccessException e) {
                        throw new LoadException(e);       
                    }
                }
            }
        }
    }

    public void processStaticPropertyAttributes() throws LoadException {
        if (staticPropertyAttributes.size() > 0) {
            for (Attribute attribute : staticPropertyAttributes) {
                Field field = new StaticAccessor(attribute.sourceType, StaticProperty.class).getFields().get(attribute.name);
                if(field != null) {
                    
                }
            }
        }
    }

    public void updateValue(Object value) {
        this.value = value;
    }

    private void processValue() throws LoadException {
        if(id != null && !id.isEmpty()) {
            loader.getNamespace().put(id, value);

            IDProperty idProperty = value.getClass().getAnnotation(IDProperty.class);
            if(idProperty != null) {
                ObjectAccessor objectAccessor = new ObjectAccessor(value);
                Field idField = objectAccessor.getObjectFields().get(idProperty.value());
                
                if(idField != null) {
                    try {
                        idField.set(value, id);
                    } catch (IllegalAccessException e) {
                        throw new LoadException(e);       
                    }
                } else {
                    throw new LoadException("Field " + idProperty.value() + " must exists in class: " + value.getClass().getName() + "." );
                }
            }

            loader.injectFields(id, value);
        }

    }

    public abstract Object constructValue() throws LoadException;

    public Object getValue() {
        return value;
    }
}
