package com.example.glsurfaceviewdemo5

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


object GLUtil {
    // 1 .从raw文件中获取字符串
    fun getRawResource(context: Context, rawId: Int): String? {
        val inputStream: InputStream = context.getResources().openRawResource(rawId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuffer()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    //2 .根据顶点着色器字符串和片元着色器字符串创建 shader
    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        return if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compile = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0)
            if (compile[0] != GLES20.GL_TRUE) {
                GLES20.glDeleteShader(shader)
                shader = 0
            }
            shader
        } else {
            0
        }
    }

    //3 .根据shader创建program
    fun createProgram(vertexSource: String, fragmentSoruce: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSoruce)
        if (vertexShader != 0 && fragmentShader != 0) {
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            return program
        }
        return 0
    }

    //顶点着色器
    val vertexShaderCode =
        "attribute vec4 inputTextureCoordinate;" +
                " varying vec2 textureCoordinate;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // 把vPosition顶点经过矩阵变换后传给gl_Position
                "  gl_Position = vPosition;" +
                "textureCoordinate = inputTextureCoordinate.xy;" +
                "}"

    //片段着色器
    val fragmentShaderCode  = """
        precision highp float;
        
        varying vec2 textureCoordinate;
        uniform sampler2D inputImageTexture;
        
        // 模糊半径
//        uniform int uBlurRadius = 25;
        const int uBlurRadius = 25;
        // 模糊步长
        uniform vec2 uBlurOffset;
//        const float vec2 uBlurOffset = 1.0f / 500.0f;
//        const float vec2 uBlurOffset = vec2(1.0f / 500.0f,1.0f / 500.0f);
//        const float vec2 uBlurOffset = vec2(1.0f / 500.0f,1.0f / 500.0f);
//        const float uBlurOffset[2] = float[2](1.0f / 500.0f,1.0f / 500.0f);

        // 总权重
        const float uSumWeight = 0.9690602;
        // PI
        const float PI = 3.1415926;

        // 边界值处理
        vec2 clampCoordinate(vec2 coordinate) {
            return vec2(clamp(coordinate.x, 0.0, 1.0), clamp(coordinate.y, 0.0, 1.0));
        }

        // 计算权重
        float getWeight(int i) {
            float sigma = float(25) *0.4f + 0.6f;
            return (1.0 / sqrt(2.0 * PI * sigma * sigma)) * exp(-float(i * i) / (2.0 * sigma * sigma)) / uSumWeight;
        }

        void main(){
            vec4 sourceColor = texture2D(inputImageTexture, textureCoordinate);

            if (25 <= 1) {
                gl_FragColor = sourceColor;
                return;
            }

            float weight = getWeight(0);

            vec3 finalColor = sourceColor.rgb * weight;

            for (int i = 1; i < uBlurRadius; i++) {
                weight = getWeight(i);
                finalColor += texture2D(inputImageTexture, clampCoordinate(textureCoordinate - uBlurOffset * float(i))).rgb * weight;
                finalColor += texture2D(inputImageTexture, clampCoordinate(textureCoordinate + uBlurOffset * float(i))).rgb * weight;
            }

            gl_FragColor = vec4(finalColor, sourceColor.a);
        }
    """

}