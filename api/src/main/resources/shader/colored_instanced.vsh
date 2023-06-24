#version 430

layout (location = 0) uniform mat4 modelViewProjectionMatrix;

layout (location = 0) in vec3 vertexPosition;
layout (location = 1) in vec3 vertexOffset;
layout (location = 2) in vec4 colorIn;
layout (location = 3) in vec3 normalIn;
layout (location = 4) in float shouldNotLightingIn;

layout (location = 0) out vec4 color;
layout (location = 1) out vec3 normalOut;
layout (location = 2) out float shouldNotLightingOut;

mat4 translate(vec3 delta) {
    return mat4(
        vec4(1.0, 0.0, 0.0, 0.0),
        vec4(0.0, 1.0, 0.0, 0.0),
        vec4(0.0, 0.0, 1.0, 0.0),
        vec4(delta, 1.0)
    );
}

void main() {
    gl_Position = modelViewProjectionMatrix * translate(vertexOffset) * vec4(vertexPosition, 1.0);
    color = colorIn;
    normalOut = normalIn;
}
