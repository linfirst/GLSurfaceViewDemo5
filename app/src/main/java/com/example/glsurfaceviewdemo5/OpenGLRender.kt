package com.example.glsurfaceviewdemo5


import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGLRender(private val context: Context) : GLSurfaceView.Renderer {
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
    private var vPosition = 0
    private var fPosition = 0
    private var textureid = 0
    private var sampler = 0

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
        //工具方法加载代码
        val vertexSource: String = GLUtil.getRawResource(context, R.raw.vertex_shader)?:""
        val fragmentSource: String =
            GLUtil.getRawResource(context, R.raw.fragment_shader)?:""
        //工具方法创建program
        program = GLUtil.createProgram(vertexSource, fragmentSource)
        if (program > 0) {
            //取出顶点坐标
            vPosition = GLES20.glGetAttribLocation(program, "v_Position")
            //取出纹理坐标
            fPosition = GLES20.glGetAttribLocation(program, "f_Position")
            sampler = GLES20.glGetUniformLocation(program, "sTexture")
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
        GLES20.glUniform1f(sampler, 0f)

        //设置环绕过滤方法
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        //绘制图片
        var bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap!!.recycle()
        bitmap = null
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f, 0f, 0f, 1f)

        //使程序生效
        GLES20.glUseProgram(program)

        //重新绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid)

        //使顶点属性数组有效
        GLES20.glEnableVertexAttribArray(vPosition)
        //为顶点属性赋值
        GLES20.glVertexAttribPointer(
            vPosition, 2, GLES20.GL_FLOAT, false, 8,
            vertexBuffer
        )
        //使纹理属性数组有效
        GLES20.glEnableVertexAttribArray(fPosition)
        //为顶点属性赋值
        GLES20.glVertexAttribPointer(
            fPosition, 2, GLES20.GL_FLOAT, false, 8,
            fragmentBuffer
        )
        //绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }
}



