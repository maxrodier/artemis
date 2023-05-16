package ca.artemis.engine.api.vulkan.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;

public class TessellationState {
        
    private int patchControlPoints;

    public VkPipelineTessellationStateCreateInfo buildTessellationStateCreateInfo(MemoryStack stack) {
        return VkPipelineTessellationStateCreateInfo.calloc(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO)
            .patchControlPoints(patchControlPoints);
    }

    public TessellationState setPatchControlPoints(int patchControlPoints) {
        this.patchControlPoints = patchControlPoints;
        return this;
    }
}