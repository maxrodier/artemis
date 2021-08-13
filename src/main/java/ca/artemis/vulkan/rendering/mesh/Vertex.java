package ca.artemis.vulkan.rendering.mesh;

import ca.artemis.math.Vector2f;
import ca.artemis.math.Vector3f;

public class Vertex {
    
    private final Vector3f position;
    private final Vector3f colour;
    private final Vector2f texCoord;

    public Vertex(Vector3f position, Vector3f colour) {

        this(position, colour, new Vector2f(0, 0));
    }

    public Vertex(Vector3f position, Vector2f texCoord) {
        this(position, new Vector3f(0, 0, 0), texCoord);
    }

    public Vertex(Vector3f position, Vector3f colour, Vector2f texCoord) {
        this.position = position;
        this.colour = colour;
        this.texCoord = texCoord;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getColour() {
        return colour;
    }

    public Vector2f getTexCoord() {
        return texCoord;
    }

    public static enum VertexKind {
        POS_COLOUR(6),
        POS_UV(5),
        POS_COLOUR_UV(8);

        public final int size;

        private VertexKind(int size) {
            this. size = size;
        }
    }
}
