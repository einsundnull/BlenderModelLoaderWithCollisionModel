#version 300 es
precision highp float;
in vec3 v_LightDir;
in vec3 v_FragPos;
in vec3 v_Normal;
in vec4 v_Color;
in vec4 v_ShadowSpacePos;
uniform vec3 u_LightPos;
uniform sampler2D u_ShadowMap;
out vec4 fragColor;
void main() {
    vec3 lightDir = normalize(u_LightPos - v_FragPos);
    float diff = max(dot(v_Normal, lightDir), 0.0);
    vec4 color = vec4(diff, diff, diff, 1.0) * v_Color;
    vec4 shadowSpacePos = v_ShadowSpacePos;
    shadowSpacePos.z += 0.0005; // Add depth bias
    float shadow = texture(u_ShadowMap, shadowSpacePos.xy).r;
    fragColor = color * shadow;
}
