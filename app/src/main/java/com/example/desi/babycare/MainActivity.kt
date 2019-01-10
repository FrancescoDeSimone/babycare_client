package com.example.desi.babycare

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startMqtt(v: View) {
        val edit = findViewById(R.id.ip_edit) as EditText
        val intent = Intent(this@MainActivity, BabyCare::class.java)
        intent.putExtra("ip",edit.text.toString())
        startActivity(intent)
    }
}
