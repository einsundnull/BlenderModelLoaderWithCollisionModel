#version 300 es
precision highp float;
layout(location = 0) in vec3 a_Position;
layout(location = 1) in vec3 a_Normal;
layout(location = 2) in vec4 a_Color;
uniform mat4 u_MVPMatrix;
uniform mat4 u_ModelMatrix;
uniform vec3 u_LightPos;
uniform mat4 u_LightSpaceMatrix;
out vec3 v_Normal;
out vec3 v_LightDir;
out vec3 v_FragPos;
out vec4 v_Color;
out vec4 v_ShadowSpacePos;
void main() {
    vec4 worldPos = u_ModelMatrix * vec4(a_Position, 1.0);
    gl_Position = u_MVPMatrix * vec4(a_Position, 1.0);
    v_Normal = mat3(u_ModelMatrix) * a_Normal;
    v_LightDir = u_LightPos - worldPos.xyz;
    v_FragPos = worldPos.xyz;
    v_Color = a_Color;
    v_ShadowSpacePos = u_LightSpaceMatrix * vec4(v_FragPos, 1.0);
}
