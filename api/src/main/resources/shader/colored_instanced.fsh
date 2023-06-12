#version 430

layout (location = 10) uniform sampler2D lightSampler;
layout (location = 11) uniform vec2 lightPosition;

layout (location = 0) in vec4 color;
layout (location = 1) in vec3 normal;

out vec4 fragColor;

const vec3 light0 = normalize(vec3(0.2, 1.0, -0.7));
const vec3 light1 = normalize(vec3(-0.2, 1.0, 0.7));
const vec3 lightColor = vec3(0.6, 0.6, 0.6);

void main() {
    vec3 ambientColor = texture(lightSampler, lightPosition).rgb * 0.8 - 0.2;
    vec3 light0DiffuseColor = max(dot(normal, light0), 0.0) * lightColor;
    vec3 light1DiffuseColor = max(dot(normal, light1), 0.0) * lightColor;
    vec3 diffuseColor = (light0DiffuseColor + light1DiffuseColor) * 0.5;

    float factor = step(0.5, max(color.x, max(color.y, color.z)));

    fragColor = vec4(color.rgb * mix(ambientColor + diffuseColor, vec3(1.0, 1.0, 1.0), factor), color.a);
}
