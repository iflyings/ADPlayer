void draw_normal() {
    gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb;
    gl_FragColor.a = 1.0;
}
// 透明度 [-，-，-，透明度]
void draw_alpha() {
    gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb;
    gl_FragColor.a = ufPosition.a;
}
// 圆形 [圆心横坐标，圆心纵坐标，半径，透明度]
void draw_circle() {
    if (distance(gl_FragCoord.xy, ufPosition.xy) < ufPosition.z)
    {
        gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb;
        gl_FragColor.a = ufPosition.a;
    }
}
// 马赛克 [纹理的长度，纹理的高度，马赛克大小，透明度]
void draw_mosaic() {
    //当前点对应在纹理中的位置
    vec2 pointXY = vec2(vTexCoord.x * ufPosition.x, vTexCoord.y * ufPosition.y);
    //找到此店对应马赛克的起点
    vec2 mosaicXY = vec2(floor(pointXY.x / ufPosition.z) * ufPosition.z, floor(pointXY.y / ufPosition.z) * ufPosition.z);
    //转换坐标
    vec2 mosaicUV = vec2(mosaicXY.x / ufPosition.x, mosaicXY.y / ufPosition.y);
    gl_FragColor.rgb = texture2D(sTexture, mosaicUV).rgb;
    gl_FragColor.a = ufPosition.a;
}
// 裁剪 [起点横坐标，起点纵左边，终点横坐标，终点纵左边]
void draw_cut_off() {
    if (gl_FragCoord.x < ufPosition[0] || gl_FragCoord.x > ufPosition[2] ||
                gl_FragCoord.y < ufPosition[1] || gl_FragCoord.y > ufPosition[3]) {
        discard;
    } else {
        gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb;
    }
    gl_FragColor.a = 1.0;
}
// 横向百叶窗 [显示的起点横坐标，显示的起点纵坐标，分段长度，显示长度]
void draw_shutter() {
    if (mod(floor(gl_FragCoord.x - ufPosition[0]), floor(ufPosition[2])) > floor(ufPosition[3]) ||
        mod(floor(gl_FragCoord.y - ufPosition[1]), floor(ufPosition[2])) > floor(ufPosition[3])) {
        discard;
    } else {
        gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb;
        gl_FragColor.a = 1.0;
    }
}
// 去掉低亮度 [ 阀值，-，-，-]
void draw_high_bright() {
    vec3 color = texture2D(sTexture, vTexCoord).rgb;
    float bright = .30 * color.r + .59 * color.g + .11 * color.b;
    float threshold = 1.0 - ufPosition[0];
    if (bright <= threshold) {
        discard;
    } else {
        gl_FragColor.rgb = color;
        if (bright - threshold <= 0.1) {
            gl_FragColor.a = 10.0 * (bright - ufPosition[0]);
        } else {
            gl_FragColor.a = 1.0;
        }
    }
}
// 丢掉多余的颜色 [红色，绿色，蓝色，是否开启]
void draw_threshold() {
    vec3 color = texture2D(sTexture, vTexCoord).rgb;
    if (ufPosition[3] > 0.0) {
        if (color.r <= ufPosition[0] && color.g <= ufPosition[1] && color.b <= ufPosition[2]) {
            discard;
        }
    }
    gl_FragColor.rgb = color;
    gl_FragColor.a = 1.0;
}

void main() {
    if (uiType == 0) {
        draw_normal();
    } else if (uiType == 1) {
        discard;
    } else if (uiType == 2) {
        draw_alpha();
    } else if (uiType == 3) {
        draw_circle();
    } else if (uiType == 4) {
        draw_mosaic();
    } else if (uiType == 5) {
        draw_cut_off();
    } else if (uiType == 6) {
        draw_shutter();
    } else if (uiType == 7) {
        draw_high_bright();
    } else if (uiType == 8) {
        draw_threshold();
    }
}