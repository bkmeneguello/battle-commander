uniform sampler2D u_Texture;

varying mediump vec2 v_TexCoordinate;

void main() {
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
}