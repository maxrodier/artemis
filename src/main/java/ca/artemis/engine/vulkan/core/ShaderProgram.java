package ca.artemis.engine.vulkan.core;

import java.util.ArrayList;
import java.util.List;

import ca.artemis.engine.vulkan.api.memory.VulkanBuffer;

public abstract class ShaderProgram {
    
    private List<VulkanBuffer> buffers = new ArrayList<>();

    // layout(binding = 0) uniform UniformBufferObject {
    //     mat4 model;
    //     mat4 view;
    //     mat4 proj;
    // } ubo;
}
