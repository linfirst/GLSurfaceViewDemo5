package com.example.glsurfaceviewdemo5


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGLRender3(private val context: Context) : GLSurfaceView.Renderer {
    //顶点坐标  主要用来在本地开辟内存
    private val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    //生成的本地内存
    private val vertexBuffer: FloatBuffer

    //纹理坐标
    private val fragmentData = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    //生成的本地内存
    private val fragmentBuffer: FloatBuffer
    private var program = 0
    private var verticesCoordinate = 0
    private var textureCoordinate = 0
    private var textureid = 0
    private var inputImageTexture = 0
    private var blurOffset = 0

    private var bitmapWidth = 0
    private var bitmapHeight = 0
    private lateinit var bitmap: Bitmap

    init {
        //生成的本地内存
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
        vertexBuffer.position(0)
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentData)
        fragmentBuffer.position(0)
        //工具方法创建program
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //获取 Bitmap
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img)
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height
        //工具方法加载代码
        val vertexSource: String = //顶点着色器
            GLUtil.getRawResource(context, R.raw.blur_vertex_shader) ?: ""
        val fragmentSource: String = //片段着色器
            GLUtil.getRawResource(context, R.raw.blur_fragment_shader) ?: ""
        //工具方法创建program
        program = GLUtil.createProgram(vertexSource, fragmentSource)
        if (program > 0) {
            //取出顶点坐标
            verticesCoordinate = GLES20.glGetAttribLocation(program, "verticesCoordinate")
            //取出纹理坐标
            textureCoordinate = GLES20.glGetAttribLocation(program, "textureCoordinate")
            //需要输入的图片纹理
            inputImageTexture = GLES20.glGetUniformLocation(program, "inputImageTexture")
            //模糊步长
            blurOffset = GLES20.glGetUniformLocation(program, "uBlurOffset")
        }

        //生成纹理
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        textureid = textureIds[0]
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid)
        //绑定至第0位
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //将sampler绑定至第0位
        GLES20.glUniform1f(inputImageTexture, 0f)
        //设置模糊步长
//        GLES20.glUniform2f(blurOffset, 1.0f / 500.0f, 0f)

        //设置环绕过滤方法
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        //绘制图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
//        bitmap!!.recycle()
//        bitmap = null
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        createFrameBuffer(bitmap)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {


    }

    private fun draw(isOne: Boolean) {
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f, 0f, 0f, 1f)

        //使程序生效
        GLES20.glUseProgram(program)

        //重新绑定
        if (isOne) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureFrameBuffer[0])
            // 绑定自定义缓冲帧
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }

        //使顶点属性数组有效
        GLES20.glEnableVertexAttribArray(verticesCoordinate)
        //为顶点属性赋值
        GLES20.glVertexAttribPointer(
            verticesCoordinate, 2, GLES20.GL_FLOAT, false, 8,
            vertexBuffer
        )

        //使纹理属性数组有效
        GLES20.glEnableVertexAttribArray(textureCoordinate)
        //为顶点属性赋值
        GLES20.glVertexAttribPointer(
            textureCoordinate, 2, GLES20.GL_FLOAT, false, 8,
            fragmentBuffer
        )

        //为自定义参数赋值,设置模糊步长
        if (isOne) {
            GLES20.glUniform2f(blurOffset, 0f, 5.0f / bitmapHeight)
        } else {
            GLES20.glUniform2f(blurOffset, 5.0f / bitmapWidth, 0f)
        }

        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    val textureFrameBuffer = intArrayOf(0)
    val frameBuffer = intArrayOf(0)

    private fun createFrameBuffer(bitmap: Bitmap) {
        // 创建纹理，后面会把这个纹理附加到帧缓冲上
        GLES20.glGenTextures(1, textureFrameBuffer, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureFrameBuffer[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            bitmap.width,
            bitmap.height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        // 创建帧缓冲
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        // 绑定帧缓冲
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0])
        // 把纹理作为帧缓冲的附件
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
            textureFrameBuffer[0], 0
        )
        Log.d("OpenGLRender2", "initFBO error status: " + GLES20.glGetError())
        // 检查帧缓冲是否完整
        val frameBufferStatic = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (frameBufferStatic != GLES20.GL_FRAMEBUFFER_COMPLETE) Log.e(
            "OpenGLRender2",
            "initFBO failed, status: $frameBufferStatic"
        )
    }
}



