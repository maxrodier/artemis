package ca.artemis.engine.vulkan.api.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;

public class RasterizationState {
        
    private boolean depthClampEnable;
    private boolean rasterizerDiscardEnable;
    private int polygonMode = VK11.VK_POLYGON_MODE_FILL;
    private int cullMode = VK11.VK_CULL_MODE_BACK_BIT;
    private int frontFace = VK11.VK_FRONT_FACE_CLOCKWISE;
    private boolean depthBiasEnable;
    private float depthBiasConstantFactor;
    private float depthBiasClamp;
    private float depthBiasSlopeFactor;
    private float lineWidth = 1.0f;

    public VkPipelineRasterizationStateCreateInfo buildRasterizationStateCreateInfo(MemoryStack stack) {
        return VkPipelineRasterizationStateCreateInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
            .depthClampEnable(depthClampEnable)
            .rasterizerDiscardEnable(rasterizerDiscardEnable)
            .polygonMode(polygonMode)
            .cullMode(cullMode)
            .frontFace(frontFace)
            .depthBiasEnable(depthBiasEnable)
            .depthBiasConstantFactor(depthBiasConstantFactor)
            .depthBiasClamp(depthBiasClamp)
            .depthBiasSlopeFactor(depthBiasSlopeFactor)
            .lineWidth(lineWidth);
    }

    public RasterizationState setDepthClampEnable(boolean depthClampEnable) {
        this.depthClampEnable = depthClampEnable;
        return this;
    }

    public RasterizationState setRasterizerDiscardEnable(boolean rasterizerDiscardEnable) {
        this.rasterizerDiscardEnable = rasterizerDiscardEnable;
        return this;
    }

    public RasterizationState setPolygonMode(int polygonMode) {
        this.polygonMode = polygonMode;
        return this;
    }

    public RasterizationState setCullMode(int cullMode) {
        this.cullMode = cullMode;
        return this;
    }

    public RasterizationState setFrontFace(int frontFace) {
        this.frontFace = frontFace;
        return this;
    }

    public RasterizationState setDepthBiasEnable(boolean depthBiasEnable) {
        this.depthBiasEnable = depthBiasEnable;
        return this;
    }

    public RasterizationState setDepthBiasConstantFactor(float depthBiasConstantFactor) {
        this.depthBiasConstantFactor = depthBiasConstantFactor;
        return this;
    }

    public RasterizationState setDepthBiasClamp(float depthBiasClamp) {
        this.depthBiasClamp = depthBiasClamp;
        return this;
    }

    public RasterizationState setDepthBiasSlopeFactor(float depthBiasSlopeFactor) {
        this.depthBiasSlopeFactor = depthBiasSlopeFactor;
        return this;
    }

    public RasterizationState setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }
}