package com.example.glsurfaceviewdemo5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class GLTest1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gltest1)
        Log.d("GLTest1Activity","${System.currentTimeMillis() - MainActivity.startTime}")
    }
}