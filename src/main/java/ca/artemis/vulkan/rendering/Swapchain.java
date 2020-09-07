package ca.artemis.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import ca.artemis.Configuration;
import ca.artemis.vulkan.commands.CommandBufferUtils;
import ca.artemis.vulkan.commands.CommandPool;
import ca.artemis.vulkan.commands.SubmitInfo;
import ca.artemis.vulkan.context.VulkanContext;
import ca.artemis.vulkan.context.VulkanDevice;
import ca.artemis.vulkan.context.VulkanMemoryAllocator;
import ca.artemis.vulkan.context.VulkanPhysicalDevice;
import ca.artemis.vulkan.context.VulkanSurface;
import ca.artemis.vulkan.descriptor.DescriptorPool;
import ca.artemis.vulkan.descriptor.DescriptorSet;
import ca.artemis.vulkan.descriptor.DescriptorSetLayout;
import ca.artemis.vulkan.memory.Framebuffer;
import ca.artemis.vulkan.memory.VulkanImage;
import ca.artemis.vulkan.memory.VulkanImageView;
import ca.artemis.vulkan.pipeline.ColorBlendState;
import ca.artemis.vulkan.pipeline.GraphicsPipeline;
import ca.artemis.vulkan.pipeline.ShaderModule;
import ca.artemis.vulkan.pipeline.VertexInputState;
import ca.artemis.vulkan.pipeline.ViewportState;
import ca.artemis.vulkan.synchronization.VulkanFence;
import ca.artemis.vulkan.synchronization.VulkanSemaphore;

public class Swapchain {
    
    public static final int SURFACEFORMAT = VK11.VK_FORMAT_B8G8R8A8_UNORM;

    private final long handle;
    private final VulkanImageView[] imageViews;
    private final RenderPass renderPass;
    private final Framebuffer[] framebuffers;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout descriptorSetLayout;
    private final DescriptorSet[] descriptorSets;
    private final GraphicsPipeline graphicsPipeline;


    private final VulkanSemaphore imageAcquiredSemaphore;
    private final VulkanSemaphore renderCompletedSemaphore;
    private final VulkanFence renderFence;
    private final SubmitInfo submitInfo;
    private final VkPresentInfoKHR presentInfo;
    private final IntBuffer pImageIndex;

    public Swapchain(VulkanContext context, CommandPool commandPool) {
        this.handle = createHandle(context.getDevice(), context.getPhysicalDevice(), context.getSurface(), context.getSurfaceCapabilities());
        this.imageViews = createImageViews(context.getDevice(), this.handle);
        this.renderPass = createRenderPass(context.getDevice());
        this.framebuffers = createFramebuffers(context.getDevice(), this.imageViews, this.renderPass, context.getSurfaceCapabilities());
        this.descriptorPool = createDescriptorPool(context.getDevice(), this.framebuffers.length);
        this.descriptorSetLayout = createDescriptorSetLayout(context.getDevice());
        this.descriptorSets = createDescriptorSets(context.getDevice(), this.descriptorPool, this.descriptorSetLayout, this.framebuffers.length);
        this.graphicsPipeline = createGraphicsPipeline(context.getDevice(), this.descriptorSetLayout, this.renderPass);


        
        this.imageAcquiredSemaphore = new VulkanSemaphore(context.getDevice());
        this.renderCompletedSemaphore = new VulkanSemaphore(context.getDevice());
        this.renderFence = new VulkanFence(context.getDevice());

        this.pImageIndex = MemoryUtil.memCallocInt(1);
        this.submitInfo = new SubmitInfo(this.renderFence);
        this.presentInfo = VkPresentInfoKHR.calloc();
    }

