package ca.artemis.engine.vulkan.core.mesh;

import java.io.Closeable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkQueue;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.maths.Vector2f;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.utils.FileUtils;
import ca.artemis.engine.utils.Tuple;
import ca.artemis.engine.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;

public class Mesh implements Closeable {
    
    public final Integer[] indices;
    public final Vertex[] vertices;
    public final VertexKind vertexKind;

    private VulkanBuffer indexBuffer;
    private VulkanBuffer vertexBuffer;
    
    public Mesh(String path) {
        Tuple<Integer[], Vertex[]> tuple = Mesh.loadModel(path);
        this.indices = tuple.getKey();
        this.vertices = tuple.getValue();
        this.vertexKind = VertexKind.POS_NORMAL_UV;
    }

    public Mesh(Integer[] indices, Vertex[] vertices, VertexKind vertexKind) {
        this.indices = indices;
        this.vertices = vertices;
        this.vertexKind = vertexKind;
    }

    @Override
    public void close() {
        VulkanMemoryAllocator memoryAllocator = LowPolyEngine.instance().getContext().getMemoryAllocator();

        if(indexBuffer != null) {
            indexBuffer.destroy(memoryAllocator);
        }
        if(vertexBuffer != null) {
            vertexBuffer.destroy(memoryAllocator);
        }
    }

    public void init() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();
        VulkanMemoryAllocator memoryAllocator = LowPolyEngine.instance().getContext().getMemoryAllocator();
        CommandPool commandPool = LowPolyEngine.instance().getContext().getCommandPool();

        indexBuffer = createIndexBuffer(device, memoryAllocator, device.getGraphicsQueue(), commandPool, indices);
        vertexBuffer = createVertexBuffer(device, memoryAllocator, device.getGraphicsQueue(), commandPool, vertices, vertexKind);
    }

    private static VulkanBuffer createIndexBuffer(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, Integer[] indices) {
		int bufferLength = indices.length;
    	int bufferSize = Integer.BYTES;
        
        VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
            .setLength(bufferLength)
            .setSize(bufferSize)
            .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
            .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
            .build(allocator);

    	PointerBuffer ppData = MemoryUtil.memAllocPointer(1);
        Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
    	IntBuffer data = ppData.getIntBuffer(0, indices.length);
    	for(int i = 0; i < indices.length; i++) {
    		data.put(i, indices[i]);
    	}
    	
        Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
        ppData.free();

		VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
			.build(allocator);

		CommandBufferUtils.copyBuffer(device, graphicsQueue, commandPool,stagingBuffer, indexBuffer, bufferLength * bufferSize);
        stagingBuffer.destroy(allocator);

        return indexBuffer;
    }

    private static VulkanBuffer createVertexBuffer(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, Vertex[] vertices, VertexKind vertexKind) {
		int bufferLength = vertices.length;
    	int bufferSize = vertexKind.size * Float.BYTES;
    	
		VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
			.build(allocator);
    	
        PointerBuffer ppData = MemoryUtil.memAllocPointer(1);
        Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
    	FloatBuffer data = ppData.getFloatBuffer(0, vertices.length * vertexKind.size);
    	
        for(int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            int offset;
            switch(vertexKind) {
                case POS_COLOUR:
                    offset = i * vertexKind.size;
                    data.put(offset+0, vertex.pos.x);
                    data.put(offset+1, vertex.pos.y);
                    data.put(offset+2, vertex.pos.z);
                    data.put(offset+3, vertex.colour.x);
                    data.put(offset+4, vertex.colour.y);
                    data.put(offset+5, vertex.colour.z);
                    break;
                case POS_UV:
                    offset = i * vertexKind.size;
                    data.put(offset+0, vertex.pos.x);
                    data.put(offset+1, vertex.pos.y);
                    data.put(offset+2, vertex.pos.z);
                    data.put(offset+3, vertex.texCoord.x);
                    data.put(offset+4, vertex.texCoord.y);
                    break;
                case POS_NORMAL_UV:
                    offset = i * vertexKind.size;
                    data.put(offset+0, vertex.pos.x);
                    data.put(offset+1, vertex.pos.y);
                    data.put(offset+2, vertex.pos.z);
                    data.put(offset+3, vertex.normal.x);
                    data.put(offset+4, vertex.normal.y);
                    data.put(offset+5, vertex.normal.z);
                    data.put(offset+6, vertex.texCoord.x);
                    data.put(offset+7, vertex.texCoord.y);
                    break;
                default:
                    throw new AssertionError("Cannot use default vertex kind");
            }
    	}
    	
        Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
        ppData.free();

		VulkanBuffer vertexBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
			.build(allocator);

		CommandBufferUtils.copyBuffer(device, graphicsQueue, commandPool, stagingBuffer, vertexBuffer, bufferLength * bufferSize);
        stagingBuffer.destroy(allocator);

        return vertexBuffer;
    }


    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    } 

    public static Tuple<Integer[], Vertex[]> loadModel(String path) {
        List<Vector3f> positions = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> textCoords = new ArrayList<>();

        HashMap<String, Tuple<Integer, Vertex>> indexedVertices = new HashMap<>();
        List<Integer> indices = new ArrayList<>();
        
        List<String> lines;
        try {
            lines = FileUtils.readLines(path);
        } catch(IOException e) {
            throw new RuntimeException("Could not load model", e);
        }
        
        for(String line : lines) {
            String[] tokens = line.split(" ");

            if(tokens[0].equals("v")) {
                positions.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tokens[0].equals("vn")) {
                normals.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])));
            } else if(tokens[0].equals("vt")) {
                textCoords.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
            } else if(tokens[0].equals("f")) {
                for(int i = 1; i < tokens.length; i++) {
                    String token = tokens[i];
                    if(indexedVertices.containsKey(token)) {
                        indices.add(indexedVertices.get(token).getKey());
                    } else {
                        int index = indexedVertices.size();
                        String[] _tokens = token.split("/");

                        indexedVertices.put(token, new Tuple<Integer, Vertex>(index, new Vertex(
                            positions.get(Integer.parseInt(_tokens[0]) - 1), 
                            normals.get(Integer.parseInt(_tokens[2]) - 1), 
                            textCoords.get(Integer.parseInt(_tokens[1]) - 1)
                        )));
                        indices.add(index);
                    }
                }
            }
        }

        Vertex[] vertices = new Vertex[indexedVertices.size()];
        indexedVertices.values().stream().forEach(t -> vertices[t.getKey()] = t.getValue());

        return new Tuple<Integer[], Vertex[]>(indices.toArray(new Integer[indices.size()]) , vertices);
    }
}
