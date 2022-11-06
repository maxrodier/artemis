#version 450

layout(location = 0) in vec4 fColor;
layout(location = 1) in vec2 fTex;

layout(location = 0) out vec4 oColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

void main() {
    oColor = texture(texSampler, fTex);
}