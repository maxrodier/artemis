package ca.artemis.vulkan.rendering;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;

import ca.artemis.Configuration;
import ca.artemis.framework.math.Vector2f;
import ca.artemis.framework.math.Vector3f;
import ca.artemis.vulkan.commands.CommandBufferUtils;
import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.descriptor.DescriptorSet;
import ca.artemis.vulkan.memory.VulkanBuffer;
import ca.artemis.vulkan.memory.VulkanImage;
import ca.artemis.vulkan.memory.VulkanImageView;
import ca.artemis.vulkan.memory.VulkanSampler;

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
    private final VulkanImage textureImage;
    private final VulkanImageView textureImageView;
    private final VulkanSampler textureSampler;
    private final VkCommandBuffer[] drawCommandBuffers;

    public SwapchainRenderer(VulkanContext context, CommandPool commandPool, Swapchain swapchain) {
        this.vertexBuffer = createVertexBuffer(context.getMemoryAllocator(), context.getDevice(), commandPool);
        this.indexBuffer = createIndexBuffer(context.getMemoryAllocator(), context.getDevice(), commandPool);
        this.textureImage = createTextureImage(context.getMemoryAllocator(), context.getDevice(), commandPool, "src/main/resources/textures/wood.png");
        this.textureImageView = createTextureImageView(context.getDevice(), this.textureImage);
        this.textureSampler = createTextureSampler(context.getDevice());
        this.updateDescriptorSets(context, swapchain, this.textureImageView, this.textureSampler);
        this.drawCommandBuffers = createCommandBuffers(context.getDevice(), commandPool, swapchain, this.vertexBuffer, this.indexBuffer);
    }

    public void destroy(VulkanContext context) {
        this.textureSampler.destroy(context.getDevice());
        this.textureImageView.destroy(context.getDevice());
        this.textureImage.destroy(context.getMemoryAllocator());
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

    private VulkanImage createTextureImage(VulkanMemoryAllocator allocator, VulkanDevice device, CommandPool commandPool, String filePath) {
        try {
            BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(Paths.get(filePath)));
            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            int imageSize = bufferedImage.getWidth() * bufferedImage.getHeight();
            
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(imageSize)
                .setSize(Integer.BYTES)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build(allocator);
            
            PointerBuffer ppData = MemoryUtil.memAllocPointer(1);
            Vma.vmaMapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
            IntBuffer data = ppData.getIntBuffer(imageSize);
            for(int y = 0; y < imageHeight; y++) {
                for(int x = 0; x < imageWidth; x++) {
                    data.put(x + y * imageWidth, bufferedImage.getRGB(x, y));
                }
            }
            Vma.vmaUnmapMemory(allocator.getHandle(), stagingBuffer.getAllocationHandle());
            
            VulkanImage textureImage = new VulkanImage.Builder()
                .setExtentWidth(imageWidth)
                .setExtentHeight(imageHeight)
                .setExtentDepth(1)
                .setMipLevels(1)
                .setArrayLayers(1)
                .setFormat(VK11.VK_FORMAT_B8G8R8A8_UNORM)
                .setTilling(VK11.VK_IMAGE_TILING_OPTIMAL)
                .setUsage(VK11.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK11.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK11.VK_IMAGE_USAGE_SAMPLED_BIT)
                .build(allocator);

            CommandBufferUtils.transitionImageLayout(device, device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_UNDEFINED, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 1);
            CommandBufferUtils.copyBufferToImage(device, device.getGraphicsQueue(), commandPool, stagingBuffer, textureImage);
            CommandBufferUtils.transitionImageLayout(device, device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 1);
            stagingBuffer.destroy(allocator);

            return textureImage;
		} catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Could not load texture");
		}
    }
    
    private VulkanImageView createTextureImageView(VulkanDevice device, VulkanImage textureImage) {
        VulkanImageView textureImageView = new VulkanImageView.Builder()
            .setImage(textureImage.getHandle())
            .setViewType(VK11.VK_IMAGE_VIEW_TYPE_2D)
            .setFormat(VK11.VK_FORMAT_B8G8R8A8_UNORM)
            .setAspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
            .setBaseMipLevel(0)
            .setLevelCount(1)
            .setBaseArrayLayer(0)
            .setLayerCount(1)
            .build(device);

        return textureImageView;
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

    private VkCommandBuffer[] createCommandBuffers(VulkanDevice device, CommandPool commandPool, Swapchain swapchain, VulkanBuffer vertexBuffer, VulkanBuffer indexBuffer) {
    	VK11.vkResetCommandPool(device.getHandle(), commandPool.getHandle(), 0);

        VkCommandBuffer[] drawCommandBuffers = new VkCommandBuffer[swapchain.getFramebuffers().length];
    	for(int i = 0; i < swapchain.getFramebuffers().length; i++) {
    		
			PrimaryCommandBuffer commandBuffer = new PrimaryCommandBuffer(device, commandPool);
			
            VkClearValue.Buffer pClearValues = VkClearValue.create(2);
            pClearValues.get(0).color()
                .float32(0, 36f/255f)
                .float32(1, 10f/255f)
                .float32(2, 48f/255f)
                .float32(3, 1);
            pClearValues.get(1).depthStencil()
            	.depth(1.0f)
            	.stencil(0);

			SecondaryCommandBuffer secondaryCommandBuffer1 = new SecondaryCommandBuffer(device, commandPool);
			secondaryCommandBuffer1.beginRecording(VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, swapchain.getRenderPass(), swapchain.getFramebuffer(i));
			secondaryCommandBuffer1.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchain.getGraphicsPipeline());
			secondaryCommandBuffer1.bindVertexBufferCmd(vertexBuffer);
			secondaryCommandBuffer1.bindIndexBufferCmd(indexBuffer);
			secondaryCommandBuffer1.bindDescriptorSetsCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchain.getGraphicsPipeline().getPipelineLayout(), new DescriptorSet[] {swapchain.getDescriptorSet(i)});
			secondaryCommandBuffer1.drawIndexedCmd(INDICES_LENGTH, 1);
			secondaryCommandBuffer1.endRecording();

            commandBuffer.beginRecording(0);
			commandBuffer.beginRenderPassCmd(swapchain.getRenderPass().getHandle(), swapchain.getFramebuffer(i).getHandle(), Configuration.windowWidth, Configuration.windowHeight, pClearValues, VK11.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

			VK11.vkCmdExecuteCommands(commandBuffer.getCommandBuffer(), MemoryUtil.memAllocPointer(1).put(0, secondaryCommandBuffer1.getHandle()));

			commandBuffer.endRenderPassCmd();
            commandBuffer.endRecording();
            
            drawCommandBuffers[i] = commandBuffer.getCommandBuffer();
        }
        
        return drawCommandBuffers;
    }

    public VulkanBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public VulkanBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public VulkanImage getTextureImage() {
        return textureImage;
    }

    public VulkanImageView getTextureImageView() {
        return textureImageView;
    }

    public VulkanSampler getTextureSampler() {
        return textureSampler;
    }

    public VkCommandBuffer[] getDrawCommandBuffers() {
        return drawCommandBuffers;
    }

    public VkCommandBuffer getDrawCommandBuffer(int index) {
        return drawCommandBuffers[index];
    }
}