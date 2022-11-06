#version 450

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec4 aColor;
layout(location = 2) in vec2 aTex;

layout(location = 0) out vec4 fColor;
layout(location = 1) out vec2 fTex;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 projection;
    mat4 view;
} ubo;


void main() {
    fColor = aColor;
    fTex = aTex;

    gl_Position = ubo.projection * ubo.view * vec4(aPos, 1.0);
    gl_Position.y = -gl_Position.y;
}