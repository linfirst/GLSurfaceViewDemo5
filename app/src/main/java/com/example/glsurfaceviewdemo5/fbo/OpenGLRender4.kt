package com.example.glsurfaceviewdemo5.fbo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.example.glsurfaceviewdemo5.GLUtil
import com.example.glsurfaceviewdemo5.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * https://blog.csdn.net/we1less/article/details/109907382?spm=1001.2014.3001.5502
 */
class OpenGLRender4(private val context: Context) : GLSurfaceView.Renderer {
    //顶点坐标
    private val vertexData = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

//    private val vertexData = floatArrayOf(
//        -1f, -1f,
//        1f, -1f,
//        -1f, 1f,
//        1f, 1f
//    )

    //纹理坐标
    private val fragmentData = floatArrayOf(
        0f, 0f, //top-left
        1f, 0f, //top-right
        0f, 1f, //bottom-left
        1f, 1f  //bottom-right
    )

    private val vertexBuffer: FloatBuffer
    private val fragmentBuffer: FloatBuffer
    private var program = 0
    private var vPosition = 0
    private var fPosition = 0
    private var textureid = 0
    private var sampler = 0
    private var blurOffset = 0
    private var vboId = 0
    private var fboId = 0
    private var imgTextureId = 0
    private val fboRender: FboRender

    private lateinit var bitmap:Bitmap

    init {
        /***********************使用FBO-START */
        fboRender = FboRender(context)
        /******************************FBO-END */
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
    }

    /***********************使用FBO-START */
    /**
     *返回图片数据的纹理
     *
     * */
    private fun loadTexture(src: Int): Int {
        //创建纹理
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        //设置环绕过滤方法
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        bitmap = BitmapFactory.decodeResource(context.resources, src)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        /***********************使用FBO-START */
        fboRender.onCreate()
        /******************************FBO-END */
        val vertexSource: String =
            GLUtil.getRawResource(context, R.raw.blur_vertex_shader)?:""
        val fragmentSource: String =
            GLUtil.getRawResource(context, R.raw.blur_fragment_shader)?:""
        program = GLUtil.createProgram(vertexSource, fragmentSource)
        if (program > 0) {
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
            /***********************创建FBO-START */
            val fbos = IntArray(1)
            GLES20.glGenBuffers(1, fbos, 0)
            fboId = fbos[0]
            //绑定
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
            /******************************FBO-END */

            //生成纹理
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            textureid = textureIds[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureid)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glUniform1i(sampler, 0)

            //设置环绕过滤方法
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            /***********************创建FBO-START */
            //设置FBO分配内存大小
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 900, 1600, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
            )
            //把纹理绑定到FBO
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureid, 0
            )
            //检查FBO绑定是否成功
            if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.e("godv", "fbo error")
            } else {
                Log.e("godv", "fbo success")
            }
            /******************************FBO-END */

            //解绑纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            /***********************创建FBO-START */
            //解绑
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
            imgTextureId = loadTexture(R.drawable.img_4)
            /******************************FBO-END */
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        /***********************使用FBO-START */
        fboRender.onChange(width, height)
        /******************************FBO-END */
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d("OpenGLRender4","onDrawFrame")
        drawToFrameBuffer()
    }

    fun drawToFrameBuffer(){
        /***********************使用FBO-START */
        //绑定FBO  离屏渲染
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId)
        /******************************FBO-END */
        //清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //使用颜色清屏
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        //使用program
        GLES20.glUseProgram(program)
        /***********************使用FBO-START */
        //绑定FBO纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId)
        /******************************FBO-END */
        //绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        //使用顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition)
        //传0 从VBO中取值
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        //为自定义参数赋值,设置模糊步长
        GLES20.glUniform2f(blurOffset, 2.0f / bitmap.width, 0f)
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
        /***********************使用FBO-START */
        //解绑FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        fboRender.onDraw(textureid, bitmap)
        /******************************FBO-END */
    }
    /******************************FBO-END */
}