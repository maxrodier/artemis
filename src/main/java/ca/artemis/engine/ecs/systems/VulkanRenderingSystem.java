package ca.artemis.engine.ecs.systems;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

import ca.artemis.engine.api.vulkan.commands.CommandPool;
import ca.artemis.engine.api.vulkan.commands.SecondaryCommandBuffer;
import ca.artemis.engine.api.vulkan.core.GLFWWindow;
import ca.artemis.engine.api.vulkan.core.VulkanDevice;
import ca.artemis.engine.api.vulkan.core.VulkanMemoryAllocator;
import ca.artemis.engine.api.vulkan.core.VulkanPhysicalDevice;
import ca.artemis.engine.api.vulkan.core.VulkanPhysicalDeviceManager;
import ca.artemis.engine.api.vulkan.core.VulkanSurface;
import ca.artemis.engine.api.vulkan.descriptor.DescriptorSet;
import ca.artemis.engine.api.vulkan.framebuffer.Swapchain;
import ca.artemis.engine.api.vulkan.synchronization.VulkanFence;
import ca.artemis.engine.api.vulkan.synchronization.VulkanSemaphore;
import ca.artemis.engine.core.EngineSettings;
import ca.artemis.engine.core.ecs.BaseSystem;
import ca.artemis.engine.core.ecs.Entity;
import ca.artemis.engine.ecs.components.CameraComponent;
import ca.artemis.engine.ecs.components.MaterialComponent;
import ca.artemis.engine.ecs.components.MeshComponent;
import ca.artemis.engine.ecs.components.TransformComponent;
import ca.artemis.engine.rendering.FrameInfo;
import ca.artemis.engine.rendering.renderers.SwapchainRenderer;
import ca.artemis.engine.rendering.resources.Mesh;
import ca.artemis.engine.rendering.resources.managers.MaterialResourcesManager;
import ca.artemis.engine.rendering.resources.managers.MeshResourcesManager;

public class VulkanRenderingSystem extends BaseSystem {
    
    public static final int MAX_FRAMES_IN_FLIGHT = 2;
    
    private final WindowSystem windowSystem;

    private VulkanPhysicalDeviceManager deviceManager;
    private VulkanPhysicalDevice physicalDevice;
    private VulkanDevice device;
    private VulkanMemoryAllocator allocator;

    private CommandPool commandPool;

    private SwapchainRenderer swapchainRenderer;

    private List<VulkanSemaphore> imageAvailableSemaphores = new ArrayList<>(); //One per frame in flight
    private List<VulkanSemaphore> renderFinishedSemaphores = new ArrayList<>();; //One per frame in flight
    private List<VulkanFence> inFlightFences = new ArrayList<>();; //One per frame in flight

    private int currentFrame = 0;

    private MaterialResourcesManager materialResourcesManager;
    private MeshResourcesManager meshResourcesManager;

    public VulkanRenderingSystem(WindowSystem windowSystem) {
        this.windowSystem = windowSystem;
    }

    @Override
    public void init(EngineSettings engineSettings) {
        deviceManager = new VulkanPhysicalDeviceManager(windowSystem.getInstance());
        VkPhysicalDeviceFeatures requiredFeatures = VkPhysicalDeviceFeatures.calloc();
        physicalDevice = deviceManager.selectPhysicalDevice(windowSystem.getInstance(), engineSettings.deviceRequiredExtensions, requiredFeatures, windowSystem.getSurface());
        device = new VulkanDevice(physicalDevice, windowSystem.getSurface(), engineSettings.deviceRequiredExtensions, physicalDevice.getPhysicalDeviceFeatures());
        requiredFeatures.free();

        allocator = new VulkanMemoryAllocator(windowSystem.getInstance(), physicalDevice, device);

        commandPool = new CommandPool(device, device.getGraphicsQueueIndex(), VK11.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);

        swapchainRenderer = new SwapchainRenderer(device, physicalDevice, windowSystem.getSurface(), windowSystem.getWindow());

        createSynchronizationObjects(device, imageAvailableSemaphores, renderFinishedSemaphores, inFlightFences);
        addResizeListener(device, physicalDevice, windowSystem.getSurface(), windowSystem.getWindow(), swapchainRenderer);
    }

    @Override
    public void initComponents() {
        for(Entity entity : entities.values()) {
            if(entity.hasComponent(MaterialComponent.class)) {
                MaterialComponent materialComponent = entity.getComponent(MaterialComponent.class);
                materialComponent.setMaterial(materialResourcesManager.getMaterial(materialComponent.getName()));
            }
            if(entity.hasComponent(MeshComponent.class)) {
                MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
                meshComponent.setMesh(meshResourcesManager.getMesh(meshComponent.getName()));
            }
        }
    }

    @Override
    public void destroy() {
        for(VulkanFence fence : inFlightFences) {
            fence.destroy(device);
        }
        for(VulkanSemaphore semaphore : renderFinishedSemaphores) {
            semaphore.destroy(device);
        }
        for(VulkanSemaphore semaphore : imageAvailableSemaphores) {
            semaphore.destroy(device);
        }

        swapchainRenderer.destroy(device);
        
        commandPool.destroy(device);

        allocator.destroy();
        device.destroy();
        deviceManager.destroy();
    }

    @Override
    public void update(float delta) {
        // TODO Auto-generated method stub
    }

    @Override
    public void render(float delta) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addEntity(Entity entity) {
        if(entity.hasComponent(TransformComponent.class)
            || entity.hasComponent(MaterialComponent.class)
            || entity.hasComponent(MeshComponent.class)
            || entity.hasComponent(CameraComponent.class)) {
            super.addEntity(entity);
        }
    }

