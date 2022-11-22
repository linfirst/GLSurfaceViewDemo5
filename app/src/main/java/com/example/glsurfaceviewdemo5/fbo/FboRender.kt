package com.example.glsurfaceviewdemo5.fbo
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.example.glsurfaceviewdemo5.GLUtil
import com.example.glsurfaceviewdemo5.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class FboRender(private val context: Context) {
    private val fragmentBuffer: FloatBuffer
    private val vertexBuffer: FloatBuffer
    private var program = 0
    private var vPosition = 0
    private var fPosition = 0
    private val textureid = 0
    private var sampler = 0
    private var blurOffset = 0
    private var vboId = 0

    //顶点坐标
    private val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    //纹理坐标
    private val fragmentData = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    init {
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentData)
        fragmentBuffer.position(0)
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)
    }

    fun onCreate() {
        val vertexSource: String =
            GLUtil.getRawResource(context, R.raw.blur_vertex_shader)?:""
        val fragmentSource: String =
            GLUtil.getRawResource(context, R.raw.blur_fragment_shader)?:""
        program = GLUtil.createProgram(vertexSource, fragmentSource)
        //顶点坐标
        vPosition = GLES20.glGetAttribLocation(program, "verticesCoordinate")
        //纹理坐标
        fPosition = GLES20.glGetAttribLocation(program, "textureCoordinate")
        sampler = GLES20.glGetUniformLocation(program, "inputImageTexture")
        //模糊步长
        blurOffset = GLES20.glGetUniformLocation(program, "uBlurOffset")


        val vbos = IntArray(1)
        GLES20.glGenBuffers(1, vbos, 0)
        vboId = vbos[0]
        //绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //分配内存
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, vertexData.size * 4 + fragmentData.size * 4,
            null, GLES20.GL_STATIC_DRAW
        )
        //缓存到显存
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexData.size * 4, vertexBuffer)
        GLES20.glBufferSubData(
            GLES20.GL_ARRAY_BUFFER, vertexData.size * 4, fragmentData.size * 4,
            fragmentBuffer
        )
        //解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun onChange(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    fun onDraw(textureId: Int,bitmap: Bitmap) {
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //使用颜色清屏
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        //使用program
        GLES20.glUseProgram(program)
        //绑定纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        //使用顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition)
        //传0 从VBO中取值
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glUniform2f(blurOffset, 0f, (2.0f * bitmap.height/bitmap.width) / bitmap.height)
        GLES20.glEnableVertexAttribArray(fPosition)


        //VBO
        GLES20.glVertexAttribPointer(
            fPosition, 2, GLES20.GL_FLOAT, false, 8,
            vertexData.size * 4
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        //解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}