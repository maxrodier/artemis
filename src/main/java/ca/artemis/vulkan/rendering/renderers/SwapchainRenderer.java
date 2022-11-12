package ca.artemis.vulkan.rendering.renderers;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;

import ca.artemis.Configuration;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.framebuffer.SurfaceSupportDetails;
import ca.artemis.vulkan.api.framebuffer.Swapchain;
import ca.artemis.vulkan.rendering.programs.SwapchainShaderProgram;

public class SwapchainRenderer {
    
    private SurfaceSupportDetails surfaceSupportDetails;
    private RenderPass renderPass;
    private Swapchain swapchain;
    private final SwapchainShaderProgram swapchainShaderProgram;

    private final DescriptorSet[] descriptorSets;
    
    public SwapchainRenderer(VulkanContext context) {
        this.surfaceSupportDetails = new SurfaceSupportDetails(context.getPhysicalDevice(), context.getSurface(), context.getWindow()); //TODO: I don't like that we need to pass a window here //Could we move code to surface??
        this.renderPass = createRenderPass(context.getDevice(), surfaceSupportDetails); 
        this.swapchain = new Swapchain(context.getDevice(), context.getSurface(), renderPass, surfaceSupportDetails);
        this.swapchainShaderProgram = new SwapchainShaderProgram(context.getDevice(), renderPass);

        this.descriptorSets = createDescriptorSets(context.getDevice());
    }

    public void destroy(VulkanDevice device) {
        swapchainShaderProgram.destroy(device);
        swapchain.destroy(device);
        renderPass.destroy(device);
        surfaceSupportDetails.destroy();
    }

    public SurfaceSupportDetails getSurfaceSupportDetails() {
        return surfaceSupportDetails;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public Swapchain getSwapchain() {
        return swapchain;
    }

    public SwapchainShaderProgram getSwapchainShaderProgram() {
        return swapchainShaderProgram;
    }

    public void regenerateRenderer(VulkanContext context) { //TODO: Verify renderpass compatibilty
        swapchain.destroy(context.getDevice());
        renderPass.destroy(context.getDevice());
        surfaceSupportDetails.destroy();

        this.surfaceSupportDetails = new SurfaceSupportDetails(context.getPhysicalDevice(), context.getSurface(), context.getWindow());
        this.renderPass = createRenderPass(context.getDevice(), surfaceSupportDetails);
        this.swapchainShaderProgram.regenerateGraphicsPipeline(context.getDevice(), renderPass);
        this.swapchain = new Swapchain(context.getDevice(), context.getSurface(), renderPass, surfaceSupportDetails);
    }

    private RenderPass createRenderPass(VulkanDevice device, SurfaceSupportDetails surfaceSupportDetails) {
        return new RenderPass.Builder()
            .addColorAttachment(new RenderPass.Attachement()
                .setFormat(surfaceSupportDetails.getSurfaceFormat().format())
                .setSamples(VK11.VK_SAMPLE_COUNT_1_BIT)
                .setLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .setStoreOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                .setStencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .setStencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .setInitialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                .setFinalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR))
            .build(device);
    }

    private DescriptorSet[] createDescriptorSets(VulkanDevice device) {
        DescriptorSet[] descriptorSets = new DescriptorSet[Configuration.MAX_FRAMES_IN_FLIGHT];
        for(int i = 0; i < descriptorSets.length; i++) {
            descriptorSets[i] = swapchainShaderProgram.allocate(device);
        }
        return descriptorSets;
    }
}
