#version 150

out vec4 fragColor;

uniform sampler2D DepthSampler;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform vec2 ScreenSize;
uniform float GameTime;
uniform vec3 CameraPos;
uniform float Intensity;

// Generate simple hash noise
float hash(vec3 p)
{
    return fract(
        sin(dot(p, vec3(127.1, 311.7, 245.1))) * 43758.5453
    );
}

// Generate smoothened noise
float noise(vec3 p)
{
    vec3 i = floor(p);
    vec3 f = fract(p);

    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash(i + vec3(0,0,0));
    float n100 = hash(i + vec3(1,0,0));
    float n010 = hash(i + vec3(0,1,0));
    float n110 = hash(i + vec3(1,1,0));

    float n001 = hash(i + vec3(0,0,1));
    float n101 = hash(i + vec3(1,0,1));
    float n011 = hash(i + vec3(0,1,1));
    float n111 = hash(i + vec3(1,1,1));

    float x00 = mix(n000, n100, f.x);
    float x10 = mix(n010, n110, f.x);

    float x01 = mix(n001, n101, f.x);
    float x11 = mix(n011, n111, f.x);

    float y0 = mix(x00, x10, f.y);
    float y1 = mix(x01, x11, f.y);

    return mix(y0, y1, f.z);
}

// Convert depth buffer value into camera distance
float linearizeDepth(float depth, mat4 projection)
{

    vec4 clip = vec4(
        0.0,
        0.0,
        depth,
        1.0
    );

    vec4 view = projection * clip;

    view.xyz /= view.w;

    return -view.z;
}

void main()
{
    float t = GameTime * 20000.0;

    mat4 projection = inverse(ProjMat);

    // ---------------------------------------
    // STEP 1: Fragment position to view space
    // ---------------------------------------

    // Get normalized pixel on screen
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    // Change normalization range from [0, 1] to [-1, 1]
    uv = uv * 2.0 - 1.0;

    // Now we put that xy coordinate in clip space (x,y,z,w)
    // z is 1.0 so we are on the near plane (Minecraft convention)
    vec4 clip = vec4(uv, 0.0, 1.0);

    // Reconstruct position in view space by multiplying by the inverse
    // of the projection matrix
    // View space = 3D space with the camera as the origin.
    vec4 view = projection * clip;
    view.xyz /= view.w;

    // -------------------------------------
    // STEP 2: View space to world space ray
    // -------------------------------------

    // ModelViewMat is rotation only at this point, so inversing the matrix is
    // the same as transposing it.
    vec3 worldRay = normalize(transpose(mat3(ModelViewMat)) * view.xyz);

    vec3 cameraWorldPos = CameraPos;
    float radius = 200.0;
    float angle = 0.1 * t;

    vec3 offset = vec3(
        cos(angle) * worldRay.x + sin(angle) * worldRay.z,
        worldRay.y,
        cos(angle) * worldRay.z - sin(angle) * worldRay.x
    );

    float density = 0.0;

    float sceneDepth = texture(DepthSampler, gl_FragCoord.xy / ScreenSize).r;
    float maxDist = linearizeDepth(sceneDepth, projection);

    if (sceneDepth <= 0) {
        maxDist = 1e6;
    }

    for(int i = 0; i < 3; i++) {
        float depth = i * 10.0;

        if (depth >= maxDist) break;

        vec3 samplePos = offset * depth + cameraWorldPos;

        vec3 wind = vec3(1.0, 0.0, 0.2);

        vec3 p = samplePos + wind * 0.2 * t;

        p *= vec3(
            0.1,
            1,
            0.2
        );

        density += noise(p) * 0.3;
    }

    density = smoothstep(
        0.4,
        0.75,
        density
    );

    density *= 0.9;

    vec3 sandColor = vec3(
        0.85,
        0.68,
        0.40
    );

    fragColor = vec4(sandColor, density);
}