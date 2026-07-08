package com.example.activity_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DetailPlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kết nối với giao diện XML em đã thiết kế từ đầu
        setContentView(R.layout.activity_detail_player)
    }
}