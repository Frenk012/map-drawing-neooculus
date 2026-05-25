#version 150

uniform sampler2D Sampler0;
uniform sampler2D Noise;

in vec2 texCoord0;
in vec2 noiseCoord;

out vec4 fragColor;

const vec3 light = vec3(255., 252., 245.) / 255.;
const vec3 dark  = vec3(245., 232., 198.) / 255.;

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = fract(sin(dot(i,              vec2(127.1, 311.7))) * 43758.5453);
    float b = fract(sin(dot(i + vec2(1, 0), vec2(127.1, 311.7))) * 43758.5453);
    float c = fract(sin(dot(i + vec2(0, 1), vec2(127.1, 311.7))) * 43758.5453);
    float d = fract(sin(dot(i + vec2(1, 1), vec2(127.1, 311.7))) * 43758.5453);
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0) {
        discard;
    }
    if (color == vec4(1., 0., 1., 1.)) {
        float noise = valueNoise(noiseCoord * 0.05);
        color = vec4(mix(light, dark, noise), 1.);
    }
    fragColor = vec4(color.rgb, 1.0);
}
