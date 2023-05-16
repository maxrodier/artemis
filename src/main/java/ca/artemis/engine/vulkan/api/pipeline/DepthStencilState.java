package ca.artemis.vulkan.api.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;

public class DepthStencilState {
    
    private boolean depthTestEnable = false;
    private boolean depthWriteEnable = false;
    private int depthCompareOp = VK11.VK_COMPARE_OP_LESS;
    private boolean depthBoundsTestEnable = false;
    private boolean stencilTestEnable = false;

    public VkPipelineDepthStencilStateCreateInfo buildDepthStencilStateCreateInfo(MemoryStack stack) {
        return VkPipelineDepthStencilStateCreateInfo.callocStack(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
            .depthTestEnable(depthTestEnable)
            .depthWriteEnable(depthWriteEnable)
            .depthCompareOp(depthCompareOp)
            .depthBoundsTestEnable(depthBoundsTestEnable)
            .stencilTestEnable(stencilTestEnable);
    }

    public DepthStencilState setDepthTestEnable(boolean depthTestEnable) {
        this.depthTestEnable = depthTestEnable;
        return this;
    }

    public DepthStencilState setDepthWriteEnable(boolean depthWriteEnable) {
        this.depthWriteEnable = depthWriteEnable;
        return this;
    }

    public DepthStencilState setDepthCompareOp(int depthCompareOp) {
        this.depthCompareOp = depthCompareOp;
        return this;
    }

    public DepthStencilState setDepthBoundsTestEnable(boolean depthBoundsTestEnable) {
        this.depthBoundsTestEnable = depthBoundsTestEnable;
        return this;
    }

    public DepthStencilState setStencilTestEnable(boolean stencilTestEnable) {
        this.stencilTestEnable = stencilTestEnable;
        return this;
    }
}