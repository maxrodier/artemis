#version 450

precision highp float;

layout(location = 0) in vec3 fragColour;
layout(location = 1) in vec2 fragTexCoord;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

layout(location = 0) out vec4 outColour;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msd = texture(texSampler, fragTexCoord).rgb;
    float sd = median(msd.r, msd.g, msd.b);
    float w = fwidth(sd);
    float opacity = smoothstep(0.5 - w, 0.5 + w, sd);
    outColour = vec4(fragColour.rgb, opacity);
}