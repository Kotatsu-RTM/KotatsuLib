#version 430

layout (location = 10) uniform sampler2D textureSampler;
layout (location = 11) uniform vec4 color;

layout (location = 0) in vec2 texturePosition;

out vec4 fragColor;

void main() {
    fragColor = vec4(texture(textureSampler, texturePosition) * color);
}
