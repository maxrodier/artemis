package ca.artemis.vulkan.rendering;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK11;

import ca.artemis.vulkan.commands.CommandBufferUtils;
import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class RenderingEngine {
    
    private final VulkanContext context;
    private final CommandPool commandPool;
    private final VulkanImage textureImage;
    private final VulkanImageView textureImageView;
    private final Swapchain swapchain;
    private final SwapchainRenderer swapchainRenderer;

    public RenderingEngine(VulkanContext context) {
        this.context = context;
        this.commandPool = new CommandPool(this.context.getDevice(), this.context.getPhysicalDevice().getQueueFamilies().get(0).getIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        this.textureImage = createTextureImage(context.getMemoryAllocator(), context.getDevice(), commandPool, "src/main/resources/textures/wood.png");
        this.textureImageView = createTextureImageView(context.getDevice(), this.textureImage);
        this.swapchain = new Swapchain(this.context, this.commandPool);
        this.swapchainRenderer = new SwapchainRenderer(this.context, this.commandPool, this.swapchain, this.textureImageView);
    }

    private static VulkanImage createTextureImage(VulkanMemoryAllocator allocator, VulkanDevice device, CommandPool commandPool, String filePath) {
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
    
    private static VulkanImageView createTextureImageView(VulkanDevice device, VulkanImage textureImage) {
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

    public void mainLoop() {
        int frame = 0;
        long lastTime = System.nanoTime();
        long currentTime;
      
        LongBuffer pWaitSemaphores = MemoryUtil.memCallocLong(1);
        pWaitSemaphores.put(imageAcquiredSemaphore.getHandle());
        pWaitSemaphores.flip();
        
        LongBuffer pSignalSemaphores = MemoryUtil.memCallocLong(1);
        pSignalSemaphores.put(drawCompleteSemaphore.getHandle());
        pSignalSemaphores.flip();
        
        IntBuffer pWaitStages = MemoryUtil.memCallocInt(1);
        pWaitStages.put(VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        pWaitStages.flip();

        VulkanFence fence = new VulkanFence(context.getDevice());
        SubmitInfo submitInfo = new SubmitInfo(fence)
            .setWaitSemaphores(pWaitSemaphores, 1)
            .setWaitDstStageMask(pWaitStages)
            .setSignalSemaphores(pSignalSemaphores);

        while (!context.getWindow().isCloseRequested()) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                frame++;
                currentTime = System.nanoTime();
                if(currentTime - lastTime >= 1000000000L) {
                    lastTime += 1000000000L;
                    System.out.println(frame);
                    frame = 0;
                }
                
                GLFW.glfwPollEvents();

                fence.waitFor(context.getDevice());
                
                IntBuffer pImageIndex = stack.callocInt(1);
                KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), swapchain.getHandle(), Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);

                submitInfo.setCommandBuffers(stack.callocPointer(1).put(swapchainRenderer.getDrawCommandBuffer(pImageIndex.get(0))).flip());
                submitInfo.submit(context.getDevice(), context.getDevice().getGraphicsQueue());

                LongBuffer pSwapchains = stack.callocLong(1);
                pSwapchains.put(swapchain.getHandle());
                pSwapchains.flip();

                VkPresentInfoKHR pPresentInfo = VkPresentInfoKHR.callocStack(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                    .pWaitSemaphores(pSignalSemaphores)
                    .swapchainCount(1)
                    .pSwapchains(pSwapchains)
                    .pImageIndices(pImageIndex);

                KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), pPresentInfo);
            }
        }

        VK11.vkDeviceWaitIdle(context.getDevice().getHandle());
        submitInfo.destroy();
        fence.destroy(context.getDevice());
        MemoryUtil.memFree(pWaitStages);
        MemoryUtil.memFree(pSignalSemaphores);
        MemoryUtil.memFree(pWaitSemaphores);
    }

    public void destroy() {
        swapchainRenderer.destroy(context);
        textureImageView.destroy(context.getDevice());
        textureImage.destroy(context.getMemoryAllocator());
        swapchain.destroy(context);
        commandPool.destroy(context.getDevice());
    }
}