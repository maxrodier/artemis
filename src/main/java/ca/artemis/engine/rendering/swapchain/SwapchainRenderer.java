package ca.artemis.engine.rendering.swapchain;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.RenderData;
import ca.artemis.engine.rendering.Renderer;
import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.engine.vulkan.api.context.VulkanSurface;
import ca.artemis.engine.vulkan.api.framebuffer.FramebufferObject;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.framebuffer.SurfaceSupportDetails;
import ca.artemis.engine.vulkan.api.framebuffer.Swapchain;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.engine.vulkan.core.ShaderProgram;

public class SwapchainRenderer extends Renderer<SwapchainRenderData, Swapchain, SwapchainShaderProgram> {

    //TempStuff
    private List<CommandPool> commandPools;
    private List<PrimaryCommandBuffer> commandBuffers;

    private Renderer<? extends RenderData, ? extends FramebufferObject, ? extends ShaderProgram> renderSource;
    private List<VulkanSemaphore> imageAvailableSemaphores;

    public SwapchainRenderer(VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, SurfaceSupportDetails surfaceSupportDetails, Renderer<? extends RenderData, ? extends FramebufferObject, ? extends ShaderProgram> renderSource) {
        super(renderSource.getSignalSemaphores());

        this.renderSource = renderSource;

        createCommandPools(device, physicalDevice);
        createCommandBuffers(device);
    }

    @Override
    public void close() throws Exception {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        for(VulkanSemaphore imageAvailableSemaphore : imageAvailableSemaphores) {
            imageAvailableSemaphore.destroy(device);
        }

        super.close();
    }

    @Override
    protected RenderPass createRenderPass() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();
        SurfaceSupportDetails surfaceSupportDetails = LowPolyEngine.instance().getContext().getSurfaceSupportDetails();

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

    @Override
    protected Swapchain createFramebufferObject() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();
        VulkanSurface surface = LowPolyEngine.instance().getContext().getSurface();
        SurfaceSupportDetails surfaceSupportDetails = LowPolyEngine.instance().getContext().getSurfaceSupportDetails();

