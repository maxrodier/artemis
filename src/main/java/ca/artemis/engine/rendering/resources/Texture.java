package ca.artemis.engine.rendering.resources;

import java.awt.image.BufferedImage;
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
import ca.artemis.engine.api.vulkan.memory.VulkanImage;
import ca.artemis.engine.api.vulkan.memory.VulkanImageBundle;
import ca.artemis.engine.api.vulkan.memory.VulkanImageView;
import ca.artemis.engine.api.vulkan.memory.VulkanSampler;
import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;

public class Texture extends Resource {
    
    private final VulkanImageBundle imageBundle;
    private final VulkanSampler sampler;

    public Texture(VulkanDevice device, VulkanMemoryAllocator allocator, CommandPool commandPool, BufferedImage bufferedImage, boolean multiSampling) {
        this.imageBundle = createTexutureImageBundle(device, allocator, commandPool, bufferedImage, multiSampling);
        this.sampler = createTextureSampler(device, this.imageBundle.getImage());
    }

    @Override
    public void destroy(VulkanRenderingSystem renderingSystem) {
        this.sampler.destroy(renderingSystem.getDevice());
        this.imageBundle.destroy(renderingSystem.getDevice(), renderingSystem.getAllocator());
    }

    private static VulkanImageBundle createTexutureImageBundle(VulkanDevice device, VulkanMemoryAllocator memoryAllocator, CommandPool commandPool, BufferedImage bufferedImage, boolean multiSampling) {
        VulkanImage textureImage = createTextureImage(device, memoryAllocator, commandPool, bufferedImage, multiSampling);
        VulkanImageView textureImageView = createTextureImageView(device, textureImage);

        return new VulkanImageBundle(textureImage, textureImageView);
    }

    private static VulkanImage createTextureImage(VulkanDevice device, VulkanMemoryAllocator memoryAllocator, CommandPool commandPool, BufferedImage bufferedImage, boolean multiSampling) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            
            int imageWidth = bufferedImage.getWidth();
            int imageHeight = bufferedImage.getHeight();
            int imageSize = bufferedImage.getWidth() * bufferedImage.getHeight();
            int mipLevels = multiSampling ? (int)Math.floor(Math.log(Math.max(imageWidth, imageHeight))) + 1 : 1;
            
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(imageSize)
                .setSize(Integer.BYTES)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_ONLY)
                .build(memoryAllocator);
            
            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle(), ppData);
            IntBuffer data = ppData.getIntBuffer(imageSize);
            for(int y = 0; y < imageHeight; y++) {
                for(int x = 0; x < imageWidth; x++) {
                    data.put(x + y * imageWidth, bufferedImage.getRGB(x, y));
                }
            }
            Vma.vmaUnmapMemory(memoryAllocator.getHandle(), stagingBuffer.getAllocationHandle());
            
            VulkanImage textureImage = new VulkanImage.Builder()
                .setExtentWidth(imageWidth)
                .setExtentHeight(imageHeight)
                .setExtentDepth(1)
                .setMipLevels(mipLevels)
                .setArrayLayers(1)
                .setFormat(VK11.VK_FORMAT_B8G8R8A8_UNORM)
                .setTilling(VK11.VK_IMAGE_TILING_OPTIMAL)
                .setUsage(VK11.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK11.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK11.VK_IMAGE_USAGE_SAMPLED_BIT)
                .build(memoryAllocator);

            CommandBufferUtils.transitionImageLayout(device, device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_UNDEFINED, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);
            CommandBufferUtils.copyBufferToImage(device, device.getGraphicsQueue(), commandPool, stagingBuffer, textureImage);
            if(multiSampling) {
                CommandBufferUtils.generateMipmaps(device, device.getGraphicsQueue(), commandPool, textureImage, mipLevels);
            } else {
                CommandBufferUtils.transitionImageLayout(device, device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, mipLevels);
            }
            stagingBuffer.destroy(memoryAllocator);

            return textureImage;
		}
    }

    private static VulkanImageView createTextureImageView(VulkanDevice device, VulkanImage textureImage) {
        VulkanImageView textureImageView = new VulkanImageView.Builder()
            .setImage(textureImage.getHandle())
            .setViewType(VK11.VK_IMAGE_VIEW_TYPE_2D)
            .setFormat(VK11.VK_FORMAT_B8G8R8A8_UNORM)
            .setAspectMask(VK11.VK_IMAGE_ASPECT_COLOR_BIT)
            .setBaseMipLevel(0)
            .setLevelCount(textureImage.getMipLevels())
            .setBaseArrayLayer(0)
            .setLayerCount(1)
            .build(device);

        return textureImageView;
    }

    private static VulkanSampler createTextureSampler(VulkanDevice device, VulkanImage textureImage) {
        VulkanSampler textureSampler = new VulkanSampler.Builder()
            .setMaxLod(textureImage.getMipLevels())
            .build(device);

        return textureSampler;
    }

    public VulkanImageBundle getImageBundle() {
        return imageBundle;
    }

    public VulkanSampler getSampler() {
        return sampler;
    }
}
