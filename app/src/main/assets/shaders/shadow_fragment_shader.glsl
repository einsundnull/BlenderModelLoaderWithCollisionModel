#version 300 es
precision highp float;
in vec4 v_ShadowSpacePos;
uniform sampler2D u_ShadowMap;
out float fragColor;
void main() {
    vec4 shadowSpacePos = v_ShadowSpacePos;
    shadowSpacePos.z += 0.05; // Add depth bias
    float shadow = texture(u_ShadowMap, shadowSpacePos.xy).r;
    fragColor = shadow;
}
