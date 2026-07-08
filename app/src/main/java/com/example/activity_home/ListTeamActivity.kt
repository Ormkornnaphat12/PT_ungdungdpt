package com.example.activity_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.activity_home.adapter.TeamAdapter
import com.example.activity_home.model.Team

class ListTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Báo cho máy biết file Kotlin này điều khiển giao diện XML nào
        setContentView(R.layout.activity_list_team)

        // 1. Ánh xạ cái danh sách từ XML
        val rvTeams: RecyclerView = findViewById(R.id.rvTeams)

        // 2. Tạo một danh sách dữ liệu giả (fake data) gồm đủ 10 đội bóng
        val myTeamList = ArrayList<Team>()
        myTeamList.add(Team("1", "Manchester United", ""))
        myTeamList.add(Team("2", "Real Madrid", ""))
        myTeamList.add(Team("3", "Barcelona", ""))
        myTeamList.add(Team("4", "Bayern Munich", ""))
        myTeamList.add(Team("5", "Paris Saint-Germain", ""))
        myTeamList.add(Team("6", "Manchester City", ""))
        myTeamList.add(Team("7", "Liverpool", ""))
        myTeamList.add(Team("8", "Arsenal", ""))
        myTeamList.add(Team("9", "Chelsea", ""))
        myTeamList.add(Team("10", "Juventus", ""))

        // 3. Khởi tạo Adapter và ném dữ liệu vào
        val adapter = TeamAdapter(myTeamList)

        // 4. Cài đặt cách hiển thị danh sách (dạng cuộn dọc)
        rvTeams.layoutManager = LinearLayoutManager(this)

        // 5. Gắn Adapter vào RecyclerView
        rvTeams.adapter = adapter
    }
}