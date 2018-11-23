precision highp float;

uniform sampler2D sTexture;
uniform int uiType;
uniform vec4 ufPosition;

varying vec2 vTexCoord;

//  HDR 效果 [ k值[0.5 – 2.0]，-，-，-]
void draw_hdr() {
    vec4 texColor = texture2D(sTexture, vTexCoord);
    float lum = .30 * texColor.r + .59 * texColor.g + .11 * texColor.b;
    float b = (4. * ufPosition[0] - 1.);
    float a = 1. - b;
    float f = lum * (a * lum + b);
    gl_FragColor =  f * texColor;
}
//  黑白化 [ -，-，-，-]
void draw_gray_photo() {
    vec4 texColor = texture2D(sTexture, vTexCoord);
    float color = .30 * texColor.r + .59 * texColor.g + .11 * texColor.b;
    gl_FragColor =  vec4(color, color, color, texColor.a);
}
// 老照片 [ -，-，-，-]
void draw_old_photo() {
    vec4 texColor = texture2D(sTexture, vTexCoord);
    gl_FragColor.r = 0.393*texColor.r+0.769*texColor.g+0.189*texColor.b;
    gl_FragColor.g = 0.349*texColor.r+0.686*texColor.g+0.168*texColor.b;
    gl_FragColor.b = 0.272*texColor.r+0.534*texColor.g+0.131*texColor.b;
    gl_FragColor.a = texColor.a;
}
// 浮雕效果 [ 纹理的长度，纹理的高度，-，-]
void draw_emboss() {
    vec4 curColor = texture2D(sTexture, vTexCoord);
    vec4 upLeftColor = texture2D(sTexture, vec2((vTexCoord.x * ufPosition[0] - 1.0) / ufPosition[0], (vTexCoord.y * ufPosition[1] -1.0) / ufPosition[1]));
    vec4 delColor = curColor - upLeftColor;
    float luminance = dot(delColor.rgb, vec3(0.3, 0.59, 0.11));
    gl_FragColor = vec4(vec3(luminance), 0.0) + vec4(0.5, 0.5, 0.5, 1.0);
}
// 抖动 [ 颜色偏移距离，-，-，-]
void draw_shake() {
    vec4 blue = texture2D(sTexture, vTexCoord);
    vec4 green = texture2D(sTexture, vec2(vTexCoord.x + ufPosition[0], vTexCoord.y + ufPosition[0]));
    vec4 red = texture2D(sTexture, vec2(vTexCoord.x - ufPosition[0], vTexCoord.y - ufPosition[0]));
    gl_FragColor = vec4(red.x, green.y, blue.z, blue.w);
}
//随机函数
float nrand(in float x,in float y) {
    return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);
}
// 毛刺 [ 阀值，最大偏移距离，颜色偏移距离，-]
void draw_burrs() {
    float u = vTexCoord.x;
    float v = vTexCoord.y;
    float jitter = nrand(v, 0.0) * 2.0 - 1.0;//这里得到一个-1到1的数
    float offsetParam = step(1.0 - ufPosition[0], abs(jitter));//step是gl自带函数，意思是:如果第一个参数大于第二个参数，那么返回0，否则返回1
    jitter = jitter * offsetParam * ufPosition[1];//offsetParam如果是0，就不便宜了，如果是1，就偏移 jitter * ufPosition[1] 的距离
    //这里计算最终的像素值，纹理坐标是0到1之间的数，如果小于0，那么图像就到屏幕右边去，如果超过1，那么就到屏幕左边去。
    vec4 color1 = texture2D(sTexture, vec2(u + jitter, v));
    vec4 color2 = texture2D(sTexture, vec2(u + jitter + ufPosition[2] ,v));
    gl_FragColor = vec4(color1.r, color1.g, color2.b, 1.0);
}

void main() {
    if (uiType == 0) {          gl_FragColor = vec4(texture2D(sTexture, vTexCoord).rgb, 1.0);
    } else if (uiType == 1) {   draw_hdr();
    } else if (uiType == 2) {   draw_gray_photo();
    } else if (uiType == 3) {   draw_old_photo();
    } else if (uiType == 4) {   draw_emboss();
    } else if (uiType == 5) {   draw_shake();
    } else if (uiType == 6) {   draw_burrs();
    } else {                    discard;
    }
}