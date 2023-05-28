package ca.artemis.engine.vulkan.core.mesh;

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

import ca.artemis.FileUtils;
import ca.artemis.Util;
import ca.artemis.engine.maths.Vector2f;
import ca.artemis.engine.maths.Vector3f;
import ca.artemis.engine.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;

public class Mesh {
    
    private final VulkanBuffer vertexBuffer;
    private final VulkanBuffer indexBuffer;

    public Mesh(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, Vertex[] vertices, Integer[] indices, VertexKind vertexKind) {
        this.vertexBuffer = createVertexBuffer(device, allocator, graphicsQueue, commandPool, vertices, vertexKind);
        this.indexBuffer = createIndexBuffer(device, allocator, graphicsQueue, commandPool, indices);
    }

    public void destroy(VulkanMemoryAllocator allocator) {
        this.indexBuffer.destroy(allocator);
        this.vertexBuffer.destroy(allocator);
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
    	
        PointerBuffer ppData = MemoryUtil.memAllocPointer(1); //TODO: Change this for stack allocation
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
                case POS_COLOUR_UV:
                    offset = i * vertexKind.size;
                    data.put(offset+0, vertex.pos.x);
                    data.put(offset+1, vertex.pos.y);
                    data.put(offset+2, vertex.pos.z);
                    data.put(offset+3, vertex.colour.x);
                    data.put(offset+4, vertex.colour.y);
                    data.put(offset+5, vertex.colour.z);
                    data.put(offset+6, vertex.texCoord.x);
                    data.put(offset+7, vertex.texCoord.y);
                    break;
                default:
                    throw new AssertionError("Cannot use default vertex kind");
            }
    	}
    	
        Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
		
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

    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public static Mesh loadModel(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, String filePath) {
	    List<Vector3f> positions = new ArrayList<>();
	    List<Vector2f> texCoords = new ArrayList<>();;
	    List<OBJIndex> indices = new ArrayList<>();;

		try {
            for(String line : FileUtils.readLines(filePath)) {
				String[] tokens = line.split(" ");
				tokens = Util.RemoveEmptyStrings(tokens);

				if(tokens.length == 0 || tokens[0].equals("#"))
					continue;
				else if(tokens[0].equals("v"))
				{
					positions.add(new Vector3f(Float.valueOf(tokens[1]),
							Float.valueOf(tokens[2]),
							Float.valueOf(tokens[3])));
				}
				else if(tokens[0].equals("vt"))
				{
					texCoords.add(new Vector2f(Float.valueOf(tokens[1]),
							1.0f - Float.valueOf(tokens[2])));
				}
				else if(tokens[0].equals("f"))
				{
					indices.add(parseOBJIndex(tokens[1]));
					indices.add(parseOBJIndex(tokens[2]));
					indices.add(parseOBJIndex(tokens[3]));
				}
			}

            return toIndexedModel(device, allocator, graphicsQueue, commandPool, positions, texCoords, indices);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to load model");
		}
	}

	private static OBJIndex parseOBJIndex(String token){
		String[] values = token.split("/");

		OBJIndex result = new OBJIndex();
		result.SetVertexIndex(Integer.parseInt(values[0]) - 1);

		if(values.length > 1)
		{
			if(!values[1].isEmpty())
			{
				result.SetTexCoordIndex(Integer.parseInt(values[1]) - 1);
			}
		}

		return result;
	}

    public static Mesh toIndexedModel(VulkanDevice device, VulkanMemoryAllocator allocator, VkQueue graphicsQueue, CommandPool commandPool, List<Vector3f> positions, List<Vector2f> texCoords, List<OBJIndex> indices) {
		HashMap<OBJIndex, Integer> resultIndexMap = new HashMap<OBJIndex, Integer>();
        List<Integer> result = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();

		for(int i = 0; i < indices.size(); i++) {
			OBJIndex currentIndex = indices.get(i);

			Vector3f currentPosition = positions.get(currentIndex.GetVertexIndex());
			Vector2f currentTexCoord = texCoords.get(currentIndex.GetTexCoordIndex());

			Integer modelVertexIndex = resultIndexMap.get(currentIndex);

			if(modelVertexIndex == null) {
				modelVertexIndex = vertices.size();
				resultIndexMap.put(currentIndex, modelVertexIndex);

                vertices.add(new Vertex(currentPosition, currentTexCoord));
			}

			result.add(modelVertexIndex);
		}

		return new Mesh(device, allocator, graphicsQueue, commandPool,vertices.toArray(new Vertex[vertices.size()]), result.toArray(new Integer[result.size()]), VertexKind.POS_COLOUR_UV);
	}

    public static class OBJIndex {
        private int vertexIndex;
        private int texCoordIndex;

        public int GetVertexIndex()   { return vertexIndex; }
        public int GetTexCoordIndex() { return texCoordIndex; }

        public void SetVertexIndex(int val)   { vertexIndex = val; }
        public void SetTexCoordIndex(int val) { texCoordIndex = val; }

        @Override
        public boolean equals(Object obj)
        {
            OBJIndex index = (OBJIndex)obj;

            return vertexIndex == index.vertexIndex
                    && texCoordIndex == index.texCoordIndex;
        }

        @Override
        public int hashCode() {
            final int BASE = 17;
            final int MULTIPLIER = 31;

            int result = BASE;

            result = MULTIPLIER * result + vertexIndex;
            result = MULTIPLIER * result + texCoordIndex;

            return result;
        }
    }
}
