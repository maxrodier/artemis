package ca.artemis.engine.vulkan.api.descriptor;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.math.Matrix4f;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;
import ca.artemis.engine.vulkan.programs.ShaderProgram;
import ca.artemis.engine.vulkan.rendering.RenderingEngine;

public class UniformBufferObject { //TODO: Make this class abstract to handle texture and unfirom and to push float buffers correctly
    
    private List<VulkanBuffer> uniformBuffers; //One per frame in flight

    public UniformBufferObject(VulkanDevice device, VulkanMemoryAllocator allocator, ShaderProgram shaderProgram, int bufferLength) {
        createUniformBuffers(allocator, bufferLength * Float.BYTES);
    }

    private void createUniformBuffers(VulkanMemoryAllocator allocator, int bufferSize) {
        uniformBuffers = new ArrayList<>();

        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            VulkanBuffer indexBuffer = new VulkanBuffer.Builder()
                .setLength(1)
                .setSize(bufferSize)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
                .build(VulkanContext.getContext().getMemoryAllocator());

            uniformBuffers.add(indexBuffer);
        }
    }

    public void updateBuffer(MemoryStack stack, VulkanMemoryAllocator allocator, Matrix4f projection, Matrix4f view, int frameIndex) {
        PointerBuffer ppData = stack.callocPointer(1);
        Vma.vmaMapMemory(allocator.getHandle(), uniformBuffers.get(frameIndex).getAllocationHandle(), ppData);
        FloatBuffer data = ppData.getFloatBuffer(0, 48);
        projection.get(data, 0);
        view.get(data, 16);
        Vma.vmaUnmapMemory(allocator.getHandle(), uniformBuffers.get(frameIndex).getAllocationHandle());
    }

    public VulkanBuffer getUniformBuffer(int index) {
        return uniformBuffers.get(index);
    }
}
