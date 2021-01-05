#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inColour;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec3 fragColour;
layout(location = 1) out vec2 fragTexCoord;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 mvp;
} ubo;

void main() {
    gl_Position = ubo.mvp * vec4(inPosition, 1.0);

    fragColour = inColour;
    fragTexCoord = inTexCoord;
}
