package ca.artemis.engine.rendering.swapchain;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;

import ca.artemis.engine.LowPolyEngine;
import ca.artemis.engine.rendering.RenderData;
import ca.artemis.engine.vulkan.api.context.VulkanContext;
import ca.artemis.engine.vulkan.api.context.VulkanDevice;
import ca.artemis.engine.vulkan.api.descriptor.DescriptorSet;
import ca.artemis.engine.vulkan.api.memory.VulkanSampler;
import ca.artemis.engine.vulkan.core.mesh.Quad;
import ca.artemis.engine.vulkan.core.mesh.Vertex.VertexKind;
import ca.artemis.game.rendering.LowPolyRenderingEngine;

public class SwapchainRenderData extends RenderData {

    private Quad quad;
    private List<VulkanSampler> textureSamplers;
    private List<DescriptorSet> descriptorSets;

    public SwapchainRenderData() {
        VulkanContext context = LowPolyEngine.instance().getContext();

        this.quad = new Quad(context, VertexKind.POS_UV);
        createTextureSamplers(context.getDevice());
        createDescriptorSets(context, LowPolyRenderingEngine.instance().getSwapchainRenderer());
    }

    private void createTextureSamplers(VulkanDevice device) {
        textureSamplers = new ArrayList<>();

        for(int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
            VulkanSampler textureSampler = new VulkanSampler.Builder()
            .setAddressModeU(VK11.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
            .setAddressModeV(VK11.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
            .setAddressModeW(VK11.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
            .build(device);

            textureSamplers.add(textureSampler);
        }
    }

    private void createDescriptorSets(VulkanContext context, SwapchainRenderer swapchainRenderer) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            descriptorSets = new ArrayList<>();
            for (int i = 0; i < LowPolyRenderingEngine.MAX_FRAMES_IN_FLIGHT; i++) {
                DescriptorSet descriptorSet = new DescriptorSet(context.getDevice(), swapchainRenderer.getShaderProgram().getDescriptorPool(), swapchainRenderer.getShaderProgram().getDescriptorSetLayouts()[0]);
                descriptorSet.updateDescriptorImageBuffer(context.getDevice(), swapchainRenderer.getRenderSource().getImageView(i), textureSamplers.get(i), VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                descriptorSets.add(descriptorSet);
            }
        }
    }

    //EndTempStuff

    private int imageIndex;

    @Override
    public void update(MemoryStack stack) {
        VulkanContext context = LowPolyEngine.instance().getContext();
        SwapchainRenderer swapchainRenderer = LowPolyRenderingEngine.instance().getSwapchainRenderer();
        descriptorSets.get(getFrameIndex()).updateDescriptorImageBuffer(context.getDevice(), swapchainRenderer.getRenderSource().getImageView(getFrameIndex()), textureSamplers.get(getFrameIndex()), VK11.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, 0, VK11.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

    public Quad getQuad() {
        return quad;
    }

    public DescriptorSet getDescriptorSet(int index) {
        return descriptorSets.get(index);
    }

    @Override
    public void close() throws Exception {
        VulkanContext context = LowPolyEngine.instance().getContext();
        for(VulkanSampler textureSampler : textureSamplers) {
            textureSampler.destroy(context.getDevice());
        }
        quad.destroy(context.getMemoryAllocator());
    }
}