    public void destroy(VulkanContext context) {
        MemoryUtil.memFree(pImageIndex);
        graphicsPipeline.destroy(context.getDevice());
        descriptorSetLayout.destroy(context.getDevice());
        descriptorPool.destroy(context.getDevice());
        for(Framebuffer framebuffer : framebuffers)
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

    private Framebuffer[] createFramebuffers(VulkanDevice device, VulkanImageView[] imageViews, RenderPass renderPass, VkSurfaceCapabilitiesKHR surfaceCapabilities) {
        Framebuffer[] framebuffers = new Framebuffer[imageViews.length];
        for(int i = 0; i < framebuffers.length; i++) {
            framebuffers[i] = new Framebuffer.Builder()
                .addAttachement(imageViews[i])
                .setRenderPass(renderPass.getHandle())
                .setWidth(surfaceCapabilities.currentExtent().width())
                .setHeight(surfaceCapabilities.currentExtent().height())
                .setLayers(1)
                .build(device);
        }
        return framebuffers;
    }

    private DescriptorPool createDescriptorPool(VulkanDevice device, int size) {
        return new DescriptorPool.Builder()
            .addPoolSize(VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, size)
            .setMaxSets(size)
            .build(device);
    }

    private DescriptorSetLayout createDescriptorSetLayout(VulkanDevice device) {
        return new DescriptorSetLayout.Builder()
            .addDescriptorSetLayoutBinding(0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1, VK11.VK_SHADER_STAGE_FRAGMENT_BIT)
            .build(device);
    }

    private DescriptorSet[] createDescriptorSets(VulkanDevice device, DescriptorPool descriptorPool, DescriptorSetLayout descriptorSetLayout, int size) {
        DescriptorSet[] descriptorSets = new DescriptorSet[size];
        for(int i = 0; i < size; i++) {
            descriptorSets[i] = new DescriptorSet(device, descriptorPool, descriptorSetLayout);
        }
        return descriptorSets;
    }

    private static GraphicsPipeline createGraphicsPipeline(VulkanDevice device, DescriptorSetLayout descriptorSetLayout, RenderPass renderPass) {
        return new GraphicsPipeline.Builder()
            .addShaderModule(new ShaderModule(device, "src/main/resources/shaders/swapchain.vert.spv", VK11.VK_SHADER_STAGE_VERTEX_BIT))
            .addShaderModule(new ShaderModule(device, "src/main/resources/shaders/swapchain.frag.spv", VK11.VK_SHADER_STAGE_FRAGMENT_BIT))
            .setVertexInputState(new VertexInputState()
                .addBinding(new VertexInputState.VertexInputBindingDescription(0, 20, VK11.VK_VERTEX_INPUT_RATE_VERTEX)
                    .addAttributes(0, VK11.VK_FORMAT_R32G32B32_SFLOAT, 0)
                    .addAttributes(1, VK11.VK_FORMAT_R32G32_SFLOAT, 12)))
            .setViewportState(new ViewportState()
                .addViewport(new ViewportState.Viewport(0, 0, Configuration.windowWidth, Configuration.windowHeight, 0.0f, 1.0f))
                .addScissors(new ViewportState.Scissor(0, 0, Configuration.windowWidth, Configuration.windowHeight)))
            .setColorBlendState(new ColorBlendState()
                .addColorBlendAttachement(new ColorBlendState.ColorBlendAttachement(false, VK11.VK_COLOR_COMPONENT_R_BIT | VK11.VK_COLOR_COMPONENT_G_BIT | VK11.VK_COLOR_COMPONENT_B_BIT | VK11.VK_COLOR_COMPONENT_A_BIT)))
            .setDescriptorSetLayouts(new DescriptorSetLayout[] {descriptorSetLayout})
            .setRenderPass(renderPass)
            .build(device);
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

    public void draw(VulkanDevice device) {
        int error = KHRSwapchain.vkAcquireNextImageKHR(device.getHandle(), handle, Long.MAX_VALUE, imageAcquiredSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if (error != VK11.VK_SUCCESS) {
            throw new AssertionError("Failed to acquire next swapchain image");
        }

        //submitInfo.setCommandBuffers(MemoryUtil.memAllocPointer(1).put(swapchainRenderer.getDrawCommandBuffer(pImageIndex.get(0))).flip());
        submitInfo.submit(device, device.getGraphicsQueue());

        error = KHRSwapchain.vkQueuePresentKHR(device.getGraphicsQueue(), presentInfo);
        if (error != VK11.VK_SUCCESS) {
            throw new AssertionError("Failed to submit present info");
        }
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

    public Framebuffer[] getFramebuffers() {
        return framebuffers;
    }

    public Framebuffer getFramebuffer(int index) {
        return framebuffers[index];
    }

    public DescriptorPool getDescriptorPool() {
        return descriptorPool;
    }

    public DescriptorSetLayout getDescriptorSetLayout() {
        return descriptorSetLayout;
    }

    public DescriptorSet[] getDescriptorSets() {
        return descriptorSets;
    }

    public DescriptorSet getDescriptorSet(int index) {
        return descriptorSets[index];
    }

    public GraphicsPipeline getGraphicsPipeline() {
        return graphicsPipeline;
    }
}