package com.example.data.repository

import com.example.data.api.WikipediaResponse
import com.example.data.api.WikipediaService
import com.example.data.database.MatchDao
import com.example.data.database.MatchEntity
import com.example.data.database.PlayerDao
import com.example.data.database.PlayerEntity
import com.example.data.database.TeamDao
import com.example.data.database.TeamEntity
import com.example.data.database.UserDao
import com.example.data.database.UserEntity
import com.example.data.database.PostDao
import com.example.data.database.PostEntity
import com.example.data.database.CommentDao
import com.example.data.database.CommentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FootballRepository(
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao,
    private val matchDao: MatchDao,
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val wikiService: WikipediaService
) {
    val allTeamsFlow: Flow<List<TeamEntity>> = teamDao.getAllTeamsFlow()
    val allMatchesFlow: Flow<List<MatchEntity>> = matchDao.getAllMatchesFlow()
    
    // Auth & Users
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun registerUser(user: UserEntity): Long {
        return userDao.insertUser(user)
    }

    suspend fun getAdminCount(): Int {
        return userDao.getAdminCount()
    }

    // Community Posts
    val allPostsFlow: Flow<List<PostEntity>> = postDao.getAllPostsFlow()

    fun getPostsForTeamFlow(teamId: Int): Flow<List<PostEntity>> {
        return postDao.getPostsForTeamFlow(teamId)
    }

    suspend fun insertPost(post: PostEntity): Long {
        return postDao.insertPost(post)
    }

    suspend fun updatePost(post: PostEntity) {
        postDao.updatePost(post)
    }

    suspend fun deletePost(postId: Int) {
        postDao.deletePost(postId)
    }

    // Comments
    fun getCommentsForPostFlow(postId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForPostFlow(postId)
    }

    fun getCommentsForTeamFlow(teamId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForTeamFlow(teamId)
    }

    suspend fun insertComment(comment: CommentEntity): Long {
        return commentDao.insertComment(comment)
    }

    suspend fun deleteComment(commentId: Int) {
        commentDao.deleteComment(commentId)
    }

    fun getPlayersForTeamFlow(teamId: Int): Flow<List<PlayerEntity>> {
        return playerDao.getPlayersForTeamFlow(teamId)
    }

    suspend fun getPlayersForTeam(teamId: Int): List<PlayerEntity> {
        return playerDao.getPlayersForTeam(teamId)
    }

    suspend fun getPlayerById(playerId: Int): PlayerEntity? {
        return playerDao.getPlayerById(playerId)
    }

    suspend fun getTeamById(teamId: Int): TeamEntity? {
        return teamDao.getTeamById(teamId)
    }

    suspend fun getMatchById(matchId: Int): MatchEntity? {
        return matchDao.getMatchById(matchId)
    }

    suspend fun updateMatch(match: MatchEntity) {
        matchDao.updateMatch(match)
        // Whenever a match is updated (e.g. score changes), we can re-compute the group standings!
        recalculateStandings()
    }

    suspend fun fetchWikipediaSummary(wikiTitle: String): WikipediaResponse? {
        return try {
            wikiService.getPlayerSummary(wikiTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun recalculateStandings() {
        val allMatches = matchDao.getAllMatches()
        val allTeams = teamDao.getAllTeams()

        val teamStats = allTeams.map { team ->
            var played = 0
            var won = 0
            var drawn = 0
            var lost = 0
            var goalsFor = 0
            var goalsAgainst = 0

            allMatches.filter { it.status == "FINISHED" }.forEach { match ->
                if (match.homeTeamId == team.id) {
                    played++
                    goalsFor += match.homeTeamScore
                    goalsAgainst += match.awayTeamScore
                    when {
                        match.homeTeamScore > match.awayTeamScore -> won++
                        match.homeTeamScore == match.awayTeamScore -> drawn++
                        else -> lost++
                    }
                } else if (match.awayTeamId == team.id) {
                    played++
                    goalsFor += match.awayTeamScore
                    goalsAgainst += match.homeTeamScore
                    when {
                        match.awayTeamScore > match.homeTeamScore -> won++
                        match.awayTeamScore == match.homeTeamScore -> drawn++
                        else -> lost++
                    }
                }
            }

            val points = (won * 3) + (drawn * 1)
            team.copy(
                played = played,
                won = won,
                drawn = drawn,
                lost = lost,
                goalsFor = goalsFor,
                goalsAgainst = goalsAgainst,
                points = points
            )
        }

        teamDao.insertTeams(teamStats)
    }

    suspend fun seedIfNeeded() {
        val teams = teamDao.getAllTeams()
        if (teams.isEmpty()) {
            val seededTeams = listOf(
                TeamEntity(1, "Argentina", "ARG", "https://flagcdn.com/w160/ar.png", "Group A"),
                TeamEntity(2, "Brazil", "BRA", "https://flagcdn.com/w160/br.png", "Group A"),
                TeamEntity(3, "France", "FRA", "https://flagcdn.com/w160/fr.png", "Group B"),
                TeamEntity(4, "Germany", "GER", "https://flagcdn.com/w160/de.png", "Group B"),
                TeamEntity(5, "Spain", "ESP", "https://flagcdn.com/w160/es.png", "Group C"),
                TeamEntity(6, "England", "ENG", "https://flagcdn.com/w160/gb.png", "Group C"),
                TeamEntity(7, "Portugal", "POR", "https://flagcdn.com/w160/pt.png", "Group D"),
                TeamEntity(8, "Netherlands", "NED", "https://flagcdn.com/w160/nl.png", "Group D"),
                TeamEntity(9, "Croatia", "CRO", "https://flagcdn.com/w160/hr.png", "Group E"),
                TeamEntity(10, "Japan", "JPN", "https://flagcdn.com/w160/jp.png", "Group E")
            )
            teamDao.insertTeams(seededTeams)

            val seededPlayers = listOf(
                // Argentina
                PlayerEntity(101, 1, "Lionel Messi", 10, "FW", 39, 102, 56, "Lionel_Messi", "https://upload.wikimedia.org/wikipedia/commons/b/b4/Lionel-Messi-Argentina-2022-FIFA-World-Cup_%28cropped%29.jpg", "Argentina", "Barcelona (Tây Ban Nha), PSG (Pháp), Inter Miami (Mỹ)", "Quả Bóng Vàng FIFA (8 lần), Chiếc Giày Vàng Châu Âu (6 lần), Quả Bóng Vàng World Cup 2014 & 2022, Vô địch World Cup 2022"),
                PlayerEntity(102, 1, "Ángel Di María", 11, "FW", 38, 30, 28, "Ángel_Di_María", "https://upload.wikimedia.org/wikipedia/commons/e/e0/Di_Maria_2018.jpg", "Argentina", "Rosario Central (Argentina), Benfica (Bồ Đào Nha), Real Madrid (Tây Ban Nha), Manchester United (Anh), PSG (Pháp), Juventus (Ý)", "Vô địch World Cup 2022, Vô địch Copa América 2021 & 2024, Huy chương Vàng Olympic 2008, Cầu thủ xuất sắc nhất trận chung kết Champions League 2014"),
                PlayerEntity(103, 1, "Enzo Fernández", 24, "MF", 25, 4, 6, "Enzo_Fernández", "", "Argentina", "River Plate (Argentina), Defensa y Justicia (Argentina), Benfica (Bồ Đào Nha), Chelsea (Anh)", "Cầu thủ trẻ xuất sắc nhất World Cup 2022, Vô địch World Cup 2022, Vô địch Copa América 2024"),
                PlayerEntity(104, 1, "Cristian Romero", 13, "DF", 28, 3, 1, "Cristian_Romero", "", "Argentina", "Belgrano (Argentina), Genoa (Ý), Juventus (Ý), Atalanta (Ý), Tottenham Hotspur (Anh)", "Vô địch World Cup 2022, Vô địch Copa América 2021 & 2024, Hậu vệ xuất sắc nhất Serie A 2020-21"),
                PlayerEntity(105, 1, "Emiliano Martínez", 23, "GK", 33, 0, 0, "Emiliano_Martínez", "", "Argentina", "Independiente (Argentina), Arsenal (Anh), Sheffield Wednesday (Anh), Reading (Anh), Aston Villa (Anh)", "Găng Tay Vàng World Cup 2022, Thủ môn xuất sắc nhất thế giới FIFA (The Best) 2022, Vô địch World Cup 2022, Cúp Yashin 2023 & 2024"),

                // Brazil
                PlayerEntity(201, 2, "Neymar Jr", 10, "FW", 34, 79, 58, "Neymar", "https://upload.wikimedia.org/wikipedia/commons/b/bc/Bra-Cos_%281%29_%28cropped%29.jpg", "Brazil", "Santos (Brazil), Barcelona (Tây Ban Nha), PSG (Pháp), Al Hilal (Ả Rập Xê Út)", "Quả Bóng Đồng FIFA (2 lần), Vô địch Champions League 2014-15, Huy chương Vàng Olympic 2016, Vô địch Confederations Cup 2013"),
                PlayerEntity(202, 2, "Vinícius Júnior", 7, "FW", 25, 5, 8, "Vinícius_Júnior", "", "Brazil", "Flamengo (Brazil), Real Madrid (Tây Ban Nha)", "Cầu thủ xuất sắc nhất Champions League 2023-24, Vô địch Champions League 2021-22 & 2023-24, Quả Bóng Đồng Ballon d'Or 2024"),
                PlayerEntity(203, 2, "Casemiro", 5, "MF", 34, 7, 3, "Casemiro", "", "Brazil", "São Paulo (Brazil), Real Madrid (Tây Ban Nha), Porto (Bồ Đào Nha), Manchester United (Anh)", "Vô địch Champions League (5 lần), Vô địch Copa América 2019, Đội hình tiêu biểu FIFA FIFPro (2 lần)"),
                PlayerEntity(204, 2, "Marquinhos", 4, "DF", 32, 7, 1, "Marquinhos", "", "Brazil", "Corinthians (Brazil), AS Roma (Ý), PSG (Pháp)", "Vô địch Copa América 2019, Huy chương Vàng Olympic 2016, Đội hình tiêu biểu Ligue 1 (nhiều lần)"),
                PlayerEntity(205, 2, "Alisson Becker", 1, "GK", 33, 0, 0, "Alisson_Becker", "", "Brazil", "Internacional (Brazil), AS Roma (Ý), Liverpool (Anh)", "Găng Tay Vàng Premier League & Copa América, Thủ môn xuất sắc nhất thế giới FIFA 2019, Vô địch Champions League 2018-19, Vô địch Copa América 2019"),

                // France
                PlayerEntity(301, 3, "Kylian Mbappé", 10, "FW", 27, 48, 32, "Kylian_Mbappé", "https://upload.wikimedia.org/wikipedia/commons/5/57/Kylian_Mbapp%C3%A9_2022.jpg", "Pháp", "Monaco (Pháp), PSG (Pháp), Real Madrid (Tây Ban Nha)", "Cúp Kopa (Cầu thủ trẻ xuất sắc nhất) 2018, Chiếc Giày Vàng World Cup 2022, Cầu thủ trẻ xuất sắc nhất World Cup 2018, Vô địch World Cup 2018"),
                PlayerEntity(302, 3, "Antoine Griezmann", 7, "FW", 35, 44, 38, "Antoine_Griezmann", "", "Pháp", "Real Sociedad (Tây Ban Nha), Atletico Madrid (Tây Ban Nha), Barcelona (Tây Ban Nha)", "Quả Bóng Đồng World Cup 2018, Cầu thủ xuất sắc nhất Europa League 2017-18, Vô địch World Cup 2018, Vua phá lưới Euro 2016"),
                PlayerEntity(303, 3, "Aurélien Tchouaméni", 8, "MF", 26, 3, 2, "Aurélien_Tchouaméni", "", "Pháp", "Bordeaux (Pháp), Monaco (Pháp), Real Madrid (Tây Ban Nha)", "Vô địch Nations League 2020-21, Á quân World Cup 2022, Vô địch Champions League 2023-24"),
                PlayerEntity(304, 3, "William Saliba", 17, "DF", 25, 0, 0, "William_Saliba", "", "Pháp", "Saint-Étienne (Pháp), Nice (Pháp), Marseille (Pháp), Arsenal (Anh)", "Đội hình xuất sắc nhất Premier League (2 lần), Cầu thủ trẻ xuất sắc nhất năm của Ligue 1 (2021-22)"),
                PlayerEntity(305, 3, "Mike Maignan", 16, "GK", 30, 0, 0, "Mike_Maignan", "", "Pháp", "PSG (Pháp), Lille (Pháp), AC Milan (Ý)", "Thủ môn xuất sắc nhất Serie A 2021-22, Vô địch Serie A 2021-22, Vô địch Ligue 1 2020-21"),

                // Germany
                PlayerEntity(401, 4, "Jamal Musiala", 10, "MF", 23, 6, 8, "Jamal_Musiala", "https://upload.wikimedia.org/wikipedia/commons/f/fc/Germany_vs._Japan_2023_-_Jamal_Musiala.jpg", "Đức", "Southampton (Anh), Chelsea (Anh), Bayern Munich (Đức)", "Cầu thủ xuất sắc nhất năm của Đội tuyển Đức 2022, Vua phá lưới Euro 2024 (đồng hạng), Đội hình tiêu biểu Bundesliga"),
                PlayerEntity(402, 4, "Florian Wirtz", 17, "MF", 23, 4, 7, "Florian_Wirtz", "", "Đức", "1. FC Köln (Đức), Bayer Leverkusen (Đức)", "Cầu thủ xuất sắc nhất Bundesliga 2023-24, Vô địch Bundesliga 2023-24, Đội hình tiêu biểu Europa League"),
                PlayerEntity(403, 4, "Kai Havertz", 7, "FW", 27, 18, 10, "Kai_Havertz", "", "Đức", "Bayer Leverkusen (Đức), Chelsea (Anh), Arsenal (Anh)", "Ghi bàn thắng quyết định chung kết Champions League 2021, Vô địch Champions League 2020-21"),
                PlayerEntity(404, 4, "Antonio Rüdiger", 2, "DF", 33, 3, 1, "Antonio_Rüdiger", "", "Đức", "Stuttgart (Đức), AS Roma (Ý), Chelsea (Anh), Real Madrid (Tây Ban Nha)", "Vô địch Champions League 2020-21 & 2023-24, Đội hình tiêu biểu Champions League, Vô địch Europa League 2018-19"),
                PlayerEntity(405, 4, "Manuel Neuer", 1, "GK", 40, 0, 0, "Manuel_Neuer", "", "Đức", "Schalke 04 (Đức), Bayern Munich (Đức)", "Thủ môn xuất sắc nhất thế kỷ (IFFHS 2011-2020), Găng Tay Vàng World Cup 2014, Vô địch World Cup 2014, Vô địch Champions League (2 lần)"),

                // Spain
                PlayerEntity(501, 5, "Lamine Yamal", 19, "FW", 18, 3, 6, "Lamine_Yamal", "https://upload.wikimedia.org/wikipedia/commons/e/e0/Lamine_Yamal_Euro_2024.jpg", "Tây Ban Nha", "Barcelona (Tây Ban Nha)", "Cầu thủ trẻ xuất sắc nhất Euro 2024, Vô địch Euro 2024, Cúp Kopa (Cầu thủ trẻ xuất sắc nhất thế giới) 2024"),
                PlayerEntity(502, 5, "Rodri", 16, "MF", 30, 4, 3, "Rodri_%28footballer%2C_born_1996%29", "", "Tây Ban Nha", "Villarreal (Tây Ban Nha), Atletico Madrid (Tây Ban Nha), Manchester City (Anh)", "Quả Bóng Vàng Ballon d'Or 2024, Cầu thủ xuất sắc nhất Euro 2024, Cầu thủ xuất sắc nhất Champions League 2022-23, Vô địch Champions League 2022-23"),
                PlayerEntity(503, 5, "Pedri", 20, "MF", 23, 2, 9, "Pedri", "", "Tây Ban Nha", "Las Palmas (Tây Ban Nha), Barcelona (Tây Ban Nha)", "Cúp Kopa 2021, Golden Boy 2021, Cầu thủ trẻ xuất sắc nhất Euro 2020"),
                PlayerEntity(504, 5, "Aymeric Laporte", 14, "DF", 32, 1, 0, "Aymeric_Laporte", "", "Tây Ban Nha", "Athletic Bilbao (Tây Ban Nha), Manchester City (Anh), Al Nassr (Ả Rập Xê Út)", "Vô địch Euro 2024, Vô địch Premier League (5 lần), Vô địch Champions League 2022-23"),
                PlayerEntity(505, 5, "Unai Simón", 23, "GK", 29, 0, 0, "Unai_Sim%C3%B3n", "", "Tây Ban Nha", "Athletic Bilbao (Tây Ban Nha)", "Vô địch Euro 2024, Vô địch Nations League 2022-23, Găng tay vàng Zamora La Liga 2023-24"),

                // England
                PlayerEntity(601, 6, "Harry Kane", 9, "FW", 32, 64, 19, "Harry_Kane", "https://upload.wikimedia.org/wikipedia/commons/a/af/Harry_Kane_Euro_2024.jpg", "Anh", "Tottenham Hotspur (Anh), Leyton Orient (Anh), Millwall (Anh), Leicester City (Anh), Bayern Munich (Đức)", "Chiếc Giày Vàng World Cup 2018, Chiếc Giày Vàng Châu Âu 2023-24, Vua phá lưới Premier League (3 lần), Vua phá lưới Bundesliga 2023-24"),
                PlayerEntity(602, 6, "Jude Bellingham", 10, "MF", 23, 6, 5, "Jude_Bellingham", "", "Anh", "Birmingham City (Anh), Borussia Dortmund (Đức), Real Madrid (Tây Ban Nha)", "Cúp Kopa 2023, Golden Boy 2023, Cầu thủ xuất sắc nhất La Liga 2023-24, Cầu thủ trẻ xuất sắc nhất Champions League 2023-24"),
                PlayerEntity(603, 6, "Bukayo Saka", 7, "FW", 24, 12, 8, "Bukayo_Saka", "", "Anh", "Arsenal (Anh)", "Cầu thủ xuất sắc nhất năm của Đội tuyển Anh (2021-22 & 2022-23), Cầu thủ trẻ xuất sắc nhất năm của PFA 2022-23"),
                PlayerEntity(604, 6, "John Stones", 5, "DF", 32, 3, 1, "John_Stones", "", "Anh", "Barnsley (Anh), Everton (Anh), Manchester City (Anh)", "Vô địch Champions League 2022-23, Vô địch Premier League (6 lần), Đội hình tiêu biểu FIFA FIFPro 2023"),
                PlayerEntity(605, 6, "Jordan Pickford", 1, "GK", 32, 0, 0, "Jordan_Pickford", "", "Anh", "Sunderland (Anh), Preston North End (Anh), Everton (Anh)", "Thủ môn xuất sắc nhất năm của Everton (3 lần), Á quân Euro 2020 & 2024"),

                // Portugal
                PlayerEntity(701, 7, "Cristiano Ronaldo", 7, "FW", 41, 130, 48, "Cristiano_Ronaldo", "https://upload.wikimedia.org/wikipedia/commons/8/8c/Cristiano_Ronaldo_2018.jpg", "Bồ Đào Nha", "Sporting CP (Bồ Đào Nha), Manchester United (Anh), Real Madrid (Tây Ban Nha), Juventus (Ý), Al Nassr (Ả Rập Xê Út)", "Quả Bóng Vàng Ballon d'Or (5 lần), Cầu thủ xuất sắc nhất thế giới FIFA (5 lần), Chiếc Giày Vàng Châu Âu (4 lần), Vô địch Euro 2016, Vô địch Champions League (5 lần)"),
                PlayerEntity(702, 7, "Bruno Fernandes", 8, "MF", 31, 22, 21, "Bruno_Fernandes", "", "Bồ Đào Nha", "Novara (Ý), Udinese (Ý), Sampdoria (Ý), Sporting CP (Bồ Đào Nha), Manchester United (Anh)", "Cầu thủ xuất sắc nhất năm của Sporting CP, Đội hình tiêu biểu Europa League (nhiều lần), Vô địch FA Cup & League Cup"),
                PlayerEntity(703, 7, "Bernardo Silva", 10, "MF", 31, 12, 15, "Bernardo_Silva", "", "Bồ Đào Nha", "Benfica (Bồ Đào Nha), Monaco (Pháp), Manchester City (Anh)", "Vô địch Nations League 2018-19, Cầu thủ xuất sắc nhất VCK Nations League 2019, Vô địch Champions League 2022-23, Vô địch Premier League (6 lần)"),
                PlayerEntity(704, 7, "Rúben Dias", 4, "DF", 29, 3, 1, "R%C3%BAben_Dias", "", "Bồ Đào Nha", "Benfica (Bồ Đào Nha), Manchester City (Anh)", "Cầu thủ xuất sắc nhất Premier League 2020-21, Hậu vệ xuất sắc nhất Champions League 2020-21, Vô địch Champions League 2022-23"),
                PlayerEntity(705, 7, "Diogo Costa", 22, "GK", 26, 0, 0, "Diogo_Costa", "", "Bồ Đào Nha", "Porto (Bồ Đào Nha)", "Vô địch Primeira Liga (2 lần), Thủ môn xuất sắc nhất tháng của Bồ Đào Nha (nhiều lần)"),

                // Netherlands
                PlayerEntity(801, 8, "Cody Gakpo", 11, "FW", 27, 12, 5, "Cody_Gakpo", "https://upload.wikimedia.org/wikipedia/commons/e/ec/Cody_Gakpo_2022.jpg", "Hà Lan", "PSV Eindhoven (Hà Lan), Liverpool (Anh)", "Vua phá lưới Euro 2024 (đồng hạng), Cầu thủ xuất sắc nhất năm của bóng đá Hà Lan 2021-22"),
                PlayerEntity(802, 8, "Frenkie de Jong", 21, "MF", 29, 2, 6, "Frenkie_de_Jong", "", "Hà Lan", "Willem II (Hà Lan), Ajax (Hà Lan), Barcelona (Tây Ban Nha)", "Cầu thủ xuất sắc nhất mùa giải Eredivisie 2018-19, Tiền vệ xuất sắc nhất UEFA 2018-19, Vô địch La Liga 2022-23"),
                PlayerEntity(803, 8, "Virgil van Dijk", 4, "DF", 34, 9, 3, "Virgil_van_Dijk", "", "Hà Lan", "Groningen (Hà Lan), Celtic (Scotland), Southampton (Anh), Liverpool (Anh)", "Cầu thủ xuất sắc nhất năm của UEFA 2018-19, Á quân Quả Bóng Vàng 2019, Vô địch Champions League 2018-19, Đội hình tiêu biểu FIFA FIFPro"),
                PlayerEntity(804, 8, "Nathan Aké", 5, "DF", 31, 5, 2, "Nathan_Ak%C3%A9", "", "Hà Lan", "Chelsea (Anh), Watford (Anh), Bournemouth (Anh), Manchester City (Anh)", "Vô địch Champions League 2022-23, Vô địch Premier League (4 lần), Cầu thủ trẻ xuất sắc nhất năm của Chelsea (2 lần)"),
                PlayerEntity(805, 8, "Bart Verbruggen", 1, "GK", 23, 0, 0, "Bart_Verbruggen", "", "Hà Lan", "NAC Breda (Hà Lan), Anderlecht (Bỉ), Brighton (Anh)", "Cầu thủ xuất sắc nhất năm của Anderlecht 2022-23, Thủ môn chính thức tuyển Hà Lan tại Euro 2024"),

                // Croatia
                PlayerEntity(901, 9, "Luka Modrić", 10, "MF", 40, 24, 29, "Luka_Modri%C4%87", "https://upload.wikimedia.org/wikipedia/commons/e/e9/Luka_Modri%C4%87_2021.jpg", "Croatia", "Dinamo Zagreb (Croatia), Tottenham Hotspur (Anh), Real Madrid (Tây Ban Nha)", "Quả Bóng Vàng Ballon d'Or 2018, Cầu thủ xuất sắc nhất năm của FIFA 2018, Quả Bóng Vàng World Cup 2018, Quả Bóng Đồng World Cup 2022, Vô địch Champions League (6 lần)"),
                PlayerEntity(902, 9, "Mateo Kovačić", 8, "MF", 32, 5, 8, "Mateo_Kova%C4%8D_and_Mateo_Kova%C4%8D-Vuj%C4%8Di%C4%87", "", "Croatia", "Dinamo Zagreb (Croatia), Inter Milan (Ý), Real Madrid (Tây Ban Nha), Chelsea (Anh), Manchester City (Anh)", "Vô địch Champions League (4 lần), Vô địch Europa League 2018-19, Cầu thủ xuất sắc nhất năm của Chelsea 2019-20"),
                PlayerEntity(903, 9, "Andrej Kramarić", 9, "FW", 35, 28, 12, "Andrej_Kramari%C4%87", "", "Croatia", "Dinamo Zagreb (Croatia), Rijeka (Croatia), Leicester City (Anh), Hoffenheim (Đức)", "Vua phá lưới giải VĐQG Croatia 2014-15, Cầu thủ ghi nhiều bàn thắng nhất lịch sử Hoffenheim"),
                PlayerEntity(904, 9, "Joško Gvardiol", 4, "DF", 24, 2, 1, "Jo%C5%A1ko_Gvardiol", "", "Croatia", "Dinamo Zagreb (Croatia), RB Leipzig (Đức), Manchester City (Anh)", "Đội hình tiêu biểu World Cup 2022, Đội hình tiêu biểu Bundesliga 2022-23, Vô địch Premier League 2023-24"),
                PlayerEntity(905, 9, "Dominik Livaković", 1, "GK", 31, 0, 0, "Dominik_Livakovi%C4%87", "", "Croatia", "NK Zagreb (Croatia), Dinamo Zagreb (Croatia), Fenerbahçe (Thổ Nhĩ Kỳ)", "Đứng thứ 7 bình chọn Thủ môn xuất sắc nhất Ballon d'Or (Yashin Trophy) 2023, Vô địch giải VĐQG Croatia (6 lần)"),

                // Japan
                PlayerEntity(1001, 10, "Kaoru Mitoma", 7, "FW", 29, 7, 5, "Kaoru_Mitoma", "https://upload.wikimedia.org/wikipedia/commons/7/77/Kaoru_Mitoma_2022.jpg", "Nhật Bản", "Kawasaki Frontale (Nhật Bản), Union SG (Bỉ), Brighton (Anh)", "Đội hình tiêu biểu J.League 2020, Cầu thủ xuất sắc nhất năm của bóng đá Nhật Bản 2023"),
                PlayerEntity(1002, 10, "Takefusa Kubo", 20, "FW", 25, 4, 9, "Takefusa_Kubo", "", "Nhật Bản", "FC Tokyo (Nhật Bản), Yokohama F. Marinos (Nhật Bản), Real Madrid (Tây Ban Nha), Mallorca (Tây Ban Nha), Villarreal (Tây Ban Nha), Getafe (Tây Ban Nha), Real Sociedad (Tây Ban Nha)", "Cầu thủ xuất sắc nhất mùa giải của Real Sociedad 2022-23, Cầu thủ xuất sắc nhất trận đấu của La Liga (nhiều lần)"),
                PlayerEntity(1003, 10, "Wataru Endo", 6, "MF", 33, 3, 4, "Wataru_Endo", "", "Nhật Bản", "Shonan Bellmare (Nhật Bản), Urawa Red Diamonds (Nhật Bản), Sint-Truiden (Bỉ), VfB Stuttgart (Đức), Liverpool (Anh)", "Cúp Quốc gia Đức 2019-20, Cúp Liên đoàn Anh 2023-24, Cầu thủ xuất sắc nhất năm của Nhật Bản"),
                PlayerEntity(1004, 10, "Takehiro Tomiyasu", 22, "DF", 27, 1, 1, "Takehiro_Tomiyasu", "", "Nhật Bản", "Avispa Fukuoka (Nhật Bản), Sint-Truiden (Bỉ), Bologna (Ý), Arsenal (Anh)", "Cầu thủ trẻ xuất sắc nhất châu Á (IFFHS) 2020, Vô địch FA Community Shield 2023"),
                PlayerEntity(1005, 10, "Zion Suzuki", 1, "GK", 23, 0, 0, "Zion_Suzuki", "", "Nhật Bản", "Urawa Red Diamonds (Nhật Bản), Sint-Truidense (Bỉ), Parma (Ý)", "Thủ môn trẻ xuất sắc nhất J.League 2021, Thủ môn chính thức tuyển Nhật Bản tại Asian Cup 2023"),

                // Argentina extra
                PlayerEntity(106, 1, "Rodrigo De Paul", 7, "MF", 32, 2, 10, "Rodrigo_De_Paul", "", "Argentina", "Racing Club (Argentina), Valencia (Tây Ban Nha), Udinese (Ý), Atlético Madrid (Tây Ban Nha)", "Vô địch World Cup 2022, Vô địch Copa América 2021 & 2024"),
                PlayerEntity(107, 1, "Lautaro Martínez", 22, "FW", 28, 29, 8, "Lautaro_Mart%C3%ADnez", "", "Argentina", "Racing Club (Argentina), Inter Milan (Ý)", "Vô địch World Cup 2022, Vô địch Copa América 2021 & 2024, Vua phá lưới Copa América 2024"),
                PlayerEntity(108, 1, "Alexis Mac Allister", 20, "MF", 27, 3, 5, "Alexis_Mac_Allister", "", "Argentina", "Argentinos Juniors (Argentina), Boca Juniors (Argentina), Brighton (Anh), Liverpool (Anh)", "Vô địch World Cup 2022, Vô địch Copa América 2024, Vô địch Carabao Cup 2023-24"),

                // Brazil extra
                PlayerEntity(206, 2, "Rodrygo Goes", 10, "FW", 25, 6, 4, "Rodrygo", "", "Brazil", "Santos (Brazil), Real Madrid (Tây Ban Nha)", "Vô địch Champions League 2021-22 & 2023-24, Vô địch La Liga (3 lần)"),
                PlayerEntity(207, 2, "Raphinha", 11, "FW", 29, 7, 6, "Raphinha", "", "Brazil", "Avaí (Brazil), Sporting CP (Bồ Đào Nha), Rennes (Pháp), Leeds United (Anh), Barcelona (Tây Ban Nha)", "Vô địch La Liga 2022-23"),
                PlayerEntity(208, 2, "Lucas Paquetá", 8, "MF", 28, 10, 8, "Lucas_Paquet%C3%A1", "", "Brazil", "Flamengo (Brazil), AC Milan (Ý), Lyon (Pháp), West Ham United (Anh)", "Vô địch Conference League 2022-23, Vô địch Copa América 2019"),

                // France extra
                PlayerEntity(306, 3, "Ousmane Dembélé", 11, "FW", 29, 6, 12, "Ousmane_Demb%C3%A9l%C3%A9", "", "Pháp", "Rennes (Pháp), Borussia Dortmund (Đức), Barcelona (Tây Ban Nha), PSG (Pháp)", "Vô địch World Cup 2018, Vô địch La Liga (3 lần)"),
                PlayerEntity(307, 3, "Theo Hernández", 22, "DF", 28, 2, 8, "Theo_Hernandez", "", "Pháp", "Atlético Madrid (Tây Ban Nha), Alavés (Tây Ban Nha), Real Madrid (Tây Ban Nha), Real Sociedad (Tây Ban Nha), AC Milan (Ý)", "Vô địch Nations League 2020-21, Á quân World Cup 2022"),
                PlayerEntity(308, 3, "Eduardo Camavinga", 6, "MF", 23, 1, 2, "Eduardo_Camavinga", "", "Pháp", "Rennes (Pháp), Real Madrid (Tây Ban Nha)", "Vô địch Champions League 2021-22 & 2023-24, Á quân World Cup 2022"),

                // Germany extra
                PlayerEntity(406, 4, "Leroy Sané", 19, "FW", 30, 13, 9, "Leroy_San%C3%A9", "", "Đức", "Schalke 04 (Đức), Manchester City (Anh), Bayern Munich (Đức)", "Vô địch Premier League (2 lần), Vô địch Bundesliga (3 lần)"),
                PlayerEntity(407, 4, "İlkay Gündoğan", 21, "MF", 35, 19, 15, "S%C3%A9bastien_Haller", "", "Đức", "Nürnberg (Đức), Borussia Dortmund (Đức), Manchester City (Anh), Barcelona (Tây Ban Nha)", "Vô địch Champions League 2022-23, Vô địch Premier League (5 lần)"),
                PlayerEntity(408, 4, "Joshua Kimmich", 6, "MF", 31, 6, 20, "Joshua_Kimmich", "", "Đức", "RB Leipzig (Đức), Stuttgart (Đức), Bayern Munich (Đức)", "Vô địch Champions League 2019-20, Vô địch Bundesliga (8 lần)"),

                // Spain extra
                PlayerEntity(506, 5, "Nico Williams", 17, "FW", 23, 4, 7, "Nico_Williams", "", "Tây Ban Nha", "Athletic Bilbao (Tây Ban Nha)", "Vô địch Euro 2024, Cầu thủ xuất sắc nhất trận chung kết Euro 2024"),
                PlayerEntity(507, 5, "Dani Olmo", 10, "MF", 28, 11, 9, "Dani_Olmo", "", "Tây Ban Nha", "Dinamo Zagreb (Croatia), RB Leipzig (Đức), Barcelona (Tây Ban Nha)", "Vô địch Euro 2024, Vua phá lưới Euro 2024"),
                PlayerEntity(508, 5, "Álvaro Morata", 7, "FW", 33, 36, 12, "%C3%81lvaro_Morata", "", "Tây Ban Nha", "Real Madrid (Tây Ban Nha), Juventus (Ý), Chelsea (Anh), Atlético Madrid (Tây Ban Nha), AC Milan (Ý)", "Vô địch Euro 2024, Vô địch Champions League (2 lần)"),

                // England extra
                PlayerEntity(606, 6, "Phil Foden", 11, "MF", 26, 4, 8, "Phil_Foden", "", "Anh", "Manchester City (Anh)", "Cầu thủ xuất sắc nhất năm FWA 2023-24, Vô địch Champions League 2022-23, Vô địch Premier League (6 lần)"),
                PlayerEntity(607, 6, "Declan Rice", 4, "MF", 27, 4, 3, "Declan_Rice", "", "Anh", "West Ham United (Anh), Arsenal (Anh)", "Vô địch Conference League 2022-23, Á quân Euro 2020 & 2024"),
                PlayerEntity(608, 6, "Cole Palmer", 24, "MF", 24, 2, 4, "Cole_Palmer", "", "Anh", "Manchester City (Anh), Chelsea (Anh)", "Cầu thủ trẻ xuất sắc nhất năm PFA 2023-24, Á quân Euro 2024"),

                // Portugal extra
                PlayerEntity(706, 7, "Rafael Leão", 17, "FW", 27, 4, 6, "Rafael_Le%C3%A3o", "", "Bồ Đào Nha", "Sporting CP (Bồ Đào Nha), Lille (Pháp), AC Milan (Ý)", "Cầu thủ xuất sắc nhất Serie A 2021-22, Vô địch Serie A 2021-22"),
                PlayerEntity(707, 7, "Diogo Jota", 21, "FW", 29, 14, 8, "Diogo_Jota", "", "Bồ Đào Nha", "Paços de Ferreira (Bồ Đào Nha), Atlético Madrid (Tây Ban Nha), Porto (Bồ Đào Nha), Wolves (Anh), Liverpool (Anh)", "Vô địch FA Cup 2021-22, Vô địch EFL Cup (2 lần)"),
                PlayerEntity(708, 7, "João Félix", 11, "FW", 26, 8, 4, "Jo%C3%A3o_F%C3%A9lix", "", "Bồ Đào Nha", "Benfica (Bồ Đào Nha), Atlético Madrid (Tây Ban Nha), Chelsea (Anh), Barcelona (Tây Ban Nha)", "Golden Boy 2019, Vô địch La Liga 2020-21"),

                // Netherlands extra
                PlayerEntity(806, 8, "Memphis Depay", 10, "FW", 32, 46, 30, "Memphis_Depay", "", "Hà Lan", "PSV Eindhoven (Hà Lan), Manchester United (Anh), Lyon (Pháp), Barcelona (Tây Ban Nha), Atlético Madrid (Tây Ban Nha)", "Vua phá lưới Eredivisie 2014-15"),
                PlayerEntity(807, 8, "Denzel Dumfries", 22, "DF", 30, 6, 15, "Denzel_Dumfries", "", "Hà Lan", "Sparta Rotterdam (Hà Lan), Heerenveen (Hà Lan), PSV Eindhoven (Hà Lan), Inter Milan (Ý)", "Vô địch Serie A 2023-24, Đội hình xuất sắc nhất Euro 2020"),
                PlayerEntity(808, 8, "Jeremie Frimpong", 12, "DF", 25, 1, 3, "Jeremie_Frimpong", "", "Hà Lan", "Manchester City (Anh), Celtic (Scotland), Bayer Leverkusen (Đức)", "Vô địch Bundesliga 2023-24, Đội hình tiêu biểu Bundesliga (2 lần)"),

                // Croatia extra
                PlayerEntity(906, 9, "Ivan Perišić", 14, "MF", 37, 33, 30, "Ivan_Peri%C5%A1i%C4%87", "", "Croatia", "Hajduk Split (Croatia), Club Brugge (Bỉ), Borussia Dortmund (Đức), Wolfsburg (Đức), Bayern Munich (Đức), Inter Milan (Ý), Tottenham Hotspur (Anh)", "Á quân World Cup 2018, Hạng ba World Cup 2022, Vô địch Champions League 2019-20"),
                PlayerEntity(907, 9, "Marcelo Brozović", 11, "MF", 33, 7, 7, "Marcelo_Brozovi%C4%87", "", "Croatia", "Hrvatski Dragovoljac (Croatia), Lokomotiva (Croatia), Dinamo Zagreb (Croatia), Inter Milan (Ý), Al Nassr (Ả Rập Xê Út)", "Á quân World Cup 2018, Hạng ba World Cup 2022, Á quân Champions League 2022-23"),
                PlayerEntity(908, 9, "Mario Pašalić", 15, "MF", 31, 10, 6, "Mario_Pa%C5%A1ali%C4%87", "", "Croatia", "Hajduk Split (Croatia), Chelsea (Anh), Elche (Tây Ban Nha), Monaco (Pháp), Milan (Ý), Spartak Moscow (Nga), Atalanta (Ý)", "Vô địch Europa League 2023-24, Hạng ba World Cup 2022"),

                // Japan extra
                PlayerEntity(1006, 10, "Ritsu Doan", 8, "FW", 28, 10, 6, "Ritsu_D%C5%8Dan", "", "Nhật Bản", "Gamba Osaka (Nhật Bản), Groningen (Hà Lan), Arminia Bielefeld (Đức), PSV Eindhoven (Hà Lan), SC Freiburg (Đức)", "Vô địch KNVB Cup 2021-22"),
                PlayerEntity(1007, 10, "Daichi Kamada", 15, "MF", 29, 7, 8, "Daichi_Kamada", "", "Nhật Bản", "Sagan Tosu (Nhật Bản), Eintracht Frankfurt (Đức), Sint-Truiden (Bỉ), Lazio (Ý), Crystal Palace (Anh)", "Vô địch Europa League 2021-22"),
                PlayerEntity(1008, 10, "Takumi Minamino", 10, "MF", 31, 21, 11, "Takumi_Minamino", "", "Nhật Bản", "Cerezo Osaka (Nhật Bản), Red Bull Salzburg (Áo), Liverpool (Anh), Southampton (Anh), Monaco (Pháp)", "Vô địch Premier League 2019-20, Vô địch EFL Cup 2021-22")
            )
            playerDao.insertPlayers(seededPlayers)

            val now = System.currentTimeMillis()
            val seededMatches = listOf(
                MatchEntity(
                    id = 1,
                    homeTeamId = 1, // Argentina
                    awayTeamId = 3, // France
                    homeTeamScore = 3,
                    awayTeamScore = 3,
                    status = "FINISHED",
                    minute = 120,
                    matchTime = "Yesterday",
                    matchTimestamp = now - 86400000,
                    highlightsUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    stadiumName = "MetLife Stadium",
                    stadiumCountry = "Mỹ",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/e/ed/MetLife_Stadium_interior.JPG",
                    stadiumWikiTitle = "MetLife_Stadium",
                    isPlayoff = true
                ),
                MatchEntity(
                    id = 2,
                    homeTeamId = 2, // Brazil
                    awayTeamId = 4, // Germany
                    homeTeamScore = 2,
                    awayTeamScore = 1,
                    status = "FINISHED",
                    minute = 90,
                    matchTime = "Yesterday",
                    matchTimestamp = now - 70000000,
                    highlightsUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    stadiumName = "Estadio Azteca",
                    stadiumCountry = "Mexico",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/af/Estadio_Azteca_vista_aerea.jpg",
                    stadiumWikiTitle = "Estadio_Azteca",
                    isPlayoff = true
                ),
                MatchEntity(
                    id = 3,
                    homeTeamId = 6, // England
                    awayTeamId = 7, // Portugal
                    homeTeamScore = 1,
                    awayTeamScore = 1,
                    status = "LIVE",
                    minute = 65,
                    matchTime = "LIVE",
                    matchTimestamp = now,
                    highlightsUrl = "",
                    stadiumName = "SoFi Stadium",
                    stadiumCountry = "Mỹ",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/df/Sofi_Stadium_%2851897282717%29_%28cropped%29.jpg",
                    stadiumWikiTitle = "SoFi_Stadium",
                    isPlayoff = true
                ),
                MatchEntity(
                    id = 4,
                    homeTeamId = 5, // Spain
                    awayTeamId = 9, // Croatia
                    homeTeamScore = 0,
                    awayTeamScore = 0,
                    status = "LIVE",
                    minute = 32,
                    matchTime = "LIVE",
                    matchTimestamp = now + 10000,
                    highlightsUrl = "",
                    stadiumName = "BC Place",
                    stadiumCountry = "Canada",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/0/00/BC_Place_Stadium_Interior_2.jpg",
                    stadiumWikiTitle = "BC_Place",
                    isPlayoff = false
                ),
                MatchEntity(
                    id = 5,
                    homeTeamId = 8, // Netherlands
                    awayTeamId = 10, // Japan
                    homeTeamScore = 0,
                    awayTeamScore = 0,
                    status = "UPCOMING",
                    minute = 0,
                    matchTime = "Tonight 20:00",
                    matchTimestamp = now + 40000000,
                    highlightsUrl = "",
                    stadiumName = "Mercedes-Benz Stadium",
                    stadiumCountry = "Mỹ",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/5/52/Mercedes-Benz_Stadium_under_construction_April_2017.jpg",
                    stadiumWikiTitle = "Mercedes-Benz_Stadium",
                    isPlayoff = false
                ),
                MatchEntity(
                    id = 6,
                    homeTeamId = 2, // Brazil
                    awayTeamId = 1, // Argentina
                    homeTeamScore = 0,
                    awayTeamScore = 0,
                    status = "UPCOMING",
                    minute = 0,
                    matchTime = "Tomorrow 19:30",
                    matchTimestamp = now + 86400000,
                    highlightsUrl = "",
                    stadiumName = "Hard Rock Stadium",
                    stadiumCountry = "Mỹ",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/d/da/Inside_Hard_Rock_Stadium.jpg",
                    stadiumWikiTitle = "Hard_Rock_Stadium",
                    isPlayoff = false
                ),
                MatchEntity(
                    id = 7,
                    homeTeamId = 3, // France
                    awayTeamId = 7, // Portugal
                    homeTeamScore = 0,
                    awayTeamScore = 0,
                    status = "UPCOMING",
                    minute = 0,
                    matchTime = "July 8, 20:45",
                    matchTimestamp = now + 172800000,
                    highlightsUrl = "",
                    stadiumName = "Lumen Field",
                    stadiumCountry = "Mỹ",
                    stadiumImageUrl = "https://upload.wikimedia.org/wikipedia/commons/3/36/CenturyLink_Field_2017.jpg",
                    stadiumWikiTitle = "Lumen_Field",
                    isPlayoff = false
                )
            )
            matchDao.insertMatches(seededMatches)
            recalculateStandings()

            // Seed a few mock posts
            val seededPosts = listOf(
                PostEntity(
                    id = 1,
                    teamId = 1,
                    teamName = "Argentina",
                    title = "Nhận định trận đấu Argentina vs France",
                    content = "Đội tuyển Argentina đang có phong độ cực cao với đầu tàu Lionel Messi. Trận đấu này hứa hẹn sẽ mang đến nhiều cảm xúc cho người hâm mộ bóng đá toàn cầu.",
                    imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop",
                    authorName = "Huy Hoàng",
                    authorRole = "ADMIN",
                    timestamp = now - 3600000
                ),
                PostEntity(
                    id = 2,
                    teamId = 7,
                    teamName = "Bồ Đào Nha",
                    title = "Cristiano Ronaldo chuẩn bị lập kỷ lục mới!",
                    content = "Huyền thoại CR7 vẫn đang không ngừng ghi bàn ở độ tuổi 41. Người hâm mộ Bồ Đào Nha tin rằng anh sẽ tiếp tục dẫn dắt đội tuyển đến những vinh quang mới tại giải đấu năm nay.",
                    imageUrl = "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop",
                    authorName = "Minh Anh",
                    authorRole = "USER",
                    timestamp = now - 7200000
                )
            )
            for (p in seededPosts) {
                postDao.insertPost(p)
            }

            // Seed some comments
            commentDao.insertComment(CommentEntity(1, teamId = 1, postId = 1, authorName = "Quốc Anh", authorRole = "USER", content = "Argentina vô địch! Tiến lên Messi!", timestamp = now - 1800000))
            commentDao.insertComment(CommentEntity(2, teamId = 1, postId = 1, authorName = "Tùng Dương", authorRole = "ADMIN", content = "Trận đấu kinh điển lặp lại chung kết World Cup 2022.", timestamp = now - 900000))
        }
    }
}
