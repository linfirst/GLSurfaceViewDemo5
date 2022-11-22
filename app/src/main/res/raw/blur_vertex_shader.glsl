attribute vec4 verticesCoordinate; //顶点坐标
attribute vec4 textureCoordinate; //纹理坐标
varying vec2 transferValue;
void main() {
    // 把vPosition顶点经过矩阵变换后传给gl_Position
    gl_Position = verticesCoordinate;
    transferValue = textureCoordinate.xy;
}