package ca.artemis.vulkan.rendering.mesh;

import ca.artemis.math.Vector2f;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.rendering.mesh.Vertex.VertexKind;

public class Quad extends Mesh {
    
    private static final Vector3f[] positions = {
        new Vector3f(-1.0f, -1.0f, 0.0f),
        new Vector3f(1.0f, -1.0f, 0.0f),
        new Vector3f(1.0f, 1.0f, 0.0f),
        new Vector3f(-1.0f, 1.0f, 0.0f)
    };

    private static final Vector2f[] texCoords = {
        new Vector2f(0.0f, 0.0f),
        new Vector2f(1.0f, 0.0f),
        new Vector2f(1.0f, 1.0f),
        new Vector2f(0.0f, 1.0f)
    };

    private static final int[] indices = { 
        0, 1, 2, 0, 2, 3 
    };

    public Quad(VulkanContext context) {
        super(context, getDefaultVertices(), indices, VertexKind.POS_UV);
    }

    public Quad(VulkanContext context, int x, int y, int width, int height, Vector3f colour) {
        this(context, getPositions(x, y, width, height), colour, null, VertexKind.POS_COLOUR);
    }

    public Quad(VulkanContext context, int x, int y, int width, int height, Vector2f u, Vector2f v) {
        this(context, getPositions(x, y, width, height), null, getTexCoords(u, v), VertexKind.POS_COLOUR);
    }

    public Quad(VulkanContext context, int x, int y, int width, int height, Vector3f colour, Vector2f u, Vector2f v) {
        this(context, getPositions(x, y, width, height), colour, getTexCoords(u, v), VertexKind.POS_COLOUR_UV);
    }

    public Quad(VulkanContext context, Vector3f[] positions, Vector3f colour, Vector2f[] texCoords, VertexKind vertexKind) {
        super(context, getVertices(positions, colour, texCoords, vertexKind), indices, vertexKind);
    }

    private static Vertex[] getDefaultVertices() {
        return new Vertex[] {
            new Vertex(positions[0], texCoords[0]),
            new Vertex(positions[1], texCoords[1]),
            new Vertex(positions[2], texCoords[2]),
            new Vertex(positions[3], texCoords[3]),
        };
    }

    private static Vector3f[] getPositions(int x, int y, int width, int height) {
        return new Vector3f[] {
            new Vector3f(x, y, 0.0f),
            new Vector3f(x + width, y, 0.0f),
            new Vector3f(x + width, y + height, 0.0f),
            new Vector3f(x, y + height, 0.0f)
        };
    }

    private static Vector2f[] getTexCoords(Vector2f u, Vector2f v) {
        return new Vector2f[] {
            new Vector2f(u.getX(), v.getX()),
            new Vector2f(u.getY(), v.getX()),
            new Vector2f(u.getY(), v.getY()),
            new Vector2f(u.getX(), v.getY())
        };
    }

    private static final Vertex[] getVertices(Vector3f[] positions, Vector3f colour, Vector2f[] texCoords, VertexKind vertexKind) {
        switch(vertexKind) {
            case POS_COLOUR:
                return new Vertex[] {
                    new Vertex(positions[0], colour),
                    new Vertex(positions[1], colour),
                    new Vertex(positions[2], colour),
                    new Vertex(positions[3], colour)
                };
            case POS_UV:
                return new Vertex[] {
                    new Vertex(positions[0], texCoords[0]),
                    new Vertex(positions[1], texCoords[1]),
                    new Vertex(positions[2], texCoords[2]),
                    new Vertex(positions[3], texCoords[3])
                };
            case POS_COLOUR_UV:
                return new Vertex[] {
                    new Vertex(positions[0], colour, texCoords[0]),
                    new Vertex(positions[1], colour, texCoords[1]),
                    new Vertex(positions[2], colour, texCoords[2]),
                    new Vertex(positions[3], colour, texCoords[3])
                };
            default:
                throw new AssertionError("Cannot use default vertex kind");

        }
    }
}
