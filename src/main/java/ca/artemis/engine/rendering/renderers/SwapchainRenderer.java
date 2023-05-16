package ca.artemis.engine.rendering.renderers;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkSubmitInfo;

import ca.artemis.engine.api.vulkan.commands.CommandPool;
import ca.artemis.engine.api.vulkan.commands.PrimaryCommandBuffer;
import ca.artemis.engine.api.vulkan.commands.SecondaryCommandBuffer;
import ca.artemis.engine.api.vulkan.core.GLFWWindow;
import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanPhysicalDevice;
import ca.artemis.engine.api.vulkan.core.VulkanSurface;
import ca.artemis.engine.api.vulkan.descriptor.DescriptorSet;
import ca.artemis.engine.api.vulkan.framebuffer.RenderPass;
import ca.artemis.engine.api.vulkan.framebuffer.SurfaceSupportDetails;
import ca.artemis.engine.api.vulkan.framebuffer.Swapchain;
import ca.artemis.engine.api.vulkan.framebuffer.VulkanFramebuffer;
import ca.artemis.engine.api.vulkan.memory.VulkanBuffer;
import ca.artemis.engine.api.vulkan.synchronization.VulkanFence;
import ca.artemis.engine.ecs.systems.VulkanRenderingSystem;
import ca.artemis.engine.rendering.FrameInfo;
import ca.artemis.engine.rendering.programs.SwapchainShaderProgram;
import ca.artemis.engine.rendering.resources.Mesh;

public class SwapchainRenderer extends Renderer {
    
    private List<CommandPool> commandPools;
    private List<PrimaryCommandBuffer> commandBuffers;
    private HashMap<String, List<SecondaryCommandBuffer>> secondaryCommandBuffersMaps;
    private List<List<String>> executionLists;

    private SurfaceSupportDetails surfaceSupportDetails;
    private RenderPass renderPass;
    private Swapchain swapchain;


    private Mesh model;
    private List<VulkanBuffer> uniformBuffers; //One per frame in flight
    private List<DescriptorSet> descriptorSets; //One per frame in flight
    private List<SecondaryCommandBuffer> secondaryCommandBuffers;

    private SwapchainShaderProgram  swapchainShaderProgram;

    public SwapchainRenderer(VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, GLFWWindow window) {
        createCommandPools(device, physicalDevice);
        createCommandBuffers(device);

        
        this.surfaceSupportDetails = new SurfaceSupportDetails(physicalDevice, surface, window);
        this.renderPass = creatRenderPass(device, surfaceSupportDetails);
        this.swapchain = new Swapchain(device, surface, renderPass, surfaceSupportDetails);

        this.swapchainShaderProgram = new SwapchainShaderProgram(device, renderPass, 64);
    }

    public void destroy(VulkanDevice device) {
        swapchain.destroy(device);
        renderPass.destroy(device);
        surfaceSupportDetails.destroy();
        commandPools.forEach(pool -> pool.destroy(device));
    }

    public void createCommandPools(VulkanDevice device, VulkanPhysicalDevice physicalDevice) {
        this.commandPools = new ArrayList<>();
        int queueFamilyIndex = device.getGraphicsQueueIndex(); //TODO: Find a better way to get this ....

        for(int i = 0; i < VulkanRenderingSystem.MAX_FRAMES_IN_FLIGHT; i++) {
            commandPools.add(new CommandPool(device, queueFamilyIndex, VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT));
        }
    }

    private void createCommandBuffers(VulkanDevice device) {
        this.commandBuffers = new ArrayList<>();
        for(int i = 0; i < VulkanRenderingSystem.MAX_FRAMES_IN_FLIGHT; i++) {
            commandBuffers.add(new PrimaryCommandBuffer(device, commandPools.get(i)));
        }
    }


    public List<SecondaryCommandBuffer> allocateSecondaryCommandBuffers(VulkanDevice device, String key) {
        if(secondaryCommandBuffersMaps.containsKey(key)) {
            throw new AssertionError("SecondaryCommandBuffers already allocated for the following key:" + key);
        }

        List<SecondaryCommandBuffer> secondaryCommandBuffers = new ArrayList<>();
        for(int i = 0; i < VulkanRenderingSystem.MAX_FRAMES_IN_FLIGHT; i++) {
            secondaryCommandBuffers.add(new SecondaryCommandBuffer(device, commandPools.get(i)));
        }
        secondaryCommandBuffersMaps.put(key, secondaryCommandBuffers);

        return secondaryCommandBuffers;
    }

