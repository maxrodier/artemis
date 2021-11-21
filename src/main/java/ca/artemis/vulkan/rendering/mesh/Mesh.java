package ca.artemis.vulkan.rendering.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.api.memory.VulkanBuffer;
import ca.artemis.vulkan.rendering.mesh.Vertex.VertexKind;

public class Mesh {
    
    private final VulkanBuffer vertexBuffer;
    private final VulkanBuffer indexBuffer;

    public Mesh(Vertex[] vertices, Integer[] indices, VertexKind vertexKind) {
        this.vertexBuffer = createVertexBuffer(vertices, vertexKind);
        this.indexBuffer = createIndexBuffer(indices);
    }

    public void destroy() {
        this.indexBuffer.destroy();
        this.vertexBuffer.destroy();
    }

    private static VulkanBuffer createVertexBuffer(Vertex[] vertices, VertexKind vertexKind) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanMemoryAllocator memoryAllocator = VulkanContext.getContext().getMemoryAllocator();

            int bufferLength = vertices.length;
            int bufferSize = vertexKind.size * Float.BYTES;
            
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build();
            
            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, vertices.length * vertexKind.size);
            
            for(int i = 0; i < vertices.length; i++) {
                Vertex vertex = vertices[i];
                int offset;
                switch(vertexKind) {
                    case POS_COLOUR:
                        offset = i * vertexKind.size;
                        data.put(offset+0, vertex.getPosition().getX());
                        data.put(offset+1, vertex.getPosition().getY());
                        data.put(offset+2, vertex.getPosition().getZ());
                        data.put(offset+3, vertex.getColour().getX());
                        data.put(offset+4, vertex.getColour().getY());
                        data.put(offset+5, vertex.getColour().getZ());
                        break;
                    case POS_UV:
                        offset = i * vertexKind.size;
                        data.put(offset+0, vertex.getPosition().getX());
                        data.put(offset+1, vertex.getPosition().getY());
                        data.put(offset+2, vertex.getPosition().getZ());
                        data.put(offset+3, vertex.getTexCoord().getX());
                        data.put(offset+4, vertex.getTexCoord().getY());
                        break;
                    case POS_COLOUR_UV:
                        offset = i * vertexKind.size;
                        data.put(offset+0, vertex.getPosition().getX());
                        data.put(offset+1, vertex.getPosition().getY());
                        data.put(offset+2, vertex.getPosition().getZ());
                        data.put(offset+3, vertex.getColour().getX());
                        data.put(offset+4, vertex.getColour().getY());
                        data.put(offset+5, vertex.getColour().getZ());
                        data.put(offset+6, vertex.getTexCoord().getX());
                        data.put(offset+7, vertex.getTexCoord().getY());
                        break;
                    default:
                        throw new AssertionError("Cannot use default vertex kind");
                }
            }
            
            Vma.vmaUnmapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle());
            
            VulkanBuffer vertexBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
                .build();


            VulkanDevice device = VulkanContext.getContext().getDevice();
            CommandPool commandPool = VulkanContext.getContext().getCommandPool();
            CommandBufferUtils.copyBuffer(device.getGraphicsQueue(), commandPool, stagingBuffer, vertexBuffer, bufferLength * bufferSize);
            stagingBuffer.destroy();
            
            return vertexBuffer;
        }
    }

    private static VulkanBuffer createIndexBuffer(Integer[] indices) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanMemoryAllocator memoryAllocator = VulkanContext.getContext().getMemoryAllocator();
            int bufferLength = indices.length;
            int bufferSize = Integer.BYTES;
            
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build();

            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
            IntBuffer data = ppData.getIntBuffer(0, indices.length);
            for(int i = 0; i < indices.length; i++) {
                data.put(i, indices[i]);
            }
            
            Vma.vmaUnmapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle());
            
            VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
                .build();

            VulkanDevice device = VulkanContext.getContext().getDevice();
            CommandPool commandPool = VulkanContext.getContext().getCommandPool();
            CommandBufferUtils.copyBuffer(device.getGraphicsQueue(), commandPool,stagingBuffer, indexBuffer, bufferLength * bufferSize);
            stagingBuffer.destroy();
            
            return indexBuffer;
        }
    }

    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    }
}
