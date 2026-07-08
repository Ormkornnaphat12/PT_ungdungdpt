package com.example.activity_home.network

import com.example.activity_home.model.Player
import com.example.activity_home.model.Team
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // 1. Danh sách các giải đấu (Competitions/Leagues)
    @GET("competitions")
    fun getCompetitions(): Call<Any>

    // 2. Bảng xếp hạng (Standings)
    @GET("standings")
    fun getStandings(
        @Query("league_id") leagueId: String,
        @Query("season") season: String
    ): Call<Any>

    // 3. Lịch thi đấu (Fixtures)
    @GET("fixtures")
    fun getFixtures(
        @Query("league_id") leagueId: String,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Call<Any>

    // 4. Kết quả trận đấu (Results/Past Fixtures)
    @GET("fixtures")
    fun getResults(
        @Query("league_id") leagueId: String,
        @Query("status") status: String = "Match Finished"
    ): Call<Any>

    // 5. Tỷ số trực tiếp (Live Scores)
    @GET("fixtures")
    fun getLiveScores(
        @Query("status") status: String = "Live"
    ): Call<Any>

    // 6. Chi tiết trận đấu (Match Details / Events)
    @GET("fixtures/events")
    fun getMatchDetails(
        @Query("fixture_id") fixtureId: String
    ): Call<Any>

    // 7. Đội hình ra sân (Lineups)
    @GET("fixtures/lineups")
    fun getLineups(
        @Query("fixture_id") fixtureId: String
    ): Call<Any>

    // 8. Thống kê trận đấu (Match Statistics)
    @GET("fixtures/statistics")
    fun getMatchStatistics(
        @Query("fixture_id") fixtureId: String
    ): Call<Any>

    // 9. Danh sách đội bóng (Teams)
    @GET("teams")
    fun getTeams(
        @Query("league_id") leagueId: String
    ): Call<List<Team>> // Đã có sẵn model Team

    // 10. Danh sách cầu thủ và chuyển nhượng (Players & Transfers)
    @GET("players")
    fun getPlayers(
        @Query("team_id") teamId: String
    ): Call<List<Player>> // Đã có sẵn model Player
}