package ca.artemis.vulkan.api.framebuffer;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.vulkan.api.context.VulkanSurface;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.memory.VulkanImageView;

public class Swapchain {
    
    public static final int SURFACEFORMAT = VK11.VK_FORMAT_B8G8R8A8_UNORM;

    private final long handle;
    private final VulkanImageView[] imageViews;
    private final RenderPass renderPass;
    private final VulkanFramebuffer[] framebuffers;

    public Swapchain(VulkanContext context) {
        this.handle = createHandle(context.getDevice(), context.getPhysicalDevice(), context.getSurface(), context.getSurfaceCapabilities());
        this.imageViews = createImageViews(context.getDevice(), this.handle);
        this.renderPass = createRenderPass(context.getDevice());
        this.framebuffers = createFramebuffers(context.getDevice(), this.imageViews, this.renderPass, context.getSurfaceCapabilities());
    }

    public void destroy(VulkanContext context) {
        for(VulkanFramebuffer framebuffer : framebuffers)
            framebuffer.destroy(context.getDevice());
        renderPass.destroy(context.getDevice());
        for(VulkanImageView imageView : imageViews)
            imageView.destroy(context.getDevice());
        KHRSwapchain.vkDestroySwapchainKHR(context.getDevice().getHandle(), handle, null);
    }

    private long createHandle(VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, VkSurfaceCapabilitiesKHR surfaceCapabilities) {
        int minImageCount = surfaceCapabilities.minImageCount();
        if (surfaceCapabilities.maxImageCount() > 0 && minImageCount > surfaceCapabilities.maxImageCount()) {
            minImageCount = surfaceCapabilities.maxImageCount();
        }

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSwapchainCreateInfoKHR pCreateInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .surface(surface.getHandle())
                .minImageCount(minImageCount)
                .imageFormat(SURFACEFORMAT)
                .imageColorSpace(KHRSurface.VK_COLORSPACE_SRGB_NONLINEAR_KHR)
                .imageExtent(surfaceCapabilities.currentExtent())
                .imageArrayLayers(1)
                .imageUsage(VK11.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .imageSharingMode(VK11.VK_SHARING_MODE_EXCLUSIVE)
                .preTransform(surfaceCapabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR)
                .clipped(true)
                .oldSwapchain(VK11.VK_NULL_HANDLE);

            LongBuffer pSwapchain = stack.callocLong(1);
            KHRSwapchain.vkCreateSwapchainKHR(device.getHandle(), pCreateInfo, null, pSwapchain);
            return pSwapchain.get(0);
        }
    }

    private VulkanImageView[] createImageViews(VulkanDevice device, long swapchain) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pSwapchainImageCount = stack.callocInt(1);
            KHRSwapchain.vkGetSwapchainImagesKHR(device.getHandle(), swapchain, pSwapchainImageCount, null);
    
            LongBuffer pSwapchainImages = stack.callocLong(pSwapchainImageCount.get(0));
            KHRSwapchain.vkGetSwapchainImagesKHR(device.getHandle(), swapchain, pSwapchainImageCount, pSwapchainImages);
            
            VulkanImageView[] imageViews = new VulkanImageView[pSwapchainImageCount.get(0)];
            for(int i = 0; i < imageViews.length; i++) {
                imageViews[i] = new VulkanImageView.Builder()
                    .setImage(pSwapchainImages.get(i))
                    .setFormat(SURFACEFORMAT)
                    .setComponentR(VK11.VK_COMPONENT_SWIZZLE_R)
                    .setComponentG(VK11.VK_COMPONENT_SWIZZLE_G)
                    .setComponentB(VK11.VK_COMPONENT_SWIZZLE_B)
                    .setComponentA(VK11.VK_COMPONENT_SWIZZLE_A)
                    .build(device);
            }
            return imageViews;
        }
    }

    private RenderPass createRenderPass(VulkanDevice device) {
        return new RenderPass.Builder()
            .addColorAttachment(new RenderPass.Attachement()
                .setFormat(VK11.VK_FORMAT_B8G8R8A8_UNORM)
                .setSamples(VK11.VK_SAMPLE_COUNT_1_BIT)
                .setLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .setStoreOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                .setStencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .setStencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .setInitialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                .setFinalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
            .build(device);
    }

    private VulkanFramebuffer[] createFramebuffers(VulkanDevice device, VulkanImageView[] imageViews, RenderPass renderPass, VkSurfaceCapabilitiesKHR surfaceCapabilities) {
        VulkanFramebuffer[] framebuffers = new VulkanFramebuffer[imageViews.length];
        for(int i = 0; i < framebuffers.length; i++) {
            framebuffers[i] = new VulkanFramebuffer.Builder()
                .addAttachement(imageViews[i])
                .setRenderPass(renderPass.getHandle())
                .setWidth(surfaceCapabilities.currentExtent().width())
                .setHeight(surfaceCapabilities.currentExtent().height())
                .setLayers(1)
                .build(device);
        }
        return framebuffers;
    }

    public long getHandle() {
        return handle;
    }

    public VulkanImageView[] getImageViews() {
        return imageViews;
    }

    public VulkanImageView getImageView(int index) {
        return imageViews[index];
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public VulkanFramebuffer[] getFramebuffers() {
        return framebuffers;
    }

    public VulkanFramebuffer getFramebuffer(int index) {
        return framebuffers[index];
    }
}