    private static void createSynchronizationObjects(VulkanDevice device, List<VulkanSemaphore> imageAvailableSemaphores, List<VulkanSemaphore> renderFinishedSemaphores, List<VulkanFence> inFlightFences) {
        for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            imageAvailableSemaphores.add(new VulkanSemaphore(device));
            renderFinishedSemaphores.add(new VulkanSemaphore(device));
            inFlightFences.add(new VulkanFence(device));
        }
    }

    private static void addResizeListener(VulkanDevice device, VulkanPhysicalDevice physicalDevice, VulkanSurface surface, GLFWWindow window, SwapchainRenderer swapchainRenderer) {
        window.addResizeListener(() -> {
            System.out.println("Window resized to " + window.getWidth() + "x" + window.getHeight());
            swapchainRenderer.regenerateRenderer(device, physicalDevice, surface, window);
        });
    }

    private void recordSecondaryCommandBuffer(MemoryStack stack, SecondaryCommandBuffer secondaryCommandBuffer, DescriptorSet descriptorSet, Mesh model, FrameInfo frameInfo) {
        
        //Record Secondary fram buffer
        VkViewport.Buffer viewports = VkViewport.calloc(1, stack);
        VkViewport viewport = viewports.get(0);
        viewport.x(0.0f);
        viewport.y(0.0f);
        viewport.width((float) surfaceSupportDetails.getSurfaceExtent().width());
        viewport.height((float) surfaceSupportDetails.getSurfaceExtent().height());
        viewport.minDepth(0.0f);
        viewport.maxDepth(1.0f);

        VkOffset2D offset = VkOffset2D.calloc(stack);
        offset.x(0);
        offset.y(0);

        VkRect2D.Buffer scissors = VkRect2D.calloc(1, stack);
        VkRect2D scissor = scissors.get(0);
        scissor.offset(offset);
        scissor.extent(surfaceSupportDetails.getSurfaceExtent());

        VK11.vkResetCommandBuffer(secondaryCommandBuffer.getCommandBuffer(), 0);        
        secondaryCommandBuffer.beginRecording(stack, VK11.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, renderPass, swapchain.getFramebuffer(frameInfo.pImageIndex.get(0)));
        secondaryCommandBuffer.bindPipelineCmd(VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline());
        secondaryCommandBuffer.setViewportCmd(viewports);
        secondaryCommandBuffer.setScissorCmd(scissors);
        secondaryCommandBuffer.bindVertexBufferCmd(stack, model.getVertexBuffer());
        secondaryCommandBuffer.bindIndexBufferCmd(model.getIndexBuffer());
        secondaryCommandBuffer.bindDescriptorSetsCmd(stack, VK11.VK_PIPELINE_BIND_POINT_GRAPHICS, swapchainShaderProgram.getGraphicsPipeline().getPipelineLayout(), descriptorSet);
        secondaryCommandBuffer.drawIndexedCmd(model.getIndexBuffer().getLenght(), 1);
        secondaryCommandBuffer.endRecording();

        //Add object to execution list if not culled
        addToExecutionList("TestRender1", frameInfo.frameIndex);
    }

    public FrameInfo prepareRender(MemoryStack stack) {
        FrameInfo frameInfo = new FrameInfo();
        frameInfo.frameIndex = currentFrame;
        frameInfo.imageAvailableSemaphore = imageAvailableSemaphores.get(currentFrame);
        frameInfo.renderFinishedSemaphore = renderFinishedSemaphores.get(currentFrame);
        frameInfo.inFlightFence = inFlightFences.get(currentFrame);

        frameInfo.pImageIndex = swapchainRenderer.acquireNextSwapchainImage(stack, device, physicalDevice, windowSystem.getSurface(), windowSystem.getWindow(), frameInfo);
        return frameInfo;
    }

    public void render(MemoryStack stack, FrameInfo frameInfo) {
        LongBuffer pWaitSemaphores = stack.callocLong(1);
        pWaitSemaphores.put(0, imageAvailableSemaphores.get(currentFrame).getHandle());

        IntBuffer pWaitDstStageMask = stack.callocInt(1);
        pWaitDstStageMask.put(0, VK11.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        LongBuffer pSignalSemaphores = stack.callocLong(1);
        pSignalSemaphores.put(0, renderFinishedSemaphores.get(currentFrame).getHandle());

        swapchainRenderer.draw(stack, device, pWaitSemaphores, pWaitDstStageMask, pSignalSemaphores, inFlightFences.get(currentFrame), frameInfo.pImageIndex.get(0), currentFrame);
        present(stack, swapchainRenderer.getSwapchain(), pSignalSemaphores, frameInfo.pImageIndex);

        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
    }

    private void present(MemoryStack stack, Swapchain swapchain, LongBuffer pSignalSemaphores, IntBuffer pImageIndex) {
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
        presentInfo.sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pSignalSemaphores);

        LongBuffer pSwapChains = stack.callocLong(1);
        pSwapChains.put(0, swapchainRenderer.getSwapchain().getHandle());

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(pSwapChains);
        presentInfo.pImageIndices(pImageIndex);

        int result = KHRSwapchain.vkQueuePresentKHR(device.getGraphicsQueue(), presentInfo);
        if (result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            swapchainRenderer.regenerateRenderer(device, physicalDevice, windowSystem.getSurface(), windowSystem.getWindow());
        } else if (result != VK11.VK_SUCCESS) {
            throw new RuntimeException("Failed to present swapchain image: " + result);
        }
    }

    public void waitIdle() {
        device.waitIdle();
    }

    public VulkanDevice getDevice() {
        return device;
    }

    public VulkanMemoryAllocator getAllocator() {
        return allocator;
    }

    public VkQueue getGraphicsQueue() {
        return device.getGraphicsQueue();
    }

    public CommandPool getCommandPool() {
        return commandPool;
    }
}
