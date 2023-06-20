#version 430

layout (location = 0) uniform mat4 modelViewProjectionMatrix;

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec2 texturePosition;

layout (location = 0) out vec2 texturePositionOut;

void main() {
    gl_Position = modelViewProjectionMatrix * vec4(vertexPosition, 1.0);
    texturePositionOut = texturePosition;
}
