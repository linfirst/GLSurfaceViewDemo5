package com.example.glsurfaceviewdemo5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    companion object{
        var startTime = 0L
        var endTime = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.bt1).apply {
            setOnClickListener {
                val intent = Intent(
                    this@MainActivity,
                    GLTest1Activity::class.java
                )
                startTime = System.currentTimeMillis()
                startActivity(intent)
            }
        }
    }
}