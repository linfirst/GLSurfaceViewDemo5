package com.example.glsurfaceviewdemo5

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import com.example.glsurfaceviewdemo5.fbo.OpenGLRender4


class GLSurfaceView1(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    private val RENDER_TYPE = 4

    init {
        setEGLContextClientVersion(2)
        //设置背景色 via：https://blog.csdn.net/weixin_35857552/article/details/117508667
        //PS:边界有小黑边，这里先设置成透明 后面优化处理
        //https://blog.csdn.net/lkl22/article/details/88776952
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        var renderer: Renderer? = null
        when (RENDER_TYPE) {
            1 -> {
                renderer = OpenGLRender(context!!)
            }
            2 -> {
                renderer = OpenGLRender2(context!!)
            }
            3 -> {
                renderer = OpenGLRender3(context!!)
            }
            4 -> {
                renderer = OpenGLRender4(context!!)
            }
            5 -> {
                renderer = OpenGLRender5(context!!)
            }
        }

        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        looperRender()
    }

    private fun looperRender(){
        postDelayed({
            Log.d("OpenGLRender4","postDelayed requestRender")
            looperRender()
            requestRender()
        }, 16)
    }
}
