#version 430

layout (location = 10) uniform vec4 mostColor;
layout (location = 11) uniform vec4 leastColor;
layout (location = 12) uniform float progression;
layout (location = 13) uniform sampler2D lightSampler;
layout (location = 14) uniform vec2 lightPosition;
layout (location = 15) uniform float shouldNotLighting;
layout (location = 16) uniform mat4 inverseModelMatrix;

layout (location = 0) in vec2 texturePosition;
layout (location = 1) in vec3 normal;

out vec4 fragColor;

const vec3 light0 = normalize(inverseModelMatrix * vec4(0.2, 1.0, -0.7, 0.0)).xyz;
const vec3 light1 = normalize(inverseModelMatrix * vec4(-0.2, 1.0, 0.7, 0.0)).xyz;
const vec3 lightColor = vec3(0.6, 0.6, 0.6);

void main() {
    vec3 ambientColor = texture(lightSampler, lightPosition).rgb * 0.8 - 0.2;
    vec3 light0DiffuseColor = max(dot(normal, light0), 0.0) * lightColor;
    vec3 light1DiffuseColor = max(dot(normal, light1), 0.0) * lightColor;
    vec3 diffuseColor = (light0DiffuseColor + light1DiffuseColor) * 0.5;
    vec3 lightColor = mix(ambientColor + diffuseColor, vec3(1.0, 1.0, 1.0), step(0.5, shouldNotLighting));

    vec4 color = mix(leastColor, mostColor, step(progression, texturePosition.x));

    fragColor = vec4(color.rgb * lightColor, color.a);
}
