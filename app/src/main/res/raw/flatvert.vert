attribute vec4 vPosition;

uniform mat4 projMatrix;
uniform mat4 mvMatrix;

void main() {
    gl_Position = projMatrix * mvMatrix * vPosition;
}
