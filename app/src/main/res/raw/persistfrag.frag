precision mediump float;

uniform sampler2D texture;

varying vec2 texCoord;

void main() {
    gl_FragColor = vec4(0,0,0,.2) + .8*texture2D(texture, texCoord);
}
