package com.example.viewmodel

import android.app.Application
import android.database.Cursor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.WikipediaResponse
import com.example.data.database.FootballDatabase
import com.example.data.database.MatchEntity
import com.example.data.database.PlayerEntity
import com.example.data.database.TeamEntity
import com.example.data.database.UserEntity
import com.example.data.database.PostEntity
import com.example.data.database.CommentEntity
import com.example.data.repository.FootballRepository
import com.example.service.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class FootballViewModel(
    application: Application,
    private val repository: FootballRepository
) : AndroidViewModel(application) {

    private val notificationHelper = NotificationHelper(application)

    // Flow from Room for automatic reactive updates
    val teamsState: StateFlow<List<TeamEntity>> = repository.allTeamsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchesState: StateFlow<List<MatchEntity>> = repository.allMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auth & Community States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    val postsState: StateFlow<List<PostEntity>> = repository.allPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected state
    private val _selectedTeam = MutableStateFlow<TeamEntity?>(null)
    val selectedTeam = _selectedTeam.asStateFlow()

    private val _teamPlayers = MutableStateFlow<List<PlayerEntity>>(emptyList())
    val teamPlayers = _teamPlayers.asStateFlow()

    private val _playerPositionFilter = MutableStateFlow("ALL") // "ALL", "GK", "DF", "MF", "FW"
    val playerPositionFilter = _playerPositionFilter.asStateFlow()

    private val _selectedPlayer = MutableStateFlow<PlayerEntity?>(null)
    val selectedPlayer = _selectedPlayer.asStateFlow()

    private val _wikiSummaryState = MutableStateFlow<WikipediaResponse?>(null)
    val wikiSummaryState = _wikiSummaryState.asStateFlow()

    private val _isWikiLoading = MutableStateFlow(false)
    val isWikiLoading = _isWikiLoading.asStateFlow()

    private val _selectedStadiumMatch = MutableStateFlow<MatchEntity?>(null)
    val selectedStadiumMatch = _selectedStadiumMatch.asStateFlow()

    private val _stadiumWikiSummary = MutableStateFlow<WikipediaResponse?>(null)
    val stadiumWikiSummary = _stadiumWikiSummary.asStateFlow()

    private val _isStadiumWikiLoading = MutableStateFlow(false)
    val isStadiumWikiLoading = _isStadiumWikiLoading.asStateFlow()

    // Light/Dark Theme Preference (saved in-memory for this session, can toggle easily)
    private val _isDarkTheme = MutableStateFlow(true) // Default to modern Dark Slate Theme
    val isDarkTheme = _isDarkTheme.asStateFlow()

    // Interactive Highlight Video Player state
    private val _playingVideoUrl = MutableStateFlow<String?>(null)
    val playingVideoUrl = _playingVideoUrl.asStateFlow()

    // SQL Console States
    private val _sqlResult = MutableStateFlow<SqlResult?>(null)
    val sqlResult = _sqlResult.asStateFlow()

    // Simulation Events State Flow
    private val _simulationEvents = MutableStateFlow<List<String>>(
        listOf(
            "⚽ Chào mừng bạn đến với World Cup Live! Trình giả lập diễn biến trực tuyến đã sẵn sàng.",
            "📢 Chờ đón những bàn thắng, thẻ phạt và tình huống nóng bỏng từ các trận đấu đang trực tiếp!"
        )
    )
    val simulationEvents = _simulationEvents.asStateFlow()

    fun addSimulationEvent(event: String) {
        val current = _simulationEvents.value.toMutableList()
        current.add(0, event) // Add at top
        if (current.size > 30) {
            current.removeAt(current.size - 1)
        }
        _simulationEvents.value = current
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedIfNeeded()
            startLiveScoreSimulation()
        }
    }

    fun selectTeam(team: TeamEntity) {
        _selectedTeam.value = team
        viewModelScope.launch(Dispatchers.IO) {
            repository.getPlayersForTeamFlow(team.id).collect { players ->
                _teamPlayers.value = players
            }
        }
    }

    fun selectPlayer(player: PlayerEntity) {
        _selectedPlayer.value = player
        _wikiSummaryState.value = null
        fetchWikiSummary(player.wikiTitle)
    }

    fun dismissPlayerDetails() {
        _selectedPlayer.value = null
        _wikiSummaryState.value = null
    }

    fun selectStadiumMatch(match: MatchEntity) {
        _selectedStadiumMatch.value = match
        _stadiumWikiSummary.value = null
        if (match.stadiumWikiTitle.isNotEmpty()) {
            fetchStadiumWikiSummary(match.stadiumWikiTitle)
        }
    }

    fun dismissStadiumDetails() {
        _selectedStadiumMatch.value = null
        _stadiumWikiSummary.value = null
    }

    private fun fetchStadiumWikiSummary(title: String) {
        viewModelScope.launch {
            _isStadiumWikiLoading.value = true
            val summary = withContext(Dispatchers.IO) {
                repository.fetchWikipediaSummary(title)
            }
            _stadiumWikiSummary.value = summary
            _isStadiumWikiLoading.value = false
        }
    }

    fun executeSql(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty()) {
                _sqlResult.value = SqlResult(
                    query = query,
                    message = "Lỗi: Truy vấn không được để trống.",
                    isError = true
                )
                return@launch
            }

            try {
                val db = FootballDatabase.getDatabase(getApplication()).openHelper.writableDatabase
                
                // Check if it's a select or meta query
                val isSelect = trimmedQuery.startsWith("select", ignoreCase = true) ||
                               trimmedQuery.startsWith("pragma", ignoreCase = true) ||
                               trimmedQuery.startsWith("explain", ignoreCase = true) ||
                               trimmedQuery.startsWith("show", ignoreCase = true)

                if (isSelect) {
                    val cursor = db.query(trimmedQuery, emptyArray<Any>())
                    val columns = cursor.columnNames.toList()
                    val rows = mutableListOf<List<String>>()

                    while (cursor.moveToNext()) {
                        val row = mutableListOf<String>()
                        for (i in 0 until cursor.columnCount) {
                            val value = try {
                                when (cursor.getType(i)) {
                                    0 -> "NULL" // Cursor.FIELD_TYPE_NULL
                                    1 -> cursor.getLong(i).toString() // Cursor.FIELD_TYPE_INTEGER
                                    2 -> cursor.getDouble(i).toString() // Cursor.FIELD_TYPE_FLOAT
                                    3 -> cursor.getString(i) ?: "NULL" // Cursor.FIELD_TYPE_STRING
                                    4 -> "[BLOB]" // Cursor.FIELD_TYPE_BLOB
                                    else -> cursor.getString(i) ?: "NULL"
                                }
                            } catch (e: Exception) {
                                "ERROR"
                            }
                            row.add(value)
                        }
                        rows.add(row)
                    }
                    cursor.close()

                    _sqlResult.value = SqlResult(
                        query = query,
                        columnNames = columns,
                        rows = rows,
                        message = "Thực thi thành công: Tìm thấy ${rows.size} hàng."
                    )
                } else {
                    db.execSQL(trimmedQuery)
                    _sqlResult.value = SqlResult(
                        query = query,
                        message = "Thực thi lệnh SQL thành công (Không trả về kết quả bảng)."
                    )
                }
            } catch (e: Exception) {
                _sqlResult.value = SqlResult(
                    query = query,
                    message = e.localizedMessage ?: "Lỗi thực thi SQL.",
                    isError = true
                )
            }
        }
    }

    fun clearSqlResult() {
        _sqlResult.value = null
    }

    fun setPlayerPositionFilter(position: String) {
        _playerPositionFilter.value = position
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun playHighlight(videoUrl: String) {
        _playingVideoUrl.value = videoUrl
    }

    fun closeHighlightPlayer() {
        _playingVideoUrl.value = null
    }

    private fun fetchWikiSummary(title: String) {
        viewModelScope.launch {
            _isWikiLoading.value = true
            val summary = withContext(Dispatchers.IO) {
                repository.fetchWikipediaSummary(title)
            }
            _wikiSummaryState.value = summary
            _isWikiLoading.value = false
        }
    }

    // Interactive simulated live match events & push notification generator!
    private fun startLiveScoreSimulation() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(15000) // update simulation status every 15 seconds!
                val matches = repository.allMatchesFlow.first()
                val liveMatches = matches.filter { it.status == "LIVE" }
 
                if (liveMatches.isNotEmpty()) {
                    // Randomly select a live match to update
                    val targetMatch = liveMatches[Random.nextInt(liveMatches.size)]
                    val nextMinute = targetMatch.minute + Random.nextInt(1, 3)
                    val teamHome = repository.getTeamById(targetMatch.homeTeamId)?.name ?: "Đội nhà"
                    val teamAway = repository.getTeamById(targetMatch.awayTeamId)?.name ?: "Đội khách"
                    val matchTitle = "$teamHome vs $teamAway"
 
                    if (nextMinute >= 90) {
                        // Finish match
                        val finishedMatch = targetMatch.copy(
                            minute = 90,
                            status = "FINISHED",
                            matchTime = "FT"
                        )
                        repository.updateMatch(finishedMatch)
                        addSimulationEvent("🏁 Phút 90 ($matchTitle): Trọng tài thổi còi mãn cuộc! Kết quả chung cuộc: $teamHome ${finishedMatch.homeTeamScore} - ${finishedMatch.awayTeamScore} $teamAway")
                        withContext(Dispatchers.Main) {
                            notificationHelper.showSystemNotification(
                                "🏁 FULL TIME whistle!",
                                "$teamHome ${finishedMatch.homeTeamScore} - ${finishedMatch.awayTeamScore} $teamAway"
                            )
                        }
                    } else {
                        // Increment minute, maybe score a goal!
                        val isGoal = Random.nextFloat() < 0.35f // 35% chance of a goal in any tick
                        var nextHomeScore = targetMatch.homeTeamScore
                        var nextAwayScore = targetMatch.awayTeamScore
                        var scorerName = ""
 
                        if (isGoal) {
                            val isHomeGoal = Random.nextBoolean()
                            val scoringTeamId = if (isHomeGoal) targetMatch.homeTeamId else targetMatch.awayTeamId
                            val roster = repository.getPlayersForTeam(scoringTeamId)
 
                            if (roster.isNotEmpty()) {
                                val scorer = roster[Random.nextInt(roster.size)]
                                scorerName = scorer.name
                                if (isHomeGoal) {
                                    nextHomeScore++
                                } else {
                                    nextAwayScore++
                                }
 
                                // Update player stats!
                                val updatedScorer = scorer.copy(goals = scorer.goals + 1)
                                repository.updateMatch(targetMatch.copy(
                                    minute = nextMinute,
                                    homeTeamScore = nextHomeScore,
                                    awayTeamScore = nextAwayScore
                                ))
                                addSimulationEvent("⚽ Phút $nextMinute ($matchTitle): VÀO!!! Cầu thủ $scorerName ghi bàn xuất sắc! Tỉ số nâng lên $nextHomeScore - $nextAwayScore cho ${if (isHomeGoal) teamHome else teamAway}!")
                            }
                        } else {
                            repository.updateMatch(targetMatch.copy(minute = nextMinute))
                            // Generate cards or other situations on the field
                            val eventRand = Random.nextFloat()
                            val eventMsg = when {
                                eventRand < 0.20f -> {
                                    val teamId = if (Random.nextBoolean()) targetMatch.homeTeamId else targetMatch.awayTeamId
                                    val roster = repository.getPlayersForTeam(teamId)
                                    val player = if (roster.isNotEmpty()) roster[Random.nextInt(roster.size)].name else "Cầu thủ"
                                    "🟨 Phút $nextMinute ($matchTitle): Thẻ vàng cho $player của ${if (teamId == targetMatch.homeTeamId) teamHome else teamAway} sau pha tranh chấp bóng nguy hiểm!"
                                }
                                eventRand < 0.30f -> {
                                    val teamId = if (Random.nextBoolean()) targetMatch.homeTeamId else targetMatch.awayTeamId
                                    val roster = repository.getPlayersForTeam(teamId)
                                    val player = if (roster.isNotEmpty()) roster[Random.nextInt(roster.size)].name else "Cầu thủ"
                                    "🟥 Phút $nextMinute ($matchTitle): THẺ ĐỎ! $player bị truất quyền thi đấu trực tiếp sau pha phạm lỗi nghiêm trọng!"
                                }
                                eventRand < 0.50f -> {
                                    val attackingTeam = if (Random.nextBoolean()) teamHome else teamAway
                                    "📐 Phút $nextMinute ($matchTitle): Phạt góc! Sóng gió nổi lên trước khung thành khi $attackingTeam dồn lên tấn công!"
                                }
                                eventRand < 0.70f -> {
                                    val teamId = if (Random.nextBoolean()) targetMatch.homeTeamId else targetMatch.awayTeamId
                                    val roster = repository.getPlayersForTeam(teamId)
                                    val player = if (roster.isNotEmpty()) roster[Random.nextInt(roster.size)].name else "Cầu thủ"
                                    "🧤 Phút $nextMinute ($matchTitle): Cản phá! Cú sút xa cực căng hiểm hóc của $player nhưng thủ môn phản xạ xuất thần cản phá!"
                                }
                                eventRand < 0.85f -> {
                                    "🖥️ Phút $nextMinute ($matchTitle): Tổ VAR đang xem lại một tình huống chạm tay trong vòng cấm đầy kịch tính!"
                                }
                                else -> {
                                    val teamId = if (Random.nextBoolean()) targetMatch.homeTeamId else targetMatch.awayTeamId
                                    val roster = repository.getPlayersForTeam(teamId)
                                    val player = if (roster.isNotEmpty()) roster[Random.nextInt(roster.size)].name else "Cầu thủ"
                                    "💥 Phút $nextMinute ($matchTitle): BÓNG ĐẬP XÀ NGANG! $player dứt điểm dội xà đầy tiếc nuối cho ${if (teamId == targetMatch.homeTeamId) teamHome else teamAway}!"
                                }
                            }
                            addSimulationEvent(eventMsg)
                        }
 
                        if (isGoal && scorerName.isNotEmpty()) {
                            val newScore = "$nextHomeScore - $nextAwayScore"
                            withContext(Dispatchers.Main) {
                                notificationHelper.showGoalNotification(
                                    matchTitle,
                                    scorerName,
                                    newScore
                                )
                            }
                        }
                    }
                } else {
                    // If no live matches, optionally revive a finished or upcoming match to keep the app dynamic
                    val anyUpcoming = matches.filter { it.status == "UPCOMING" }
                    if (anyUpcoming.isNotEmpty()) {
                        val toLive = anyUpcoming.first().copy(
                            status = "LIVE",
                            minute = 1,
                            homeTeamScore = 0,
                            awayTeamScore = 0,
                            matchTime = "LIVE"
                        )
                        repository.updateMatch(toLive)
                        val home = repository.getTeamById(toLive.homeTeamId)?.name ?: "Đội nhà"
                        val away = repository.getTeamById(toLive.awayTeamId)?.name ?: "Đội khách"
                        addSimulationEvent("⚽ Phút 1 ($home vs $away): TRẬN ĐẤU BẮT ĐẦU! Tiếng còi khai cuộc khai màn trận đấu hấp dẫn!")
                        withContext(Dispatchers.Main) {
                            notificationHelper.showSystemNotification(
                                "⚽ KICK OFF!",
                                "$home vs $away has started LIVE!"
                            )
                        }
                    }
                }
            }
        }
    }

    // --- AUTHENTICATION ---
    fun register(username: String, password: String, role: String, fullName: String, onResult: (Boolean, String) -> Unit) {
        if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
            onResult(false, "Vui lòng điền đầy đủ thông tin.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (role == "ADMIN") {
                    val adminCount = repository.getAdminCount()
                    if (adminCount >= 1) {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Hệ thống chỉ cho phép tối đa 1 tài khoản Admin duy nhất.")
                        }
                        return@launch
                    }
                }
                val existing = repository.getUserByUsername(username)
                if (existing != null) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Tên đăng nhập đã tồn tại.")
                    }
                } else {
                    val newUser = UserEntity(username = username, password = password, role = role, fullName = fullName)
                    repository.registerUser(newUser)
                    val registered = repository.getUserByUsername(username)
                    withContext(Dispatchers.Main) {
                        _currentUser.value = registered
                        onResult(true, "Đăng ký thành công!")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Có lỗi xảy ra: ${e.message}")
                }
            }
        }
    }

    fun login(username: String, password: String, onResult: (Boolean, String) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            onResult(false, "Vui lòng điền đầy đủ tên đăng nhập và mật khẩu.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = repository.getUserByUsername(username)
                withContext(Dispatchers.Main) {
                    if (user != null && user.password == password) {
                        _currentUser.value = user
                        onResult(true, "Đăng nhập thành công!")
                    } else {
                        onResult(false, "Sai tên đăng nhập hoặc mật khẩu.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Có lỗi xảy ra: ${e.message}")
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- COMMUNITY POSTS ---
    fun createPost(teamId: Int, teamName: String, title: String, content: String, imageUrl: String, onResult: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false)
            return
        }
        if (title.isBlank() || content.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val post = PostEntity(
                teamId = teamId,
                teamName = teamName,
                title = title,
                content = content,
                imageUrl = imageUrl,
                authorName = user.fullName,
                authorRole = user.role
            )
            repository.insertPost(post)
            withContext(Dispatchers.Main) {
                onResult(true)
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePost(postId)
        }
    }

    fun toggleLikePost(post: PostEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isLikedNow = !post.isLiked
            val likesDiff = if (isLikedNow) 1 else -1
            val updatedPost = post.copy(
                isLiked = isLikedNow,
                likesCount = (post.likesCount + likesDiff).coerceAtLeast(0)
            )
            repository.updatePost(updatedPost)
        }
    }

    fun toggleSavePost(post: PostEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSavedNow = !post.isSaved
            val savesDiff = if (isSavedNow) 1 else -1
            val updatedPost = post.copy(
                isSaved = isSavedNow,
                savesCount = (post.savesCount + savesDiff).coerceAtLeast(0)
            )
            repository.updatePost(updatedPost)
        }
    }

    // --- COMMENTS ---
    fun getCommentsForPost(postId: Int): Flow<List<CommentEntity>> {
        return repository.getCommentsForPostFlow(postId)
    }

    fun getCommentsForTeam(teamId: Int): Flow<List<CommentEntity>> {
        return repository.getCommentsForTeamFlow(teamId)
    }

    fun addComment(postId: Int, teamId: Int, content: String, onResult: (Boolean) -> Unit) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false)
            return
        }
        if (content.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val comment = CommentEntity(
                postId = postId,
                teamId = teamId,
                content = content,
                authorName = user.fullName,
                authorRole = user.role
            )
            repository.insertComment(comment)
            withContext(Dispatchers.Main) {
                onResult(true)
            }
        }
    }

    fun deleteComment(commentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteComment(commentId)
        }
    }
}

class FootballViewModelFactory(
    private val application: Application,
    private val repository: FootballRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FootballViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FootballViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SqlResult(
    val query: String,
    val columnNames: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false
)
