precision mediump float;     //绘制精度
varying vec2 ft_Position;     //与顶点着色器中传值
uniform sampler2D sTexture;    //图片
void main() {
    gl_FragColor=texture2D(sTexture, ft_Position);
}
