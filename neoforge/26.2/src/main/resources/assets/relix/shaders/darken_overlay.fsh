#version 330

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);

    // Darken
    color.rgb *= 0.65;

    // Desaturate
    float lum = dot(color.rgb,
        vec3(0.2126, 0.7152, 0.0722));

    color.rgb = mix(
        color.rgb,
        vec3(lum),
        0.35
    );

    // Yellow warmth
    color.rgb *= vec3(1.0, 0.9, 0.65);

    fragColor = color;
}