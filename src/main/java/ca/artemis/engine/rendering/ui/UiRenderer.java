package ca.artemis.engine.rendering.ui;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.RenderData;
import ca.artemis.engine.rendering.Renderer;
import ca.artemis.engine.rendering.RenderingEngine;
import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.engine.vulkan.api.framebuffer.FramebufferObject;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.framebuffer.SurfaceSupportDetails;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.engine.vulkan.core.ShaderProgram;

public class UiRenderer extends Renderer<UiRenderData, FramebufferObject, UiShaderProgram> {

    //TempStuff
    private List<CommandPool> commandPools;
    private List<PrimaryCommandBuffer> commandBuffers;

    private Renderer<? extends RenderData, ? extends FramebufferObject, ? extends ShaderProgram> renderSource;

    public UiRenderer(VulkanDevice device, VulkanPhysicalDevice physicalDevice, Renderer<? extends RenderData, ? extends FramebufferObject, ? extends ShaderProgram> renderSource) {
        super(renderSource.getSignalSemaphores());

        this.renderSource = renderSource;

        createCommandPools(device, physicalDevice);
        createCommandBuffers(device);
    }

    @Override
    protected RenderPass createRenderPass() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        return new RenderPass.Builder()
            .addColorAttachment(new RenderPass.Attachement()
                .setFormat(VK11.VK_FORMAT_R16G16B16A16_SFLOAT)
                .setSamples(VK11.VK_SAMPLE_COUNT_1_BIT)
                .setLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .setStoreOp(VK11.VK_ATTACHMENT_STORE_OP_STORE)
                .setStencilLoadOp(VK11.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .setStencilStoreOp(VK11.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .setInitialLayout(VK11.VK_IMAGE_LAYOUT_UNDEFINED)
                .setFinalLayout(VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL))
            .build(device);
    }

    @Override
    protected FramebufferObject createFramebufferObject() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();
        VulkanMemoryAllocator memoryAllocator = LowPolyEngine.instance().getContext().getMemoryAllocator();
        SurfaceSupportDetails surfaceSupportDetails = LowPolyEngine.instance().getContext().getSurfaceSupportDetails();

        return new FramebufferObject.Builder()
            .setRenderPass(renderPass)
            .setWidth(surfaceSupportDetails.getSurfaceExtent().width())
            .setHeight(surfaceSupportDetails.getSurfaceExtent().height())
            .setFormat(VK11.VK_FORMAT_R16G16B16A16_SFLOAT)
            .build(device, memoryAllocator);
    }

    @Override
    protected UiShaderProgram createShaderProgram() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        return new UiShaderProgram(device, renderPass);
    }

    @Override
    protected void createSynchronizationObjects() {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();

        this.signalSemaphores = new ArrayList<>();
        this.inFlightFences = new ArrayList<>();

        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            signalSemaphores.add(new VulkanSemaphore(device));
            inFlightFences.add(new VulkanFence(device));
        }
    }

    //We need to wait for the previous render to be finished and for the image to be available
    //QUESTION: Validate the waitdstStageMask
    //We need to render using the renderData
    @Override
    public void render(MemoryStack stack, VulkanContext context) {

        LongBuffer pWaitSemaphores = stack.callocLong(1);
        pWaitSemaphores.put(0, waitSemaphores.get(renderData.getFrameIndex()).getHandle());

        IntBuffer pWaitDstStageMask = stack.callocInt(1);
        pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, signalSemaphores.get(renderData.getFrameIndex()).getHandle());

        VulkanFramebuffer framebuffer = framebufferObject.getFramebuffer(renderData.getFrameIndex());
        recordCommandBuffer(stack, framebuffer,  framebuffer.getWidth(), framebuffer.getHeight(), renderData.getFrameIndex());
        submitPrimaryCommandBuffer(stack, context, 1, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFences.get(renderData.getFrameIndex()), renderData.getFrameIndex());
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
        VkClearValue.Buffer pClearValues = VkClearValue.calloc(1, stack);
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
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
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
