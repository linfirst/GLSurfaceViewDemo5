attribute vec4 v_Position;     //顶点坐标
attribute vec4 f_Position;      //纹理坐标
varying vec2 ft_Position;       //
void main() {
    gl_Position = v_Position;
    ft_Position = f_Position.xy;
}