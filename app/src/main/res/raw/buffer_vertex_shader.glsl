attribute vec4 aPosition;
attribute vec4 aTexCoord;
uniform mat4 uTexMatrix;
uniform mat4 uPosMatrix;

varying vec2 vTexCoord;

void main() {
    vTexCoord = (uTexMatrix * aTexCoord).xy;
    vTexCoord = vec2(vTexCoord.x, 1.0 - vTexCoord.y);
    gl_Position = uPosMatrix * aPosition;
}