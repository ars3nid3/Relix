#version 150

#moj_import <fog.glsl>
#moj_import <globals.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <projection.glsl>

in vec3 Position;
in vec4 Color;
in vec3 Normal;
const float LineWidth = 2.0;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;

const float VIEW_SHRINK = 1.0 - (1.0 / 256.0);
const mat4 VIEW_SCALE = mat4(
    VIEW_SHRINK, 0.0, 0.0, 0.0,
    0.0, VIEW_SHRINK, 0.0, 0.0,
    0.0, 0.0, VIEW_SHRINK, 0.0,
    0.0, 0.0, 0.0, 1.0
);

void main() {
    float angle = -GameTime * 50;
    mat2 rot = mat2(cos(angle), sin(angle), -sin(angle), cos(angle));

    vec3 pos = Position;
    pos.xz = rot * pos.xz;

    vec3 normal = Normal;
    normal.xz = rot * normal.xz;

    vec3 worldPosition = pos + vec3(0.0, 20.0, 0.0);
    vec4 linePosStart = ProjMat * VIEW_SCALE * ModelViewMat * vec4(worldPosition, 1.0);
    vec4 linePosEnd = ProjMat * VIEW_SCALE * ModelViewMat * vec4(worldPosition + normal, 1.0);

    vec3 ndc1 = linePosStart.xyz / linePosStart.w;
    vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;

    vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * ScreenSize);
    vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * LineWidth / ScreenSize;

    if (lineOffset.x < 0.0) {
        lineOffset *= -1.0;
    }

    if (gl_VertexID % 2 == 0) {
        gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
    } else {
        gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
    }

    sphericalVertexDistance = fog_spherical_distance(worldPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(worldPosition);
    vertexColor = Color;
}
