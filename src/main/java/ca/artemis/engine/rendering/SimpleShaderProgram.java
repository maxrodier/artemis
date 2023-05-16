package ca.artemis.engine.rendering;

import ca.artemis.engine.vulkan.core.ShaderProgram;
import ca.artemis.engine.vulkan.core.UniformMat4;

public class SimpleShaderProgram extends ShaderProgram {

    // layout(binding = 0) uniform UniformBufferObject {
    //     mat4 model;
    //     mat4 view;
    //     mat4 proj;
    // } ubo;

    protected UniformMat4 model = new UniformMat4(0, 16);
    protected UniformMat4 view = new UniformMat4(0, 32);
    protected UniformMat4 proj = new UniformMat4(0, 48);
    
}
