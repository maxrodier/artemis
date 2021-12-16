package ca.artemis.engine.scene3;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ChildrenDeserializer extends StdDeserializer<List<Node>> {

    public ChildrenDeserializer() { 
        this(null); 
    } 

    public ChildrenDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public List<Node> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException{
        List<Node> nodes = new ArrayList<>();

        ObjectCodec codec = parser.getCodec();
        JsonNode array = codec.readTree(parser);

        if(!array.isArray()) {
            throw new JsonParseException(parser, "Array expected");
        } 

        Iterator<JsonNode> it = array.elements();
        while(it.hasNext()) {
            JsonNode node = it.next();

            try {
                Class<?> cls = Class.forName(node.get("class").asText());
                if(Base.class.isAssignableFrom(cls)) {
                    nodes.add(Node.class.cast(codec.treeToValue(node, cls)));
                } else if(View.class.isAssignableFrom(cls)) {
                    Method createMethod = View.class.getMethod("createView", String.class, Class.class);
                    nodes.add(Node.class.cast(createMethod.invoke(null, node.get("id").asText(), cls)));
                } else {
                    throw new JsonParseException(parser, "Children must be assignable from base or component");
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return nodes;
    }
    
}
