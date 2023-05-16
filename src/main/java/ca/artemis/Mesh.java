package ca.artemis;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkQueue;

import ca.artemis.Vertex.VertexKind;
import ca.artemis.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.api.memory.VulkanBuffer;

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
                case POS_UV_NORMAL:
                    offset = i * vertexKind.size;
                    data.put(offset+0, vertex.pos.x);
                    data.put(offset+1, vertex.pos.y);
                    data.put(offset+2, vertex.pos.z);
                    data.put(offset+3, vertex.texCoord.x);
                    data.put(offset+4, vertex.texCoord.y);
                    data.put(offset+5, vertex.normal.x);
                    data.put(offset+6, vertex.normal.y);
                    data.put(offset+7, vertex.normal.z);
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
}
