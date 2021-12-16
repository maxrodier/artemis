package ca.artemis.engine.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ca.artemis.engine.xml.accessors.ControllerAccessor;
import ca.artemis.engine.xml.elements.Element;
import ca.artemis.engine.xml.elements.IncludeElement;
import ca.artemis.engine.xml.elements.InstanceElement;
import ca.artemis.engine.xml.elements.PropertyElement;
import ca.artemis.engine.xml.elements.RootElement;

public class XMLLoader {

    public static final String NAMESPACE_PREFIX = "system";
    public static final String INCLUDE_TAG = "include";
    public static final String ROOT_TAG = "root";
    public static final String ID_TAG = "id";
    public static final String ROOT_TYPE_ATTRIBUTE = "type";

    private final String location;
    private final ClassLoader classLoader;

    private Object root;
    private ControllerAccessor controllerAccessor;

    private Element current;

    private List<String> packages = new LinkedList<>();
    private Map<String, Class<?>> classes = new HashMap<>();
    private Map<String, Object> namespace = new HashMap<>();


    public XMLLoader(String location) {
        this(location, null);
    }

    public XMLLoader(String location, ClassLoader classLoader) {
        this.location = location;
        this.classLoader = classLoader;
    } 

    public void load() throws IOException {
        if(location == null) {
            throw  new IllegalStateException("Location is not set.");
        }

        InputStream inputStream = Files.newInputStream(Paths.get(location));

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty("javax.xml.stream.isCoalescing", true);

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.defaultCharset());
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStreamReader);

            current = null;

            while(xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();

                switch (event) {
                    case XMLStreamConstants.PROCESSING_INSTRUCTION: {
                        processProcessingInstruction(xmlStreamReader);
                        break;
                    }
                    case XMLStreamConstants.START_ELEMENT: {
                        processStartElement(xmlStreamReader);
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        processEndElement(xmlStreamReader);
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        processCharacters(xmlStreamReader);
                        break;
                    }
                    case XMLStreamConstants.COMMENT: {
                        break;
                    }
                    case XMLStreamConstants.END_DOCUMENT: {
                        break;
                    }
                    default: {
                        throw new LoadException("Unsupported XML Event: " + event + ".");
                    }
                }
            }
        } catch(XMLStreamException e) {
            throw new LoadException(e);
        }
    }

    private void processProcessingInstruction(XMLStreamReader xmlStreamReader) throws LoadException {
        String piTarget = xmlStreamReader.getPITarget().trim();
        if (piTarget.equals("import")) {
            processImport(xmlStreamReader);
        } else {
            throw new LoadException("Unsupported Processing Instruction: " + piTarget + ".");
        }
    }

    private void processImport(XMLStreamReader xmlStreamReader) throws LoadException {
        String target = xmlStreamReader.getPIData().trim();
        if (target.endsWith(".*")) {
            importPackage(target.substring(0, target.length() - 2));
        } else {
            importClass(target);
        }
    }

    private void processStartElement(XMLStreamReader xmlStreamReader) throws LoadException{
        createElement(xmlStreamReader);
        current.processStartElement(xmlStreamReader);
    }

    private void createElement(XMLStreamReader xmlStreamReader) throws LoadException {
        String prefix = xmlStreamReader.getPrefix();
        String localName = xmlStreamReader.getLocalName();

        if(prefix == null || prefix.isEmpty()) {
            if (Character.isLowerCase(localName.charAt(0))) { //This is a property
                current = new PropertyElement(this, current, localName);
            } else { //This is a type
                current = new InstanceElement(this, current, getType(localName));
            }
        } else if(prefix.equals(NAMESPACE_PREFIX)) { //This is a system element
            if(localName.equals(INCLUDE_TAG)) {
                current = new IncludeElement(this, current);
            } else if(localName.equals(ROOT_TAG)) {
                current = new RootElement(this, current);
            } else {
                throw new LoadException("Unsupported system element: " + prefix + ":" + localName + ".");
            }
        } else {
            throw new LoadException("Unsupported namespace prefix: " + prefix + ".");
        }
    }

    private void processEndElement(XMLStreamReader xmlStreamReader) throws LoadException{
        current.processEndElement(xmlStreamReader);
        current = current.getParent();
    }

    private void processCharacters(XMLStreamReader xmlStreamReader) throws LoadException{
        if(!xmlStreamReader.isWhiteSpace()) {
            current.processCharacters(xmlStreamReader);
        }
    }

    private void importPackage(String name) throws LoadException {
        packages.add(name);
    }

    private void importClass(String name) throws LoadException {
        int i = name.lastIndexOf('.');
        if(i == -1) {
            throw new LoadException("Import is not a fully-qualified class name: " +  name + ".");
        }

        try {
            classes.put(name.substring(i+1), loadType(name));
        } catch(ClassNotFoundException e) {
            throw new LoadException(e);
        }
    }

    public Class<?> loadType(String fullyQualifiedClassName) throws ClassNotFoundException {
        return getClassLoader().loadClass(fullyQualifiedClassName);
    }

    public Class<?> getType(String name) throws LoadException {
        Class<?> type = classes.get(name); //Look for type in cache
        if(type != null) {
            return type;
        }

        if(Character.isLowerCase(name.charAt(0))) { //This is a fully-qualified class name
            int i = name.lastIndexOf('.');
            if(i == -1) {
                throw new LoadException("Invalid type: " +  name + ".");
            }

            try {
                type = loadType(name);
            } catch(ClassNotFoundException e) {
                throw new LoadException(e);
            }
        } else { //This is an unqualified class name
            for(String packageName : packages) {
                try {
                    type = loadType(packageName.concat(".").concat(name));
                } catch(ClassNotFoundException e) { }

                if(type != null) {
                    break;
                }
            }

            if(type == null) {
                throw new LoadException("Type not found: " + name + ".");
            }
        }

        classes.put(name, type); //Cache class
        return type;
    }

    public void injectFields(String fieldName, Object value) throws LoadException {
        Object controller = controllerAccessor.getController();
        if(controller != null && fieldName != null) {
            Field field = controllerAccessor.getControllerFields().get(fieldName);
            if(field != null) {
                try {
                    field.set(controller, value);
                } catch (IllegalAccessException e) {
                    throw new LoadException(e);
                }
            }
        }
    }

    private ClassLoader getClassLoader() {
        if(classLoader == null)
            return this.getClass().getClassLoader();
        return classLoader;
    }

    public Object getRoot() {
        return root;
    }

    public void setRoot(Object root) {
        this.root = root;
    }

    public void setController(Object controller) {
        this.controllerAccessor = new ControllerAccessor(controller);
    }

    public Map<String, Object> getNamespace() {
        return namespace;
    }
}