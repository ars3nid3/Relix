#version 150

#moj_import <fog.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <globals.glsl>

in vec4 vertexColor;
in vec3 worldPosition;
in vec2 texCoord;

out vec4 fragColor;

// Generate simple hash noise
float hash(vec2 p)
{
    return fract(
        sin(dot(p, vec2(127.1, 311.7))) * 43758.5453
    );
}

void main() {

    // Put coord in a grid of cells. Each cell is a brick.
    vec2 brickPos = texCoord * vec2(12.0, 16.0);
    // Create the stagger pattern for the bricks
    brickPos.x += floor(brickPos.y) * 0.5;

    // Position inside current cell ([0, 1])
    vec2 grid = fract(brickPos);

    float mortar = 0.05;

    vec4 color = vertexColor * mix(0.9, 1.0, hash(floor(brickPos)));


    if (grid.x < mortar || grid.y < mortar) {
        color.rgb *= 0.8;
    }

    float bandCenter = 0.45;
    float bandHalf = 0.08;
    float distToBandCenter = abs(texCoord.y - bandCenter);
    float inBand = step(distToBandCenter, bandHalf);

    vec3 blue = vec3(0.05, 0.08, 0.35);
    color.rgb = mix(color.rgb, blue, inBand * 0.9);

    float star = 0.0;
    float twinkle = 0.0;
    if (inBand > 0.0 && distToBandCenter < 0.8 * bandHalf) {
        vec2 starCell = floor(texCoord * 192.0);

        float starHash = hash(starCell);
        float speed = mix(3.0, 10.0, hash(starCell + 17.0));
        float phase = starHash * 6.2831853;
        star = step(0.98, starHash);

        twinkle = 0.5 + 0.5 * sin(200 * GameTime * speed + phase);
    }

    fragColor = apply_fog(
        color * ColorModulator,
        fog_spherical_distance(worldPosition),
        fog_cylindrical_distance(worldPosition),
        0.0,
        FogSkyEnd,
        FogSkyEnd,
        FogSkyEnd,
        FogColor
    );
    fragColor.rgb += star * twinkle;
}