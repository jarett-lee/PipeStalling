#version 330

uniform float time;

out vec4 gl_FragColor;

void main( void ) {
	vec2 iResolution = vec2(1280, 720);
	vec2 uv = (gl_FragCoord.xy / );
	
	//get the colour
	float xCol = (uv.x - (time * 10.0)) * 3.0;
	xCol = mod(xCol, 3.0);
	vec3 horColour = vec3(0.25, 0.25, 0.25);
	
	if (xCol < 1.0) {
		
		horColour.r += 1.0 - xCol;
		horColour.g += xCol;
	}
	else if (xCol < 2.0) {
		
		xCol -= 1.0;
		horColour.g += 1.0 - xCol;
		horColour.b += xCol;
	}
	else {
		
		xCol -= 2.0;
		horColour.b += 1.0 - xCol;
		horColour.r += xCol;
	}
	
	//background lines
	float backValue = 1.0;
	float aspect = iResolution.x / iResolution.y;
	if (mod(uv.y * 100.0, 1.0) > 0.75 || mod(uv.x * 100.0 * aspect, 1.0) > 0.75) {
		
		backValue = 1.0;	
	}
	
	vec3 backLines  = vec3(backValue);
	
	//main beam
	uv = (2.0 * uv) - 1.0;
	float beamWidth = abs(1.0 / (20.0 * uv.y));
	vec3 horBeam = vec3(beamWidth);
	
	gl_FragColor = vec4(((backLines * horBeam) * horColour), 1.0);
	gl_FragDepth = .5;
}