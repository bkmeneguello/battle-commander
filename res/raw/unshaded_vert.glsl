uniform mat4 modelViewProjection;

attribute vec4 vertex;
attribute vec2 texCoord;

varying vec2 _texCoord;

void main() {
    _texCoord = texCoord;
    gl_Position = modelViewProjection * vertex;
}