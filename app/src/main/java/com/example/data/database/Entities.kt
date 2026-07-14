package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val code: String, // e.g. ARG, BRA
    val flagUrl: String,
    val groupName: String,
    val points: Int = 0,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: Int,
    val teamId: Int,
    val name: String,
    val number: Int,
    val position: String, // GK, DF, MF, FW
    val age: Int,
    val goals: Int = 0,
    val assists: Int = 0,
    val wikiTitle: String, // Wikipedia search keyword
    val imageUrl: String = "",
    val nationality: String = "",
    val formerTeams: String = "",
    val awards: String = ""
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeTeamScore: Int = 0,
    val awayTeamScore: Int = 0,
    val status: String, // "UPCOMING", "LIVE", "FINISHED"
    val minute: Int = 0,
    val matchTime: String, // e.g., "18:00" or date
    val matchTimestamp: Long, // timestamp for ordering
    val highlightsUrl: String = "", // video stream / highlights mock link
    val stadiumName: String = "",
    val stadiumCountry: String = "",
    val stadiumImageUrl: String = "",
    val stadiumWikiTitle: String = "",
    val isPlayoff: Boolean = false
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val role: String, // "ADMIN" or "USER"
    val fullName: String
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int,
    val teamName: String,
    val title: String,
    val content: String,
    val imageUrl: String = "",
    val authorName: String,
    val authorRole: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val savesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int = 0,
    val postId: Int = 0,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

