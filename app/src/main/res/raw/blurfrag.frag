precision mediump float;

uniform sampler2D texture;
uniform vec2 texSize;// = vec2(256.0);

varying vec2 texCoord;

void main() {
    float fadeCoef = .25;
    vec2 halfPixelStep = vec2(0.5/texSize);
    gl_FragColor = texture2D(texture, texCoord+halfPixelStep)*.25*fadeCoef;
    gl_FragColor += texture2D(texture, texCoord+halfPixelStep*vec2(-1.0,1.0))*.25*fadeCoef;
    gl_FragColor += texture2D(texture, texCoord+halfPixelStep*vec2(1.0,-1.0))*.25*fadeCoef;
    gl_FragColor += texture2D(texture, texCoord+halfPixelStep*vec2(-1.0,-1.0))*.25*fadeCoef;
}
