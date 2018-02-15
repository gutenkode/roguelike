precision mediump float;

varying vec2 texCoord;

uniform sampler2D tex_scene;
uniform sampler2D tex_scanlines;
uniform sampler2D tex_noise;
uniform sampler2D tex_vignette;
uniform sampler2D tex_persist;

uniform vec2 noiseOffset;// = vec2(0.0);
uniform vec2 texSize;// = vec2(256.0);
//uniform vec3 colorMult = vec3(1.0);

vec4 quilezTexture(sampler2D tex, vec2 p)
{
	// pulled from:
	// iquilezles.org/www/articles/texture/texture.htm
    p = p*texSize + 0.5;

    vec2 i = floor(p);
    vec2 f = p - i;
	//f = smoothstep(0,1,f);
    f = f*f*f*(f*(f*6.0-15.0)+10.0); // smoothstep
    p = i + f;

    p = (p - 0.5)/texSize;
    return texture2D(tex, p);
}

vec4 crtTexture(sampler2D tex, vec2 p)
{
	vec4 result = texture2D(tex, p) * .45;
	result += texture2D(tex, p+vec2(0.5/texSize.x,0.0)) * .25;
	result += texture2D(tex, p-vec2(0.5/texSize.x,0.0)) * .25;
	result += texture2D(tex, p+vec2(1.5/texSize.x,0.0)) * .15;
	result += texture2D(tex, p-vec2(1.5/texSize.x,0.0)) * .15;
	//result += texture2D(tex, p+vec2(2.5/texSize.x,0.0)) * .10;
	//result += texture2D(tex, p-vec2(2.5/texSize.x,0.0)) * .10;

	float blue = texture2D(tex, p-vec2(2.0/texSize.x,0)).b * .5;
	//float red = texture2D(tex, p+vec2(1.0/texSize.x,0)).r;// * .25;
	result.b = max(result.b, blue);
	//result.r = max(result.r, red);
	return result;
}

void main()
{
	gl_FragColor = crtTexture(tex_scene, texCoord); // blurred scene color
	gl_FragColor = max(gl_FragColor, crtTexture(tex_persist, texCoord)); // blurred scene color

    float num_scanlines = 50.0*3.0;
	vec2 scanlineCoord = texCoord * vec2(1.0,num_scanlines); // scanlines, create black lines on the screen
    gl_FragColor = max(gl_FragColor, .05* texture2D(tex_scanlines, scanlineCoord)); // add faint scanlines everywhere

	gl_FragColor += texture2D(tex_noise, texCoord*2.5+noiseOffset); // noise

	gl_FragColor *= texture2D(tex_scanlines, scanlineCoord);

	gl_FragColor *= texture2D(tex_vignette, texCoord*vec2(-1.0)); // vignette

	//gl_FragColor *= colorMult; // final global multiply, used for fading in/out
}
