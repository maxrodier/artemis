package ca.artemis.engine.vulkan.core.mesh;

import java.util.HashMap;
import java.util.Map;

import ca.artemis.engine.maths.Vector2f;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;

public class Quad extends Mesh {

    private static final Map<Vertex.VertexKind, Vertex[]> vertices = new HashMap<>();
    
    static {
        vertices.put(VertexKind.POS_COLOUR, new Vertex[] {
            new Vertex(new Vector3f(-1f, -1f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f)),
            new Vertex(new Vector3f(1f, -1f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f)),
            new Vertex(new Vector3f(1f, 1f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
            new Vertex(new Vector3f(-1f, 1f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f)),
        });

        vertices.put(VertexKind.POS_UV, new Vertex[] {
            new Vertex(new Vector3f(-1f, -1f, 0.0f), new Vector2f(0.0f, 0.0f)),
            new Vertex(new Vector3f(1f, -1f, 0.0f), new Vector2f(1.0f, 1.0f)),
            new Vertex(new Vector3f(1f, 1f, 0.0f), new Vector2f(1.0f, 1.0f)),
            new Vertex(new Vector3f(-1f, 1f, 0.0f), new Vector2f(0.0f, 1.0f)),
        });
    }

    private static final Integer[] indices = {
        0, 1, 2, 2, 3, 0
    };

    public Quad(VulkanContext context, VertexKind vertexKind) {
        super(context.getDevice(), context.getMemoryAllocator(), context.getDevice().getGraphicsQueue(), context.getCommandPool(), vertices.get(vertexKind), indices, vertexKind);
    }
}
