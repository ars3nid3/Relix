#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV;

out vec4 vertexColor;
out vec3 worldPosition;
out vec2 texCoord;

void main() {

    float angle = -GameTime * 50;
    mat2 rot = mat2(cos(angle), sin(angle), -sin(angle), cos(angle));

    vec3 pos = Position;
    pos.xz = rot * pos.xz;

    worldPosition = pos + vec3(0.0, 20.0, 0.0);
    vertexColor = Color;
    texCoord = UV;

    gl_Position = ProjMat * ModelViewMat * vec4(worldPosition, 1.0);
}