#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES sTexture;
uniform int uiType;
uniform vec4 ufPosition;

varying vec2 vTexCoord;
