package ca.artemis.engine.api.vulkan.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;

public class InputAssemblyState {

    private int topology = VK11.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
    private boolean primitiveRestartEnable = false;

    public VkPipelineInputAssemblyStateCreateInfo buildInputAssemblyStateCreateInfo(MemoryStack stack) {
        return VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
            .sType(VK11.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
            .topology(topology)
            .primitiveRestartEnable(primitiveRestartEnable);
    }

    public InputAssemblyState setTopology(int topology) {
        this.topology = topology;
        return this;
    }

    public InputAssemblyState setPrimitiveRestartEnable(boolean primitiveRestartEnable) {
        this.primitiveRestartEnable = primitiveRestartEnable;
        return this;
    }
}