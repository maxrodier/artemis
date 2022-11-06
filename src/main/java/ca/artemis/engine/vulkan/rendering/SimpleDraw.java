package ca.artemis.engine.vulkan.rendering;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.engine.vulkan.api.commands.CommandBuffer;
import ca.artemis.engine.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.engine.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice.QueueFamily;
import ca.artemis.game.Constants;

public class SimpleDraw {
    
    private long commandPool;
    private List<CommandBuffer> commandBuffers; //One per frame in flight
    private List<SecondaryCommandBuffer> secondaryCommandBuffers; //One per frame in flight //Remove

    public SimpleDraw(VulkanContext context) {
        createCommandPool(context);
        createCommandBuffers(context);
        createSecondaryCommandBuffers(context);
    }

    public void destroy(VulkanContext context) {
        VK11.vkDestroyCommandPool(context.getDevice().getHandle(), commandPool, null);
    }

    private void createCommandPool(VulkanContext context) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            List<QueueFamily> queueFamilies = QueueFamily.getQueueFamilies(context.getSurface(), context.getPhysicalDevice().getHandle());
    
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.flags(VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            poolInfo.queueFamilyIndex(queueFamilies.get(0).getIndex());
    
            LongBuffer pCommandPool = stack.callocLong(1);
            if(VK11.vkCreateCommandPool(context.getDevice().getHandle(), poolInfo, null, pCommandPool) != VK11.VK_SUCCESS) {
                throw new RuntimeException("failed to create graphics command pool!");
            }
            commandPool = pCommandPool.get(0);
        }
    }

    private void createCommandBuffers(VulkanContext context) {
        commandBuffers = new ArrayList<>();
        for(int i = 0; i < Presenter.MAX_FRAMES_IN_FLIGHT; i++) {
            commandBuffers.add(new PrimaryCommandBuffer(context.getDevice().getHandle(), commandPool));
        }
    }

    private void createSecondaryCommandBuffers(VulkanContext context) {
        secondaryCommandBuffers = new ArrayList<>();
        for(int i = 0; i < Presenter.MAX_FRAMES_IN_FLIGHT; i++) {
            secondaryCommandBuffers.add(new SecondaryCommandBuffer(context.getDevice().getHandle(), commandPool));
        }
    }

    private void recordCommandBuffer(MemoryStack stack, Presenter presenter, int frameIndex, int imageIndex) {
        VkCommandBuffer commandBuffer = commandBuffers.get(frameIndex).getCommandBuffer();
        VK11.vkResetCommandBuffer(commandBuffer, 0);

        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
        beginInfo.sType(VK11.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        if(VK11.vkBeginCommandBuffer(commandBuffer, beginInfo) != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to begin recording command buffer!");
        }

        SwapchainRenderer swapchainRenderer = presenter.getSwapchainRenderer();

        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
        renderPassInfo.sType(VK11.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(swapchainRenderer.getRenderPass().getHandle());
        renderPassInfo.framebuffer(swapchainRenderer.getSwapchain().getFramebuffer(imageIndex).getHandle());
        renderPassInfo.renderArea().offset().x(0);
        renderPassInfo.renderArea().offset().y(0);
        renderPassInfo.renderArea().extent(swapchainRenderer.getSurfaceSupportDetails().getSurfaceExtent());

        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.get(0).color().float32(0, Constants.rClearColor);
        clearValues.get(0).color().float32(1, Constants.gClearColor);
        clearValues.get(0).color().float32(2, Constants.bClearColor);
        clearValues.get(0).color().float32(3, 1.0f);
        
        renderPassInfo.pClearValues(clearValues);

        VK11.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK11.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        long secondaryCommandBuffer = secondaryCommandBuffers.get(frameIndex).getHandle();
        PointerBuffer pCommandBuffers = stack.callocPointer(1); //Size for the number of secondary command buffers for this frame
        pCommandBuffers.put(0, secondaryCommandBuffer); //Add all commandBuffers in the selected list for this frame

        VK11.vkCmdExecuteCommands(commandBuffer, pCommandBuffers);

        VK11.vkCmdEndRenderPass(commandBuffer);

        if(VK11.vkEndCommandBuffer(commandBuffer) != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to record command buffer!");
        }
    }

    public LongBuffer draw(MemoryStack stack, VulkanContext context, Presenter presenter, int imageIndex) {
        int currentFrame = presenter.getCurrentFrame();
        recordCommandBuffer(stack, presenter, currentFrame, imageIndex);

        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

        LongBuffer pWaitSemaphores = stack.callocLong(1);
        pWaitSemaphores.put(0, presenter.getImageAvailableSemaphore());

        IntBuffer pWaitDstStageMask = stack.callocInt(1);
        pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitSemaphores(pWaitSemaphores);
        submitInfo.pWaitDstStageMask(pWaitDstStageMask);

        PointerBuffer pCommandBuffers = stack.callocPointer(1);
        pCommandBuffers.put(0, commandBuffers.get(currentFrame).getCommandBuffer());

        submitInfo.pCommandBuffers(pCommandBuffers);

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, presenter.getRenderFinishedSemaphore());

        submitInfo.pSignalSemaphores(pSignalSemaphores);

        if(VK11.vkQueueSubmit(context.getDevice().getGraphicsQueue(), submitInfo, presenter.getinFlightFence()) != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to submit draw command buffer!");
        }

        return pSignalSemaphores;
    }

    public SecondaryCommandBuffer getSecondaryCommandBuffer(int index) {
        return secondaryCommandBuffers.get(index);
    }
}
