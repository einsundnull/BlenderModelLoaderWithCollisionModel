#version 300 es
precision highp float;
in vec4 v_Color;
out vec4 fragColor;

void main() {
    fragColor = v_Color;
}
