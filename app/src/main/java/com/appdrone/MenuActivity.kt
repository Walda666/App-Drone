package com.appdrone

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val button = findViewById<Button>(R.id.button)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)

        val vue1Activity : Intent = Intent(this, Vue1::class.java)
        val vue2Activity : Intent = Intent(this, Vue2::class.java)
        val vue3Activity : Intent = Intent(this, Vue3::class.java)

        button.setOnClickListener {
            startActivity(vue1Activity)
        }

        button2.setOnClickListener {
            startActivity(vue2Activity)
        }

        button3.setOnClickListener {
            startActivity(vue3Activity)
        }
    }
}