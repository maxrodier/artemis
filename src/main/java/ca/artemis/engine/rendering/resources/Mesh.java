package ca.artemis.engine.rendering.resources;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.api.vulkan.commands.CommandBufferUtils;
import ca.artemis.engine.api.vulkan.commands.CommandPool;
import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanMemoryAllocator;
import ca.artemis.engine.api.vulkan.memory.VulkanBuffer;
import ca.artemis.engine.core.math.Vector2f;
import ca.artemis.engine.core.math.Vector3f;
import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;

public class Mesh extends Resource {
    
    private final VulkanBuffer vertexBuffer;
    private final VulkanBuffer indexBuffer;

    public Mesh(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, Vertex[] vertices, Integer[] indices) {
        this.vertexBuffer = createVertexBuffer(device, allocator, commandPool, vertices);
        this.indexBuffer = createIndexBuffer(device, allocator, commandPool, indices);
    }

    @Override
    public void destroy(VulkanRenderingSystem renderingSystem) {
        this.vertexBuffer.destroy(renderingSystem.getAllocator());
        this.indexBuffer.destroy(renderingSystem.getAllocator());
    }

    private static VulkanBuffer createVertexBuffer(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, Vertex[] vertices) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            
            int bufferLength = vertices.length;
            int bufferSize = Vertex.SIZE * Float.BYTES;
    
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build(allocator);
    
            PointerBuffer ppData = stack.mallocPointer(1);
            Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
            FloatBuffer data = ppData.getFloatBuffer(0, vertices.length * Vertex.SIZE);

            for(int i = 0; i < vertices.length; i++) {
                Vertex vertex = vertices[i];
                int offset = i * Vertex.SIZE;
                data.put(offset+0, vertex.position.x);
                data.put(offset+1, vertex.position.y);
                data.put(offset+2, vertex.position.z);
                data.put(offset+3, vertex.texCoord.x);
                data.put(offset+4, vertex.texCoord.y);
                data.put(offset+5, vertex.normal.x);
                data.put(offset+6, vertex.normal.y);
                data.put(offset+7, vertex.normal.z);
            }

            Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());

            VulkanBuffer vertexBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK11.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_GPU_ONLY)
                .build(allocator);

            CommandBufferUtils.copyBuffer(device, device.getGraphicsQueue(), commandPool, stagingBuffer, vertexBuffer, bufferLength * bufferSize);
            stagingBuffer.destroy(allocator);
            
            return vertexBuffer;
        }
    }

    private static VulkanBuffer createIndexBuffer(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, Integer[] indices) {
        try(MemoryStack stack = MemoryStack.stackPush()) {

            int bufferLength = indices.length;
            int bufferSize = Integer.BYTES;

            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(bufferLength)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build(allocator);

            PointerBuffer ppData = stack.mallocPointer(1);
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

            CommandBufferUtils.copyBuffer(device, device.getGraphicsQueue(), commandPool, stagingBuffer, indexBuffer, bufferLength * bufferSize);
            stagingBuffer.destroy(allocator);

            return indexBuffer;
        }
    }

    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public static class Vertex {

        public static final int SIZE = 3 + 2 + 3;

        private final Vector3f position;
        private final Vector2f texCoord;
        private final Vector3f normal;

        public Vertex(Vector3f position, Vector2f texCoord, Vector3f normal) {
            this.position = position;
            this.texCoord = texCoord;
            this.normal = normal;
        }

        public Vector3f getPosition() {
            return position;
        }

        public Vector2f getTexCoord() {
            return texCoord;
        }

        public Vector3f getNormal() {
            return normal;
        }
    }
}