    private static RenderPass creatRenderPass(VulkanDevice device, SurfaceSupportDetails surfaceSupportDetails) {
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

    public IntBuffer acquireNextSwapchainImage(MemoryStack stack, VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, GLFWWindow window, FrameInfo frameInfo) {
        LongBuffer pFence = stack.callocLong(1);
        pFence.put(0, frameInfo.inFlightFence.getHandle());
        VK11.vkWaitForFences(device.getHandle(), pFence, true, Long.MAX_VALUE);

        IntBuffer pImageIndex = stack.callocInt(1);
        int result = KHRSwapchain.vkAcquireNextImageKHR(device.getHandle(), swapchain.getHandle(), Long.MAX_VALUE, frameInfo.imageAvailableSemaphore.getHandle(), VK11.VK_NULL_HANDLE, pImageIndex);
        if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
            regenerateRenderer(device, physicalDevice, surface, window);
        } else if(result != VK11.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("Failed to acquire swapchain image: " + result);
        }

        VK11.vkResetFences(device.getHandle(), pFence);

        return pImageIndex;
    }

    public void draw(MemoryStack stack, VulkanDevice device, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int imageIndex, int frameIndex) {
        VulkanFramebuffer framebuffer = swapchain.getFramebuffer(imageIndex);
        recordCommandBuffer(stack, framebuffer,  framebuffer.getWidth(), framebuffer.getHeight(), frameIndex);
        submitPrimaryCommandBuffer(stack, device, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFence, frameIndex);
    }

    public void recordCommandBuffer(MemoryStack stack, VulkanFramebuffer framebuffer, int width, int height, int frameIndex) {
        VkClearValue.Buffer pClearValues = VkClearValue.calloc(1, stack);
        pClearValues.get(0).color().float32(0, 0.0f);
        pClearValues.get(0).color().float32(1, 1.0f);
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

    public void submitPrimaryCommandBuffer(MemoryStack stack, VulkanDevice device, LongBuffer pWaitSemaphores, IntBuffer pWaitDstStageMask, LongBuffer pSignalSemaphores, VulkanFence inFlightFence, int frameIndex) {
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
        submitInfo.sType(VK11.VK_STRUCTURE_TYPE_SUBMIT_INFO);

        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitSemaphores(pWaitSemaphores);
        submitInfo.pWaitDstStageMask(pWaitDstStageMask);

        PointerBuffer pCommandBuffers = stack.callocPointer(1);
        pCommandBuffers.put(0, commandBuffers.get(frameIndex).getCommandBuffer());

        submitInfo.pCommandBuffers(pCommandBuffers);
        submitInfo.pSignalSemaphores(pSignalSemaphores);

        int result = VK11.vkQueueSubmit(device.getGraphicsQueue(), submitInfo, inFlightFence.getHandle());
        if(result != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to submit draw command buffer: " + result);
        }
    }

    public void addToExecutionList(String key, int frameIndex) {
        executionLists.get(frameIndex).add(key);
    }

    public void regenerateRenderer(VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, GLFWWindow window) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.callocInt(1), height = stack.callocInt(1);
            GLFW.glfwGetFramebufferSize(window.getHandle(), width, height);
            while(width.get(0) == 0 || height.get(0) == 0) {
                width.clear(); height.clear();
                GLFW.glfwGetFramebufferSize(window.getHandle(), width, height);
                GLFW.glfwWaitEvents();
            }
        }

        device.waitIdle();
        
        swapchain.destroy(device);
        renderPass.destroy(device);
        surfaceSupportDetails.destroy();

        this.surfaceSupportDetails = new SurfaceSupportDetails(physicalDevice, surface, window);
        this.renderPass = creatRenderPass(device, surfaceSupportDetails);
        this.swapchain = new Swapchain(device, surface, renderPass, surfaceSupportDetails);
    }

    public Swapchain getSwapchain() { 
        return swapchain; 
    }
}
