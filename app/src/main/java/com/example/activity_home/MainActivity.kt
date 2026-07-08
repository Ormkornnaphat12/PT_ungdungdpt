package com.example.activity_home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Nút 1: Danh sách đội bóng
        val btnListTeam = findViewById<Button>(R.id.btnListTeam)
        btnListTeam.setOnClickListener {
            val intent = Intent(this, ListTeamActivity::class.java)
            startActivity(intent)
        }

        // Nút 2: Chi tiết cầu thủ
        val btnDetailPlayer = findViewById<Button>(R.id.btnDetailPlayer)
        btnDetailPlayer.setOnClickListener {
            val intent = Intent(this, DetailPlayerActivity::class.java)
            startActivity(intent)
        }
    }
}