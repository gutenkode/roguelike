precision mediump float;

uniform sampler2D texture;
uniform vec4 vColor;

varying vec2 texCoord;

void main() {
    gl_FragColor = vColor * texture2D(texture, texCoord);
}
