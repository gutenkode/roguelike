attribute vec4 vPosition;
attribute vec2 tPosition;

uniform mat4 projMatrix;
uniform mat4 mvMatrix;

varying vec2 texCoord;

// contains information about the sprite being drawn:
// number of tiles horizontally,
// number of tiles vertically,
// sprite index to draw
uniform vec3 spriteInfo;

vec2 getSpriteCoords(vec3 info) {
	float posX = info.z;
	float posY = floor(info.z / info.x);
	float width = 1.0/info.x;
	float height = 1.0/info.y;

	vec2 tex = tPosition;
	tex *= vec2(width,height);
	tex += vec2(width*posX,height*posY);
	return tex;
}

void main()
{
    gl_Position = projMatrix * mvMatrix * vPosition;
    texCoord = getSpriteCoords(spriteInfo);
}
