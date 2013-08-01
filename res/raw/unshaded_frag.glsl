uniform sampler2D texture;

varying mediump vec2 _texCoord;

void main() {
    gl_FragColor = texture2D(texture, _texCoord);
}