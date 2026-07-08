package com.example.activity_home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.activity_home.R
import com.example.activity_home.model.Team

class TeamAdapter(private val teamList: List<Team>) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    // Khai báo các thành phần giao diện trong item_team.xml
    class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTeamName: TextView = itemView.findViewById(R.id.tvTeamName)
        val imgTeamLogo: ImageView = itemView.findViewById(R.id.imgTeamLogo)
    }

    // Tạo cái khung (inflate layout)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    // Thêm dữ liệu vào khung
    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val currentTeam = teamList[position]
        holder.tvTeamName.text = currentTeam.teamName
        // Tạm thời chưa gọi API lấy ảnh
    }

    // Khai báo số lượng item trong danh sách
    override fun getItemCount(): Int {
        return teamList.size
    }
}