package ca.artemis.engine.vulkan.api.memory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.core.utils.FileUtils;
import ca.artemis.engine.vulkan.api.commands.CommandBufferUtils;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;

public class VulkanTexture {
    
    private final VulkanImageBundle imageBundle;
    private final VulkanSampler sampler;

    public VulkanTexture(VulkanContext context, String filePath, boolean multiSampling) {
        this.imageBundle = createTexutureImageBundle(context.getDevice(), context.getMemoryAllocator(), context.getCommandPool(), filePath, multiSampling);
        this.sampler = createTextureSampler(context.getDevice(), this.imageBundle.getImage());
    }

    public void destroy(VulkanContext context) {
        this.sampler.destroy(context.getDevice());
        this.imageBundle.destroy(context.getDevice(), context.getMemoryAllocator());
    }

    public VulkanImageBundle getImageBundle() {
        return imageBundle;
    }

    public VulkanSampler getSampler() {
        return sampler;
    }

    private static VulkanImageBundle createTexutureImageBundle(VulkanDevice device, VulkanMemoryAllocator memoryAllocator, long commandPool, String filePath, boolean multiSampling) {
        VulkanImage textureImage = createTextureImage(device, memoryAllocator, commandPool, filePath, multiSampling);
        VulkanImageView textureImageView = createTextureImageView(device, textureImage);

        return new VulkanImageBundle(textureImage, textureImageView);
    }

    private static VulkanImage createTextureImage(VulkanDevice device, VulkanMemoryAllocator memoryAllocator, long commandPool, String filePath, boolean multiSampling) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            BufferedImage bufferedImage = FileUtils.getBufferedImage(filePath);
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

            CommandBufferUtils.transitionImageLayout(device.getHandle(), device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_UNDEFINED, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);
            CommandBufferUtils.copyBufferToImage(device.getHandle(), device.getGraphicsQueue(), commandPool, stagingBuffer, textureImage, imageWidth, imageHeight);
            if(multiSampling) {
                CommandBufferUtils.generateMipmaps(device.getHandle(), device.getGraphicsQueue(), commandPool, textureImage, imageWidth, imageHeight, mipLevels);
            } else {
                CommandBufferUtils.transitionImageLayout(device.getHandle(), device.getGraphicsQueue(), commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, mipLevels);
            }
            stagingBuffer.destroy(memoryAllocator);

            return textureImage;
		} catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Could not load texture");
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
}
