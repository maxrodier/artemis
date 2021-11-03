#version 450

precision highp float;

layout(location = 0) in vec3 fragColour;
layout(location = 1) in vec2 fragTexCoord;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

layout(location = 0) out vec4 outColour;

const float smoothing = 1.0/32.0;

void main() {

    float distance = texture(texSampler, fragTexCoord).r;
    float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    outColour = vec4(fragColour.rgb * alpha, 1 * alpha);
}