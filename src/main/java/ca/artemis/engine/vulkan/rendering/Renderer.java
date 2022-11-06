package ca.artemis.engine.vulkan.rendering;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;

import ca.artemis.engine.vulkan.api.commands.CommandPool;
import ca.artemis.engine.vulkan.api.commands.PrimaryCommandBuffer;
import ca.artemis.engine.vulkan.api.commands.SecondaryCommandBuffer;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanPhysicalDevice;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.memory.VulkanFramebuffer;
import ca.artemis.game.Constants;

public abstract class Renderer {
    
    protected RenderPass renderPass;

    private List<CommandPool> commandPools;
    private List<PrimaryCommandBuffer> commandBuffers;
    private HashMap<String, List<SecondaryCommandBuffer>> secondaryCommandBuffersMaps;
    private List<List<String>> executionLists;

    public Renderer() {
        VulkanContext context = VulkanContext.getContext();

        createCommandPools(context.getDevice(), context.getPhysicalDevice());
        createCommandBuffers(context.getDevice());

        this.secondaryCommandBuffersMaps = new HashMap<>();

        this.executionLists = new ArrayList<>();
        for(int i = 0; i < RenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            executionLists.add(new ArrayList<>());
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

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public abstract void render(MemoryStack stack, VulkanContext context, LongBuffer pWaitSemaphores, LongBuffer pSignalSemaphores);

    public void recordPrimaryCommandBuffer(MemoryStack stack, VulkanFramebuffer framebuffer, int width, int height, int frameIndex) {
        VkClearValue.Buffer pClearValues = VkClearValue.callocStack(1, stack);
        pClearValues.get(0).color().float32(0, Constants.rClearColor);
        pClearValues.get(0).color().float32(1, Constants.gClearColor);
        pClearValues.get(0).color().float32(2, Constants.bClearColor);
        pClearValues.get(0).color().float32(3, 1.0f);

        PrimaryCommandBuffer commandBuffer = commandBuffers.get(frameIndex);
        commandBuffer.beginRecording(null, 0);
        commandBuffer.beginRenderPassCmd(stack, renderPass.getHandle(), framebuffer.getHandle(), width, height, pClearValues, VK11.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        List<String> executionList = executionLists.get(frameIndex);
        PointerBuffer pSecondaryCommandBuffers = stack.callocPointer(executionList.size());
        for(String key: executionList) {
            pSecondaryCommandBuffers.put(secondaryCommandBuffersMaps.get(key).get(frameIndex).getHandle());
        }
        VK11.vkCmdExecuteCommands(commandBuffer.getCommandBuffer(), pSecondaryCommandBuffers);
        
        commandBuffer.endRenderPassCmd();
        commandBuffer.endRecording();
    }
}
