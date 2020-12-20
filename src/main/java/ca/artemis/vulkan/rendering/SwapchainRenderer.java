package ca.artemis.vulkan.rendering;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.Configuration;
import ca.artemis.math.Vector2f;
import ca.artemis.math.Vector3f;
import ca.artemis.vulkan.commands.CommandBuffer;
import ca.artemis.vulkan.commands.CommandBufferUtils;
import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.commands.PresentInfo;
import ca.artemis.vulkan.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.commands.SubmitInfo;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.descriptor.DescriptorSet;
import ca.artemis.vulkan.memory.VulkanBuffer;
import ca.artemis.vulkan.memory.VulkanImageView;
import ca.artemis.vulkan.memory.VulkanSampler;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class SwapchainRenderer {

    private static final int VERTEX_SIZE = 5;
    private static final int VERTEX_BYTES = Float.BYTES*VERTEX_SIZE;
    private static final Vertex[] vertices = {
        new Vertex(new Vector3f(-1.0f, -1.0f, 0.0f), new Vector2f(0.0f, 0.0f)),
        new Vertex(new Vector3f(1.0f, -1.0f, 0.0f), new Vector2f(1.0f, 0.0f)),
        new Vertex(new Vector3f(1.0f, 1.0f, 0.0f), new Vector2f(1.0f, 1.0f)),
        new Vertex(new Vector3f(-1.0f, 1.0f, 0.0f), new Vector2f(0.0f, 1.0f))
    };

    private static class Vertex {

        public final Vector3f position;
        public final Vector2f texCoord;

        public Vertex(Vector3f position, Vector2f texCoord) {
            this.position = position;
            this.texCoord = texCoord;
        }
    }

    private static final int INDICES_LENGTH = 6;
    private static final int[] indices = { 
        0, 1, 2, 0, 2, 3 
    };

    private final VulkanBuffer vertexBuffer;
    private final VulkanBuffer indexBuffer;

    private final VulkanSampler textureSampler;
    private final CommandBuffer[] drawCommandBuffers;

    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanSemaphore renderCompletedSemaphore;
    private final VulkanFence renderFence;

    private final IntBuffer pImageIndex;
    private final SubmitInfo submitInfo;
    private final PresentInfo presentInfo;

    public SwapchainRenderer(VulkanContext context, CommandPool commandPool, Swapchain swapchain, VulkanImageView displayImageView) {
        this.vertexBuffer = createVertexBuffer(context.getMemoryAllocator(), context.getDevice(), commandPool);
        this.indexBuffer = createIndexBuffer(context.getMemoryAllocator(), context.getDevice(), commandPool);

        this.textureSampler = createTextureSampler(context.getDevice());
        
        this.updateDescriptorSets(context, swapchain, displayImageView, this.textureSampler);

        this.drawCommandBuffers = createCommandBuffers(context.getDevice(), commandPool, swapchain, this.vertexBuffer, this.indexBuffer);

        this.imageAcquiredSemaphore = new VulkanSemaphore(context.getDevice());
        this.renderCompletedSemaphore = new VulkanSemaphore(context.getDevice());
        
        this.renderFence = new VulkanFence(context.getDevice());

        this.pImageIndex = MemoryUtil.memCallocInt(1);
        this.submitInfo = new SubmitInfo(this.renderFence)
            .setWaitSemaphores(imageAcquiredSemaphore)
            .setWaitDstStageMask(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
            .setSignalSemaphores(renderCompletedSemaphore);
        this.presentInfo = new PresentInfo()
            .setWaitSemaphores(renderCompletedSemaphore)
            .setSwapchains(swapchain)
            .setImageIndexPointer(pImageIndex);
    }

    public void destroy(VulkanContext context) {
        this.presentInfo.destroy(context.getDevice());
        this.submitInfo.destroy();
        MemoryUtil.memFree(this.pImageIndex);
        this.renderFence.destroy(context.getDevice());
        this.renderCompletedSemaphore.destroy(context.getDevice());
        this.imageAcquiredSemaphore.destroy(context.getDevice());
        this.textureSampler.destroy(context.getDevice());
        this.indexBuffer.destroy(context.getMemoryAllocator());
        this.vertexBuffer.destroy(context.getMemoryAllocator());
    }

    private VulkanBuffer createVertexBuffer(VulkanMemoryAllocator allocator, VulkanDevice device, CommandPool commandPool) {
		int bufferLength = vertices.length;
    	int bufferSize = VERTEX_BYTES;
    	
		VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
			.setLength(bufferLength)
			.setSize(bufferSize)
			.setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
			.build(allocator);
    	
        PointerBuffer ppData = MemoryUtil.memAllocPointer(1);
        Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
    	FloatBuffer data = ppData.getFloatBuffer(0, vertices.length * VERTEX_SIZE);
    	
        for(int i = 0; i < vertices.length; i++) {
            Vertex vertex = vertices[i];
            int offset = i * VERTEX_SIZE;
    		data.put(offset+0, vertex.position.getX());
    		data.put(offset+1, vertex.position.getY());
    		data.put(offset+2, vertex.position.getZ());
    		data.put(offset+3, vertex.texCoord.getX());
    		data.put(offset+4, vertex.texCoord.getY());
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

    private VulkanBuffer createIndexBuffer(VulkanMemoryAllocator allocator, VulkanDevice device, CommandPool commandPool) {
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

		CommandBufferUtils.copyBuffer(device, device.getGraphicsQueue(), commandPool, stagingBuffer, indexBuffer, bufferLength * bufferSize);
        stagingBuffer.destroy(allocator);
        
        return indexBuffer;
    }

    private VulkanSampler createTextureSampler(VulkanDevice device) {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .build(device, 1);

        return textureSampler;
    }

    private void updateDescriptorSets(VulkanContext context, Swapchain swapchain, VulkanImageView textureImageView, VulkanSampler textureSampler) {
    	for(int i = 0; i < swapchain.getFramebuffers().length; i++) {
			DescriptorSet descriptorSet = swapchain.getDescriptorSet(i);
			descriptorSet.updateDescriptorImageBuffer(context, textureImageView, textureSampler, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
		}
    }

    private static CommandBuffer[] createCommandBuffers(VulkanDevice device, CommandPool commandPool, Swapchain swapchain, VulkanBuffer vertexBuffer, VulkanBuffer indexBuffer) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK11.vkResetCommandPool(device.getHandle(), commandPool.getHandle(), 0);

            CommandBuffer[] drawCommandBuffers = new CommandBuffer[swapchain.getFramebuffers().length];
            for(int i = 0; i < swapchain.getFramebuffers().length; i++) {
                
                PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(device, commandPool);
                
                VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1);
                pClearValues.get(0).color()
                    .float32(0, 36f/255f)
                    .float32(1, 10f/255f)
                    .float32(2, 48f/255f)
                    .float32(3, 1);

                commandBuffer.beginRecording(stack, 0);
                commandBuffer.beginRenderPassCmd(stack, swapchain.getRenderPass().getHandle(), swapchain.getFramebuffer(i).getHandle(), Configuration.windowWidth, Configuration.windowHeight, pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);

                commandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchain.getGraphicsPipeline());
                commandBuffer.bindVertexBufferCmd(stack, vertexBuffer);
                commandBuffer.bindIndexBufferCmd(indexBuffer);
                commandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchain.getGraphicsPipeline().getPipelineLayout(), swapchain.getDescriptorSet(i));
                commandBuffer.drawIndexedCmd(INDICES_LENGTH, 1);

                commandBuffer.endRenderPassCmd();
                commandBuffer.endRecording();
                
                drawCommandBuffers[i] = commandBuffer;
            }
            
            return drawCommandBuffers;
        }
    }

    public void draw(VulkanDevice device, Swapchain swapchain) {
        int error = KHRSwapchain.vkAcquireNextImageKHR(device.getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if (error != VK11.VK_SUCCESS) {
            throw new AssertionError("Failed to acquire next swapchain image");
        }


        submitInfo.setCommandBuffers(drawCommandBuffers[pImageIndex.get(0)]);

        this.renderFence.waitFor(device);
        submitInfo.submit(device, device.getGraphicsQueue());

        presentInfo.present(device);
    }

    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public VulkanSampler getTextureSampler() {
        return textureSampler;
    }

    public CommandBuffer[] getDrawCommandBuffers() {
        return drawCommandBuffers;
    }

    public CommandBuffer getDrawCommandBuffer(int index) {
        return drawCommandBuffers[index];
    }
}