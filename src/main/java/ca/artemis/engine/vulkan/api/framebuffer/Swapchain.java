package ca.artemis.engine.vulkan.api.framebuffer;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.context.VulkanSurface;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.engine.vulkan.api.memory.VulkanImage;
import ca.artemis.engine.vulkan.api.memory.VulkanImageView;

public class Swapchain extends FramebufferObject {
    
    private final long handle;

    public Swapchain(long handle, VulkanImage[] images, VulkanImageView[] imageViews, VulkanFramebuffer[] framebuffers) {
        super(images, imageViews, framebuffers);
        this.handle = handle;
    }

    @Override
    public void destroy(VulkanDevice device, VulkanMemoryAllocator vulkanMemoryAllocator) {
        super.destroy(device, null);
        KHRSwapchain.vkDestroySwapchainKHR(device.getHandle(), handle, null);
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public VulkanImage[] getImages() {
        throw new UnsupportedOperationException("Cannot get Images on swapchain.");
    }

    @Override 
    public VulkanImage getImage(int index) {
        throw new UnsupportedOperationException("Cannot get Image at index on swapchain.");
    }

    public static class Builder {

        private VulkanSurface surface; 
        private RenderPass renderPass; 
        private SurfaceSupportDetails surfaceSupportDetails;

        public Swapchain build(VulkanDevice device) {
            long handle = createHandle(device, surface, surfaceSupportDetails);
            VulkanImage[] images = new VulkanImage[] { };
            VulkanImageView[] imageViews = createImageViews(device, handle, surfaceSupportDetails);
            VulkanFramebuffer[] framebuffers = createFramebuffers(device, imageViews, renderPass, surfaceSupportDetails);

            return new Swapchain(handle, images, imageViews, framebuffers);
        }

        public Builder setVulkanSurface(VulkanSurface surface) {
            this.surface = surface;
            return this;
        }

        public Builder setRenderPass(RenderPass renderPass) {
            this.renderPass = renderPass;
            return this;
        } 

        public Builder setSurfaceSupportDetails(SurfaceSupportDetails surfaceSupportDetails) {
            this.surfaceSupportDetails = surfaceSupportDetails;
            return this;
        }

        private long createHandle(VulkanDevice device, VulkanSurface surface, SurfaceSupportDetails surfaceSupportDetails) {
            VkSurfaceCapabilitiesKHR surfaceCapabilities = surfaceSupportDetails.getSurfaceCapabilities();
            int minImageCount = surfaceCapabilities.minImageCount() + 1;
            if (surfaceCapabilities.maxImageCount() > 0 && minImageCount > surfaceCapabilities.maxImageCount()) {
                minImageCount = surfaceCapabilities.maxImageCount();
            }
    
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VkSwapchainCreateInfoKHR pCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                    .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface.getHandle())
                    .minImageCount(minImageCount)
                    .imageFormat(surfaceSupportDetails.getSurfaceFormat().format())
                    .imageColorSpace(surfaceSupportDetails.getSurfaceFormat().colorSpace())
                    .imageExtent(surfaceSupportDetails.getSurfaceExtent())
                    .imageArrayLayers(1)
                    .imageUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .imageSharingMode(VK11.VK_SHARING_MODE_EXCLUSIVE)
                    .preTransform(surfaceCapabilities.currentTransform())
                    .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(surfaceSupportDetails.getSurfacePresentMode())
                    .clipped(true)
                    .oldSwapchain(VK11.VK_NULL_HANDLE);
    
                LongBuffer pSwapchain = stack.callocLong(1);
                KHRSwapchain.vkCreateSwapchainKHR(device.getHandle(), pCreateInfo, null, pSwapchain);
                return pSwapchain.get(0);
            }
        }

        private VulkanImageView[] createImageViews(VulkanDevice device, long swapchain, SurfaceSupportDetails surfaceSupportDetails) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pSwapchainImageCount = stack.callocInt(1);
                KHRSwapchain.vkGetSwapchainImagesKHR(device.getHandle(), swapchain, pSwapchainImageCount, null);
        
                LongBuffer pSwapchainImages = stack.callocLong(pSwapchainImageCount.get(0));
                KHRSwapchain.vkGetSwapchainImagesKHR(device.getHandle(), swapchain, pSwapchainImageCount, pSwapchainImages);
                
                VulkanImageView[] imageViews = new VulkanImageView[pSwapchainImageCount.get(0)];
                for(int i = 0; i < imageViews.length; i++) {
                    imageViews[i] = new VulkanImageView.Builder()
                        .setImage(pSwapchainImages.get(i))
                        .setFormat(surfaceSupportDetails.getSurfaceFormat().format())
                        .setComponentR(VK11.VK_COMPONENT_SWIZZLE_R)
                        .setComponentG(VK11.VK_COMPONENT_SWIZZLE_G)
                        .setComponentB(VK11.VK_COMPONENT_SWIZZLE_B)
                        .setComponentA(VK11.VK_COMPONENT_SWIZZLE_A)
                        .build(device);
                }
                return imageViews;
            }
        }

        private VulkanFramebuffer[] createFramebuffers(VulkanDevice device, VulkanImageView[] imageViews, RenderPass renderPass, SurfaceSupportDetails surfaceSupportDetails) {
            VulkanFramebuffer[] framebuffers = new VulkanFramebuffer[imageViews.length];
            for(int i = 0; i < framebuffers.length; i++) {
                framebuffers[i] = new VulkanFramebuffer.Builder()
                    .addAttachement(imageViews[i])
                    .setRenderPass(renderPass.getHandle())
                    .setWidth(surfaceSupportDetails.getSurfaceExtent().width())
                    .setHeight(surfaceSupportDetails.getSurfaceExtent().height())
                    .setLayers(1)
                    .build(device);
            }
            return framebuffers;
        }
    }
}