        return new Swapchain.Builder()
            .setVulkanSurface(surface)
            .setRenderPass(renderPass)
            .setSurfaceSupportDetails(surfaceSupportDetails)
            .build(device);
    }

    @Override
    protected SwapchainShaderProgram createShaderProgram() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        return new SwapchainShaderProgram(device, renderPass);
    }

    @Override
    protected void createSynchronizationObjects() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        this.imageAvailableSemaphores = new ArrayList<>();
        this.signalSemaphores = new ArrayList<>();
        this.inFlightFences = new ArrayList<>();

        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            imageAvailableSemaphores.add(new VulkanSemaphore(device));
            signalSemaphores.add(new VulkanSemaphore(device));
            inFlightFences.add(new VulkanFence(device));
        }
    }

    //We acquire the next available swapchain image
    //QUESTION: Why wait for fence
    //We signal that the image is available with the imageAvailableSemaphore
    //If the KHR is out of date, we need to recreate the swapchain
    public int acquireNextSwapchainImage(MemoryStack stack, VulkanContext context) {
        VulkanFence inFlightFence = inFlightFences.get(renderData.getFrameIndex());
        inFlightFence.waitFor(stack, context.getDevice());

        IntBuffer pImageIndex = stack.callocInt(1);
        int result = KHRSwapchain.vkAcquireNextImageKHR(context.getDevice().getHandle(), framebufferObject.getHandle(), Long.MAX_VALUE, imageAvailableSemaphores.get(renderData.getFrameIndex()).getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("Failed to acquire swapchain image!");
        } 
        inFlightFence.reset(stack, context.getDevice());
        renderData.setImageIndex(pImageIndex.get(0));
        
        if(result == KHRSwapchain.VK_SUBOPTIMAL_KHR) { //TODO: We wait for the semaphore to be signaled else since we don't wait for it in the render because we regenerated renderers because of a suboptiomal swapchain
            LongBuffer pWaitSemaphores = stack.callocLong(1);
            pWaitSemaphores.put(0, imageAvailableSemaphores.get(renderData.getFrameIndex()).getHandle());

            IntBuffer pWaitDstStageMask = stack.callocInt(1);
            pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pWaitSemaphores(pWaitSemaphores);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitDstStageMask(pWaitDstStageMask);

            VK11.vkQueueSubmit(context.getDevice().getGraphicsQueue(), submitInfo, inFlightFence.getHandle());
        }

        return result;
    }

    //We need to wait for the previous render to be finished and for the image to be available
    //QUESTION: Validate the waitdstStageMask
    //We need to render using the renderData
    @Override
    public void render(MemoryStack stack, VulkanContext context) {
        int waitSemaphoreCount = waitSemaphores == null ? 1 : 2;

        LongBuffer pWaitSemaphores = stack.callocLong(waitSemaphoreCount);
        pWaitSemaphores.put(0, imageAvailableSemaphores.get(renderData.getFrameIndex()).getHandle());
        if(waitSemaphoreCount == 2) {
            pWaitSemaphores.put(1, waitSemaphores.get(renderData.getFrameIndex()).getHandle());
        }

        IntBuffer pWaitDstStageMask = stack.callocInt(1);
        pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, signalSemaphores.get(renderData.getFrameIndex()).getHandle());

        VulkanFramebuffer framebuffer = framebufferObject.getFramebuffer(renderData.getImageIndex());
        recordCommandBuffer(stack, framebuffer,  framebuffer.getWidth(), framebuffer.getHeight(), renderData.getFrameIndex());
        submitPrimaryCommandBuffer(stack, context, waitSemaphoreCount, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFences.get(renderData.getFrameIndex()), renderData.getFrameIndex());
    }


    //When the signalSempahre is signal we know the render is finished and we can present the render target
    //We recreate the swapchain if needed.
    public int present(MemoryStack stack, VulkanContext context) {
        IntBuffer pImageIndex = stack.callocInt(1);
        pImageIndex.put(0, renderData.getImageIndex());

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, signalSemaphores.get(renderData.getFrameIndex()).getHandle());

        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pSignalSemaphores);

        LongBuffer pSwapChains = stack.callocLong(1);
        pSwapChains.put(0, framebufferObject.getHandle());

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(pSwapChains);
        presentInfo.pImageIndices(pImageIndex);

        int result = KHRSwapchain.vkQueuePresentKHR(context.getDevice().getGraphicsQueue(), presentInfo);
        if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("failed to present swap chain image!");
        }
        return result;
    }

    //TempStuff
    public void createCommandPools(VulkanDevice device, VulkanPhysicalDevice physicalDevice) {
        this.commandPools = new ArrayList<>();
        int queueFamilyIndex = physicalDevice.getQueueFamilies().get(0).getIndex(); //TODO: Find a better way to get this ....

        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            commandPools.add(new CommandPool(device, queueFamilyIndex, VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT));
        }
    }

    private void createCommandBuffers(VulkanDevice device) {
        this.commandBuffers = new ArrayList<>();
        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            commandBuffers.add(new PrimaryCommandBuffer(device, commandPools.get(i)));
        }
    }

    public void recordCommandBuffer(MemoryStack stack, VulkanFramebuffer framebuffer, int width, int height, int frameIndex) {
        VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1, stack);
        pClearValues.get(0).color().float32(0, 0.0f);
        pClearValues.get(0).color().float32(1, 0.0f);
        pClearValues.get(0).color().float32(2, 0.0f);
        pClearValues.get(0).color().float32(3, 1.0f);
        
        PrimaryCommandBuffer commandBuffer = commandBuffers.get(frameIndex);

        VK11.vkResetCommandBuffer(commandBuffer.getCommandBuffer(), 0);
        commandBuffer.beginRecording(stack, 0);
        commandBuffer.beginRenderPassCmd(stack, renderPass.getHandle(), framebuffer.getHandle(), width, height, pClearValues, VK11.VK_SUBPASS_CONTENTS_INLINE);

        commandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, shaderProgram.getGraphicsPipeline());
        commandBuffer.bindVertexBufferCmd(stack, renderData.getQuad().getVertexBuffer());
        commandBuffer.bindIndexBufferCmd(renderData.getQuad().getIndexBuffer());
        commandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, shaderProgram.getGraphicsPipeline().getPipelineLayout(), renderData.getDescriptorSet(frameIndex));
        commandBuffer.drawIndexedCmd(renderData.getQuad().getIndexBuffer().getLenght(), 1);

        commandBuffer.endRenderPassCmd();
        commandBuffer.endRecording();
    }

    public void submitPrimaryCommandBuffer(MemoryStack stack, VulkanContext context, int waitSemaphoreCount, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int frameIndex) {
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

        submitInfo.waitSemaphoreCount(waitSemaphoreCount);
        submitInfo.pWaitSemaphores(pWaitSemaphores);
        submitInfo.pWaitDstStageMask(pWaitDstStageMask);

        PointerBuffer pCommandBuffers = stack.callocPointer(1);
        pCommandBuffers.put(0, commandBuffers.get(frameIndex).getCommandBuffer());

        submitInfo.pCommandBuffers(pCommandBuffers);

        submitInfo.pSignalSemaphores(pSignalSemaphores);

        if(VK11.vkQueueSubmit(context.getDevice().getGraphicsQueue(), submitInfo, inFlightFence.getHandle()) != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to submit draw command buffer!");
        }
    }

    public FramebufferObject getRenderSource() {
        return renderSource.getFramebufferObject();
    }
}
