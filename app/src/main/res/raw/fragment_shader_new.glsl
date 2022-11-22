precision mediump float;     //绘制精度
varying vec2 transferValue;     //与顶点着色器中传值
uniform sampler2D inputImageTexture;    //输入的图片纹理
void main() {
    gl_FragColor = texture2D(inputImageTexture, transferValue);
}
