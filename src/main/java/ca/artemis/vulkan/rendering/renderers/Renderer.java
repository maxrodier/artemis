package ca.artemis.vulkan.rendering.renderers;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.vulkan.api.commands.CommandBuffer;
import ca.artemis.vulkan.api.commands.CommandPool;
import ca.artemis.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.vulkan.api.context.VulkanContext;
import ca.artemis.vulkan.api.context.VulkanDevice;
import ca.artemis.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.vulkan.api.framebuffer.RenderPass;
import ca.artemis.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.vulkan.api.synchronization.VulkanFence;
import ca.artemis.vulkan.rendering.RenderingEngine;

public abstract class Renderer {
    
    protected RenderPass renderPass;

    private List<CommandPool> commandPools;
    private List<PrimaryCommandBuffer> commandBuffers;
    private HashMap<String, List<SecondaryCommandBuffer>> secondaryCommandBuffersMaps;
    private List<List<String>> executionLists;
    
    protected Renderer(VulkanContext context) {
        createCommandPools(context.getDevice(), context.getPhysicalDevice());
        createCommandBuffers(context.getDevice());

        this.secondaryCommandBuffersMaps = new HashMap<>();

        this.executionLists = new ArrayList<>();
        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            executionLists.add(new ArrayList<>());
        }
    }

    public void destroy(VulkanContext context) {
        for(CommandPool commandPool : commandPools) {
            commandPool.destroy(context.getDevice());
        }
    }

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

    public List<SecondaryCommandBuffer> allocateSecondaryCommandBuffers(VulkanDevice device, String key) {
        if(secondaryCommandBuffersMaps.containsKey(key)) {
            throw new AssertionError("SecondaryCommandBuffers already allocated for the following key:" + key);
        }

        List<SecondaryCommandBuffer> secondaryCommandBuffers = new ArrayList<>();
        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            secondaryCommandBuffers.add(new SecondaryCommandBuffer(device, commandPools.get(i)));
        }
        secondaryCommandBuffersMaps.put(key, secondaryCommandBuffers);

        return secondaryCommandBuffers;
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
        commandBuffer.beginRenderPassCmd(stack, renderPass.getHandle(), framebuffer.getHandle(), width, height, pClearValues, VK11.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        List<String> executionList = executionLists.get(frameIndex);
        PointerBuffer pSecondaryCommandBuffers = stack.callocPointer(executionList.size());
        for(int i = 0; i < executionList.size(); i++) {
            pSecondaryCommandBuffers.put(i, secondaryCommandBuffersMaps.get(executionList.get(i)).get(frameIndex).getHandle());
        }
        executionList.clear();
        VK11.vkCmdExecuteCommands(commandBuffer.getCommandBuffer(), pSecondaryCommandBuffers);

        commandBuffer.endRenderPassCmd();
        commandBuffer.endRecording();
    }

    public void submitPrimaryCommandBuffer(MemoryStack stack, VulkanContext context, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int frameIndex) {
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

        submitInfo.waitSemaphoreCount(1);
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

    public void addToExecutionList(String key, int frameIndex) {
        executionLists.get(frameIndex).add(key);
    }

    public abstract void draw(MemoryStack stack, VulkanContext context, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int imageIndex, int frameIndex);

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public CommandBuffer getCommandBuffer(int index) {
        return commandBuffers.get(index);
    }
}
