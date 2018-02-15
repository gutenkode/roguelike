attribute vec4 vPosition;
attribute vec2 tPosition;

uniform mat4 projMatrix;
uniform mat4 mvMatrix;

varying vec2 texCoord;

void main() {
    gl_Position = projMatrix * mvMatrix * vPosition;
    texCoord = tPosition;
}
