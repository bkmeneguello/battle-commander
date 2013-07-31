uniform mat4 u_MVPMatrix;

attribute highp vec4 a_Position;
attribute mediump vec2 a_TexCoordinate;

varying mediump vec2 v_TexCoordinate;

void main() {
    v_TexCoordinate = a_TexCoordinate;
    gl_Position = u_MVPMatrix * a_Position;
}