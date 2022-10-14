package ca.artemis;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import ca.artemis.Util.VkImage;

public class Texture {
    
    public final VkImage image;
    public final long imageView;
    public final long sampler;

    public Texture(VkImage image, long imageView, long sampler) {
        this.image = image;
        this.imageView = imageView;
        this.sampler = sampler;
    }

    public static Texture createTexture(VkDevice device, long allocator, VkQueue graphicsQueue, long commandPool, String filePath) {
        BufferedImage bufferedImage = Util.getBufferedImage(filePath);
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();
        int imageSize = bufferedImage.getWidth() * bufferedImage.getHeight();
        int mipLevels = (int)Math.floor(Math.log(Math.max(imageWidth, imageHeight))) + 1;
        
        VkImage image = createImage(device, allocator, graphicsQueue, commandPool, bufferedImage, imageWidth, imageHeight, imageSize, mipLevels);
        long imageView = createImageView(device, image, mipLevels);
        long sampler = createSampler(device, mipLevels);

        return new Texture(image, imageView, sampler);
    }

    private static VkImage createImage(VkDevice device, long allocator, VkQueue graphicsQueue, long commandPool, BufferedImage bufferedImage, int imageWidth, int imageHeight, int imageSize, int mipLevels) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
                .setLength(imageSize)
                .setSize(Integer.BYTES)
                .setBufferUsage(VK11.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
                .setMemoryUsage(Vma.VMA_MEMORY_USAGE_CPU_TO_GPU)
                .build(allocator);
            
            PointerBuffer ppData = stack.callocPointer(1);
            Vma.vmaMapMemory(allocator, stagingBuffer.getAllocationHandle(), ppData);
            IntBuffer data = ppData.getIntBuffer(imageSize);
            for(int y = 0; y < imageHeight; y++) {
                for(int x = 0; x < imageWidth; x++) {
                    data.put(x + y * imageWidth, bufferedImage.getRGB(x, y));
                }
            }
            Vma.vmaUnmapMemory(allocator, stagingBuffer.getAllocationHandle());
            
            VkImage textureImage = Util.createImage(device, allocator, imageWidth, imageHeight, mipLevels, VK11.VK_SAMPLE_COUNT_1_BIT, VK11.VK_FORMAT_R8G8B8A8_SRGB, VK11.VK_IMAGE_TILING_OPTIMAL, VK11.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK11.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK11.VK_IMAGE_USAGE_SAMPLED_BIT, Vma.VMA_MEMORY_USAGE_GPU_ONLY);

            CommandBufferUtils.transitionImageLayout(device, graphicsQueue, commandPool, textureImage, VK11.VK_FORMAT_B8G8R8A8_UNORM, VK11.VK_IMAGE_LAYOUT_UNDEFINED, VK11.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);
            CommandBufferUtils.copyBufferToImage(device, graphicsQueue, commandPool, stagingBuffer, textureImage, imageWidth, imageHeight);
            CommandBufferUtils.generateMipmaps(device, graphicsQueue, commandPool, textureImage, imageWidth, imageHeight, mipLevels);

            stagingBuffer.destroy(allocator);

            return textureImage;
		}
    }

    private static long createImageView(VkDevice device, VkImage textuImage, int mipLevels) {
        return Util.createImageView(device, textuImage.image, VK11.VK_FORMAT_R8G8B8A8_SRGB, VK11.VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
    }

    private static long createSampler(VkDevice device, long mipLevels) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerInfo.sType(VK11.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(VK11.VK_FILTER_LINEAR);
            samplerInfo.minFilter(VK11.VK_FILTER_LINEAR);
            samplerInfo.addressModeU(VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK11.VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(1); //TODO: Use properties from physical device
            samplerInfo.borderColor(VK11.VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK11.VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(VK11.VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerInfo.minLod(0.0f);
            samplerInfo.maxLod(mipLevels);
            samplerInfo.mipLodBias(0.0f);

            LongBuffer pTextureSampler = stack.callocLong(1);
            if (VK11.vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK11.VK_SUCCESS) {
                throw new RuntimeException("failed to create texture sampler!");
            }
            return pTextureSampler.get(0);
        }
    }
}
