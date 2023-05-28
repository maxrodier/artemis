package ca.artemis.engine.rendering;

import java.util.List;

import org.lwjgl.system.MemoryStack;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.context.VulkanMemoryAllocator;
import ca.artemis.engine.vulkan.api.framebuffer.FramebufferObject;
import ca.artemis.engine.vulkan.api.framebuffer.RenderPass;
import ca.artemis.engine.vulkan.api.synchronization.VulkanFence;
import ca.artemis.engine.vulkan.api.synchronization.VulkanSemaphore;
import ca.artemis.engine.vulkan.core.ShaderProgram;

public abstract class Renderer<T extends RenderData, F extends FramebufferObject, P extends ShaderProgram> implements AutoCloseable {
    
    protected T renderData;

    protected RenderPass renderPass;
    protected F framebufferObject;
    protected P shaderProgram;

    protected List<VulkanSemaphore> waitSemaphores;
    protected List<VulkanSemaphore> signalSemaphores;
    protected List<VulkanFence> inFlightFences;

    protected Renderer(List<VulkanSemaphore> waitSemaphores) {
        this.renderPass = createRenderPass();
        this.framebufferObject = createFramebufferObject();
        this.shaderProgram = createShaderProgram();

        this.waitSemaphores = waitSemaphores;
        createSynchronizationObjects();
    }

    @Override
    public void close() throws Exception {
        VulkanDevice device = LowPolyEngine.instance().getContext().getDevice();
        VulkanMemoryAllocator memoryAllocator = LowPolyEngine.instance().getContext().getMemoryAllocator();

        for(VulkanFence inFlightFence : inFlightFences) {
            inFlightFence.destroy(device);
        }

        for(VulkanSemaphore signalSemaphore : signalSemaphores) {
            signalSemaphore.destroy(device);
        }

        shaderProgram.close();
        framebufferObject.destroy(device, memoryAllocator);
        renderPass.destroy(device);
    }

    protected abstract RenderPass createRenderPass();
    protected abstract P createShaderProgram();
    protected abstract F createFramebufferObject();
    protected abstract void createSynchronizationObjects();

    public abstract void render(MemoryStack stack, VulkanContext context);

    public void regenerate() {
        VulkanContext context = LowPolyEngine.instance().getContext();
        
        framebufferObject.destroy(context.getDevice(), context.getMemoryAllocator());
        renderPass.destroy(context.getDevice());

        this.renderPass = createRenderPass();
        this.framebufferObject = createFramebufferObject();

        this.shaderProgram.regenerateGraphicsPipeline(context.getDevice(), renderPass);
    }

    public T getRenderData() {
        return renderData;
    }

    public void setRenderData(T renderData) { //TODO: do we want to set render data?
        this.renderData = renderData;
    }

    public RenderPass getRenderPass() {
        return renderPass;
    }

    public P getShaderProgram() {
        return shaderProgram;
    }

    public F getFramebufferObject() {
        return framebufferObject;
    }

    public List<VulkanSemaphore> getSignalSemaphores() {
        return signalSemaphores;
    }

    public List<VulkanFence> getinFlightFences() {
        return inFlightFences;
    }
}
