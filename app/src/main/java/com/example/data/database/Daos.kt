package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY points DESC, (goalsFor - goalsAgainst) DESC, name ASC")
    fun getAllTeamsFlow(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams ORDER BY points DESC, (goalsFor - goalsAgainst) DESC, name ASC")
    suspend fun getAllTeams(): List<TeamEntity>

    @Query("SELECT * FROM teams WHERE id = :teamId LIMIT 1")
    suspend fun getTeamById(teamId: Int): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Query("DELETE FROM teams")
    suspend fun deleteAllTeams()
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayersFlow(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY number ASC")
    fun getPlayersForTeamFlow(teamId: Int): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY number ASC")
    suspend fun getPlayersForTeam(teamId: Int): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getPlayerById(playerId: Int): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players")
    suspend fun deleteAllPlayers()
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY matchTimestamp ASC")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY matchTimestamp ASC")
    suspend fun getAllMatches(): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: Int): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsFlow(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE teamId = :teamId ORDER BY timestamp DESC")
    fun getPostsForTeamFlow(teamId: Int): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPostFlow(postId: Int): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE teamId = :teamId ORDER BY timestamp ASC")
    fun getCommentsForTeamFlow(teamId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)
}

