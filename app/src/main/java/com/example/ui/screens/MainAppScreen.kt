package com.example.ui.screens

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.api.WikipediaResponse
import com.example.data.database.MatchEntity
import com.example.data.database.PlayerEntity
import com.example.data.database.TeamEntity
import com.example.data.database.UserEntity
import com.example.data.database.PostEntity
import com.example.data.database.CommentEntity
import com.example.ui.components.TacticalPitchView
import com.example.viewmodel.FootballViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: FootballViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val teams by viewModel.teamsState.collectAsStateWithLifecycle()
    val matches by viewModel.matchesState.collectAsStateWithLifecycle()
    val selectedTeam by viewModel.selectedTeam.collectAsStateWithLifecycle()
    val teamPlayers by viewModel.teamPlayers.collectAsStateWithLifecycle()
    val playerPositionFilter by viewModel.playerPositionFilter.collectAsStateWithLifecycle()
    val selectedPlayer by viewModel.selectedPlayer.collectAsStateWithLifecycle()
    val wikiSummary by viewModel.wikiSummaryState.collectAsStateWithLifecycle()
    val isWikiLoading by viewModel.isWikiLoading.collectAsStateWithLifecycle()
    val playingVideoUrl by viewModel.playingVideoUrl.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedStadiumMatch by viewModel.selectedStadiumMatch.collectAsStateWithLifecycle()
    val stadiumWikiSummary by viewModel.stadiumWikiSummary.collectAsStateWithLifecycle()
    val isStadiumWikiLoading by viewModel.isStadiumWikiLoading.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("schedules") } // "schedules", "teams", "highlights", "community"
    var showAuthDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Soccer Ball Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "World Cup Live",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light/Dark Theme"
                        )
                    }
                    if (selectedTeam != null) {
                        IconButton(onClick = { viewModel.selectTeam(selectedTeam!!) }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh Roster")
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    if (currentUser != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { viewModel.logout() }
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (currentUser!!.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser!!.fullName.take(8),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showAuthDialog = true },
                            modifier = Modifier.padding(end = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đăng nhập", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == "schedules",
                    onClick = { currentTab = "schedules" },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Schedules") },
                    label = { Text("Tỉ Số & BXH", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_schedules")
                )
                NavigationBarItem(
                    selected = currentTab == "teams",
                    onClick = { currentTab = "teams" },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Teams") },
                    label = { Text("Đội Tuyển", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_teams")
                )
                NavigationBarItem(
                    selected = currentTab == "highlights",
                    onClick = { currentTab = "highlights" },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Highlights") },
                    label = { Text("Highlights", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_highlights")
                )
                NavigationBarItem(
                    selected = currentTab == "community",
                    onClick = { currentTab = "community" },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Community") },
                    label = { Text("Cộng đồng", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_community")
                )
                NavigationBarItem(
                    selected = currentTab == "profile",
                    onClick = { currentTab = "profile" },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Cá nhân") },
                    label = { Text("Cá nhân", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "schedules" -> SchedulesAndStandingsScreen(
                    matches = matches,
                    teams = teams,
                    onHighlightPlay = { viewModel.playHighlight(it) },
                    viewModel = viewModel
                )
                "teams" -> TeamsScreen(
                    teams = teams,
                    selectedTeam = selectedTeam,
                    teamPlayers = teamPlayers,
                    positionFilter = playerPositionFilter,
                    onTeamSelected = { viewModel.selectTeam(it) },
                    onPlayerSelected = { viewModel.selectPlayer(it) },
                    onFilterChange = { viewModel.setPlayerPositionFilter(it) },
                    onBackToTeams = { viewModel.selectTeam(teams.first()) },
                    viewModel = viewModel
                )
                "highlights" -> VideoHighlightsScreen(
                    onHighlightPlay = { viewModel.playHighlight(it) }
                )
                "community" -> CommunityScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel, onShowAuth = { showAuthDialog = true })
            }

            // Global Overlay Player Details dialog (from Wikipedia API)
            selectedPlayer?.let { player ->
                PlayerDetailsOverlay(
                    player = player,
                    wikiSummary = wikiSummary,
                    isLoading = isWikiLoading,
                    onDismiss = { viewModel.dismissPlayerDetails() } // Toggle/Close
                )
            }

            // Global Overlay Stadium Details dialog (from Wikipedia API)
            selectedStadiumMatch?.let { match ->
                StadiumDetailsOverlay(
                    match = match,
                    wikiSummary = stadiumWikiSummary,
                    isLoading = isStadiumWikiLoading,
                    onDismiss = { viewModel.dismissStadiumDetails() }
                )
            }

            // Global Video Highlight Stream player overlay
            playingVideoUrl?.let { url ->
                HighlightPlayerOverlay(
                    videoUrl = url,
                    onClose = { viewModel.closeHighlightPlayer() }
                )
            }

            // Auth Dialog Overlay
            if (showAuthDialog) {
                AuthDialog(
                    viewModel = viewModel,
                    onDismiss = { showAuthDialog = false }
                )
            }
        }
    }
}

@Composable
fun SchedulesAndStandingsScreen(
    matches: List<MatchEntity>,
    teams: List<TeamEntity>,
    onHighlightPlay: (String) -> Unit,
    viewModel: FootballViewModel
) {
    var listTab by remember { mutableStateOf("live") } // "live", "standings"
    var scheduleFilter by remember { mutableStateOf("ALL") } // "ALL", "GROUP", "PLAYOFF"
    var standingsFilter by remember { mutableStateOf("GROUP") } // "GROUP", "PLAYOFF"
    
    val simulationEvents by viewModel.simulationEvents.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub Tabs
        TabRow(
            selectedTabIndex = if (listTab == "live") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = listTab == "live",
                onClick = { listTab = "live" },
                text = { Text("Lịch Thi Đấu & Live Score", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = listTab == "standings",
                onClick = { listTab = "standings" },
                text = { Text("BXH & Nhánh Đấu", fontWeight = FontWeight.Bold) }
            )
        }

        if (listTab == "live") {
            // Filter Stage Chips for Matches/Schedule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Tất cả",
                    "GROUP" to "Vòng Bảng",
                    "PLAYOFF" to "Playoffs"
                ).forEach { (code, label) ->
                    val isSelected = scheduleFilter == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { scheduleFilter = code },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Filtered Matches list
            val filteredMatches = when (scheduleFilter) {
                "GROUP" -> matches.filter { !it.isPlayoff }
                "PLAYOFF" -> matches.filter { it.isPlayoff }
                else -> matches
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Simulation ticker at the very top of live tab
                item {
                    SimulationEventsTicker(events = simulationEvents)
                }

                val live = filteredMatches.filter { it.status == "LIVE" }
                val upcoming = filteredMatches.filter { it.status == "UPCOMING" }
                val finished = filteredMatches.filter { it.status == "FINISHED" }

                if (live.isNotEmpty()) {
                    item {
                        SectionHeader(title = "🔴 TRỰC TIẾP LIVE SCORE")
                    }
                    items(live) { match ->
                        MatchCard(
                            match = match, 
                            teams = teams, 
                            onHighlightPlay = onHighlightPlay,
                            onStadiumClick = { viewModel.selectStadiumMatch(it) }
                        )
                    }
                }

                if (upcoming.isNotEmpty()) {
                    item {
                        SectionHeader(title = "LỊCH THI ĐẤU TIẾP THEO")
                    }
                    items(upcoming) { match ->
                        MatchCard(
                            match = match, 
                            teams = teams, 
                            onHighlightPlay = onHighlightPlay,
                            onStadiumClick = { viewModel.selectStadiumMatch(it) }
                        )
                    }
                }

                if (finished.isNotEmpty()) {
                    item {
                        SectionHeader(title = "ĐÃ KẾT THÚC HIGHLIGHTS")
                    }
                    items(finished) { match ->
                        MatchCard(
                            match = match, 
                            teams = teams, 
                            onHighlightPlay = onHighlightPlay,
                            onStadiumClick = { viewModel.selectStadiumMatch(it) }
                        )
                    }
                }
                
                if (filteredMatches.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không có trận đấu nào phù hợp bộ lọc.", color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Standings / Bracket tab
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "GROUP" to "Bảng Xếp Hạng Vòng Bảng",
                        "PLAYOFF" to "Nhánh Đấu Playoffs"
                    ).forEach { (code, label) ->
                        val isSelected = standingsFilter == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { standingsFilter = code },
                            label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (standingsFilter == "GROUP") {
                    GroupStandingsView(teams = teams)
                } else {
                    PlayoffsBracketView(
                        matches = matches,
                        teams = teams,
                        onHighlightPlay = onHighlightPlay,
                        onStadiumClick = { viewModel.selectStadiumMatch(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupStandingsView(teams: List<TeamEntity>) {
    val groups = teams.groupBy { it.groupName }.toSortedMap()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groups.forEach { (groupName, groupTeams) ->
            item {
                Text(
                    text = groupName.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#", modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("ĐỘI BÓNG", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("TR", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text("T", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text("H", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text("B", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text("HS", modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                            Text("ĐIỂM", modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }

                        // Table Body Rows (sorted by points, goalsFor - goalsAgainst)
                        val sortedGroupTeams = groupTeams.sortedWith(
                            compareByDescending<TeamEntity> { it.points }
                                .thenByDescending { it.goalsFor - it.goalsAgainst }
                        )
                        
                        sortedGroupTeams.forEachIndexed { idx, team ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    modifier = Modifier.width(20.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (idx < 2) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontSize = 12.sp
                                )

                                Row(
                                    modifier = Modifier.weight(1.2f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = team.flagUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp, 13.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = team.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(text = "${team.played}", modifier = Modifier.width(28.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text(text = "${team.won}", modifier = Modifier.width(28.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text(text = "${team.drawn}", modifier = Modifier.width(28.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text(text = "${team.lost}", modifier = Modifier.width(28.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                val diff = team.goalsFor - team.goalsAgainst
                                val diffStr = if (diff > 0) "+$diff" else "$diff"
                                Text(text = diffStr, modifier = Modifier.width(36.dp), fontSize = 11.sp, textAlign = TextAlign.Center, color = if (diff > 0) Color(0xFF4CAF50) else if (diff < 0) Color.Red else Color.Gray)
                                Text(text = "${team.points}", modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)
                            }
                            if (idx < sortedGroupTeams.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayoffsBracketView(
    matches: List<MatchEntity>,
    teams: List<TeamEntity>,
    onHighlightPlay: (String) -> Unit,
    onStadiumClick: (MatchEntity) -> Unit
) {
    val playoffMatches = matches.filter { it.isPlayoff }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "NHÁNH ĐẤU PLAYOFFS",
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "Cập nhật kết quả các trận đấu knock-out, tứ kết, bán kết và chung kết giải đấu.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            )
        }

        val finalMatch = playoffMatches.find { it.id == 1 }
        val semiMatches = playoffMatches.filter { it.id == 2 }
        val quarterMatches = playoffMatches.filter { it.id == 3 }

        if (finalMatch != null) {
            item {
                PlayoffRoundSection(
                    title = "CHUNG KẾT (FINAL)",
                    match = finalMatch,
                    teams = teams,
                    onHighlightPlay = onHighlightPlay,
                    onStadiumClick = onStadiumClick
                )
            }
        }

        if (semiMatches.isNotEmpty()) {
            item {
                Text(
                    text = "BÁN KẾT (SEMI-FINALS)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            items(semiMatches) { match ->
                MatchCard(
                    match = match,
                    teams = teams,
                    onHighlightPlay = onHighlightPlay,
                    onStadiumClick = onStadiumClick
                )
            }
        }

        if (quarterMatches.isNotEmpty()) {
            item {
                Text(
                    text = "TỨ KẾT (QUARTER-FINALS)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            items(quarterMatches) { match ->
                MatchCard(
                    match = match,
                    teams = teams,
                    onHighlightPlay = onHighlightPlay,
                    onStadiumClick = onStadiumClick
                )
            }
        }
        
        if (playoffMatches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có thông tin trận đấu playoff.", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun PlayoffRoundSection(
    title: String,
    match: MatchEntity,
    teams: List<TeamEntity>,
    onHighlightPlay: (String) -> Unit,
    onStadiumClick: (MatchEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFFFF9800),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
        MatchCard(
            match = match,
            teams = teams,
            onHighlightPlay = onHighlightPlay,
            onStadiumClick = onStadiumClick
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    )
}

@Composable
fun SimulationEventsTicker(events: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("simulation_ticker_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Sim",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DIỄN BIẾN GIẢ LẬP TRỰC TUYẾN ⚡",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE FEED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (events.isEmpty()) {
                Text(
                    text = "Chờ đón các diễn biến trực tiếp từ các trận đấu đang diễn ra...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            } else {
                // Show the latest event with special prominent layout
                val latest = events.first()
                val iconColor = when {
                    latest.contains("⚽") -> Color(0xFF4CAF50)
                    latest.contains("🟨") -> Color(0xFFFFC107)
                    latest.contains("🟥") -> Color(0xFFF44336)
                    latest.contains("🏁") -> Color(0xFF2196F3)
                    else -> MaterialTheme.colorScheme.tertiary
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(iconColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    latest.contains("⚽") -> "⚽"
                                    latest.contains("🟨") -> "🟨"
                                    latest.contains("🟥") -> "🟥"
                                    latest.contains("🏁") -> "🏁"
                                    latest.contains("🖥️") -> "🖥️"
                                    latest.contains("🧤") -> "🧤"
                                    else -> "📢"
                                },
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = latest,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (events.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Các tình huống chú ý vừa diễn ra:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        events.take(5).drop(1).forEach { ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = ev,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: MatchEntity,
    teams: List<TeamEntity>,
    onHighlightPlay: (String) -> Unit,
    onStadiumClick: (MatchEntity) -> Unit
) {
    val homeTeam = teams.find { it.id == match.homeTeamId }
    val awayTeam = teams.find { it.id == match.awayTeamId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Match info line (Live indicator, or upcoming time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORLD CUP 2026",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )

                if (match.status == "LIVE") {
                    Box(
                        modifier = Modifier
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE • ${match.minute}'",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Text(
                        text = match.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (match.status == "FINISHED") Color.Gray else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scoreboard view
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = homeTeam?.flagUrl ?: "",
                        contentDescription = "Flag ${homeTeam?.name}",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { onStadiumClick(match) },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = homeTeam?.name ?: "Home",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (match.status == "UPCOMING") {
                        Text(
                            text = match.matchTime,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "${match.homeTeamScore}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (match.status == "LIVE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = " - ",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "${match.awayTeamScore}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (match.status == "LIVE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Away Team
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = awayTeam?.flagUrl ?: "",
                        contentDescription = "Flag ${awayTeam?.name}",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable { onStadiumClick(match) },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = awayTeam?.name ?: "Away",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nhấn vào Quốc Kỳ để xem thông tin sân vận động ",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (match.status == "FINISHED" && match.highlightsUrl.isNotEmpty()) {
                Button(
                    onClick = { onHighlightPlay(match.highlightsUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Watch Highlights")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Xem Highlights Video", fontWeight = FontWeight.Bold)
                }
            } else if (match.status == "LIVE") {
                Text(
                    text = "Giả lập trực tuyến: Goals, thẻ phạt diễn ra thời gian thực...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StandingsTable(teams: List<TeamEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("ĐỘI BÓNG", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("TR", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("T", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("H", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("B", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("HS", modifier = Modifier.width(42.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("ĐIỂM", modifier = Modifier.width(42.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            // Body Rows
            LazyColumn {
                items(teams.size) { index ->
                    val team = teams[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.width(24.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (index < 4) MaterialTheme.colorScheme.primary else Color.Gray,
                            fontSize = 13.sp
                        )

                        Row(
                            modifier = Modifier.weight(1.2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = team.flagUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp, 16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = team.name,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp
                            )
                        }

                        Text("${team.played}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontSize = 13.sp)
                        Text("${team.won}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontSize = 13.sp)
                        Text("${team.drawn}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontSize = 13.sp)
                        Text("${team.lost}", modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, fontSize = 13.sp)
                        Text("${team.goalsFor - team.goalsAgainst}", modifier = Modifier.width(42.dp), textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${team.points}",
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun TeamsScreen(
    teams: List<TeamEntity>,
    selectedTeam: TeamEntity?,
    teamPlayers: List<PlayerEntity>,
    positionFilter: String,
    onTeamSelected: (TeamEntity) -> Unit,
    onPlayerSelected: (PlayerEntity) -> Unit,
    onFilterChange: (String) -> Unit,
    onBackToTeams: () -> Unit,
    viewModel: FootballViewModel
) {
    var inDetailMode by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTeam) {
        if (selectedTeam != null) {
            inDetailMode = true
        }
    }

    if (!inDetailMode || selectedTeam == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "10 ĐỘI BÓNG WORLD CUP 🏆",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTeamSelected(team)
                                inDetailMode = true
                            }
                            .testTag("team_card_${team.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = team.flagUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp, 44.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = team.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = team.groupName,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Detailed squad view
        var detailTab by remember { mutableStateOf("squad") } // "squad", "forum"

        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { inDetailMode = false }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to list")
                }
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = selectedTeam.flagUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp, 22.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedTeam.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
            }

            // Sub Tab Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { detailTab = "squad" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (detailTab == "squad") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (detailTab == "squad") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Đội hình Roster", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { detailTab = "forum" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (detailTab == "forum") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (detailTab == "forum") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hội Cổ Động Viên", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (detailTab == "squad") {
                // Draw Custom view: Tactical Pitch Model!
                TacticalPitchView(
                    players = teamPlayers,
                    onPlayerSelected = onPlayerSelected,
                    modifier = Modifier.fillMaxWidth()
                )

                // Player List Header & filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH SÁCH CẦU THỦ",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Position Filters Row
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "GK", "DF", "MF", "FW").forEach { pos ->
                            val selected = positionFilter == pos
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(4.dp)
                                            )
                                    .clickable { onFilterChange(pos) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = pos,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Filtered Player Squad
                val filteredPlayers = if (positionFilter == "ALL") {
                    teamPlayers
                } else {
                    teamPlayers.filter { it.position == positionFilter }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredPlayers) { player ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayerSelected(player) }
                                .testTag("player_item_${player.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        val defaultProfile = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
                                        val photoUrl = player.imageUrl.ifEmpty { defaultProfile }

                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = player.name,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Badge for Jersey number
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.BottomEnd)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${player.number}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "Vị trí: ${player.position} • Tuổi: ${player.age} • Quốc tịch: ${player.nationality}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                        )
                                    }
                                }

                                Row {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 12.dp)) {
                                        Text("⚽ Goals", fontSize = 11.sp, color = Color.Gray)
                                        Text("${player.goals}", fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🎯 Assist", fontSize = 11.sp, color = Color.Gray)
                                        Text("${player.assists}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                TeamForumView(
                    team = selectedTeam,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun VideoHighlightsScreen(onHighlightPlay: (String) -> Unit) {
    val highlightVideos = listOf(
        HighlightItem(1, "ARG 3 - 3 FRA [Chung Kết Kịch Tính]", "8:42", "2.1M views", "https://flagcdn.com/w160/ar.png", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        HighlightItem(2, "BRA 2 - 1 GER [Bản Lĩnh Selecao]", "6:15", "1.5M views", "https://flagcdn.com/w160/br.png", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        HighlightItem(3, "POR 3 - 2 ESP [Siêu Phẩm Hủy Diệt]", "10:05", "3.4M views", "https://flagcdn.com/w160/pt.png", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        HighlightItem(4, "JPN 2 - 1 GER [Kỳ Tích Châu Á]", "5:50", "950K views", "https://flagcdn.com/w160/jp.png", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        HighlightItem(5, "ENG 1 - 2 FRA [Quyết Chiến Châu Âu]", "7:30", "1.1M views", "https://flagcdn.com/w160/gb.png", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "HIGHLIGHTS TRẬN ĐẤU 🔥",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(highlightVideos) { video ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHighlightPlay(video.url) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp, 64.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = video.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = video.duration,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = video.views,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class HighlightItem(
    val id: Int,
    val title: String,
    val duration: String,
    val views: String,
    val imageUrl: String,
    val url: String
)

@Composable
fun PlayerDetailsOverlay(
    player: PlayerEntity,
    wikiSummary: WikipediaResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thoát", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Thông Tin Cầu Thủ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Thoát",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo
                val defaultProfile = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
                val photoUrl = if (wikiSummary?.thumbnail?.source != null) {
                    wikiSummary.thumbnail.source
                } else if (player.imageUrl.isNotEmpty()) {
                    player.imageUrl
                } else {
                    defaultProfile
                }

                AsyncImage(
                    model = photoUrl,
                    contentDescription = player.name,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = player.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            text = "Vị trí: ${player.position}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = "Số áo: #${player.number}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player Details Card (Age, Nationality, Former Clubs)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tuổi:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${player.age} tuổi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quốc tịch:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = player.nationality.ifEmpty { "Không rõ" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (player.formerTeams.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Đội bóng từng tham gia:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = player.formerTeams,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        if (player.awards.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                            
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Giải thưởng & Danh hiệu:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = player.awards,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Đang tải dữ liệu Wikipedia...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else if (wikiSummary != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tóm tắt Wikipedia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wikiSummary.extract ?: "Không tìm thấy nội dung tóm tắt.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // Fallback local info
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Mô tả",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${player.name} là cầu thủ bóng đá chuyên nghiệp chơi ở vị trí ${player.position} cho đội tuyển quốc gia. Đã ghi ${player.goals} bàn thắng và kiến tạo ${player.assists} lần.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun StadiumDetailsOverlay(
    match: MatchEntity,
    wikiSummary: WikipediaResponse?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Đóng", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Thông Tin Sân Vận Động", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val photoUrl = if (wikiSummary?.thumbnail?.source != null && wikiSummary.thumbnail.source.isNotEmpty()) {
                    wikiSummary.thumbnail.source
                } else if (match.stadiumImageUrl.isNotEmpty()) {
                    match.stadiumImageUrl
                } else {
                    "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800&auto=format&fit=crop"
                }

                AsyncImage(
                    model = photoUrl,
                    contentDescription = match.stadiumName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = match.stadiumName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Quốc gia: ${match.stadiumCountry}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Đang tải dữ liệu Wikipedia...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else if (wikiSummary != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tóm tắt Wikipedia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wikiSummary.extract ?: "Không tìm thấy nội dung tóm tắt từ Wikipedia.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Chi tiết Sân vận động",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sân vận động ${match.stadiumName} tọa lạc tại ${match.stadiumCountry}. Đây là một trong những địa điểm tổ chức thi đấu chính thức cho giải đấu đỉnh cao hành tinh.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun HighlightPlayerOverlay(
    videoUrl: String,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.4f) }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Đóng", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Đang chiếu Highlight", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video visual content simulator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { isPlaying = !isPlaying }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPlaying) "Đang phát video highlights..." else "Đã Tạm Dừng",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator bar
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        thumbColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("03:12", color = Color.White, fontSize = 11.sp)
                    Text("08:42", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    )
}

@Composable
fun AuthDialog(
    viewModel: FootballViewModel,
    onDismiss: () -> Unit
) {
    var isLoginTab by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var isAdminRole by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = { 
                        isLoginTab = true
                        errorMessage = ""
                        successMessage = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isLoginTab) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text("ĐĂNG NHẬP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                TextButton(
                    onClick = { 
                        isLoginTab = false
                        errorMessage = ""
                        successMessage = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (!isLoginTab) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text("ĐĂNG KÝ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                if (!isLoginTab) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Họ và Tên") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Tên đăng nhập") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                if (!isLoginTab && password.isNotEmpty()) {
                    val hasMinLength = password.length >= 6
                    val hasDigit = password.any { it.isDigit() }
                    val hasSpecialOrUpper = password.any { it.isUpperCase() || !it.isLetterOrDigit() }

                    val strengthScore = (if (hasMinLength) 1 else 0) + (if (hasDigit) 1 else 0) + (if (hasSpecialOrUpper) 1 else 0)
                    val (strengthText, strengthColor) = when (strengthScore) {
                        0 -> "Cực kỳ yếu " to Color(0xFFE57373)
                        1 -> "Yếu " to Color(0xFFE57373)
                        2 -> "Trung bình " to Color(0xFFFFB74D)
                        else -> "Mạnh " to Color(0xFF81C784)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Độ bảo mật: $strengthText",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = strengthColor
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = strengthScore.toFloat() / 3f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = strengthColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasMinLength) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (hasMinLength) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mật khẩu tối thiểu 6 ký tự",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasMinLength) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasDigit) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (hasDigit) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chứa ít nhất 1 chữ số",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasDigit) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasSpecialOrUpper) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (hasSpecialOrUpper) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chứa chữ in hoa hoặc ký tự đặc biệt",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasSpecialOrUpper) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }
                }

                if (!isLoginTab) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAdminRole = !isAdminRole }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isAdminRole,
                            onCheckedChange = { isAdminRole = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đăng ký tài khoản Admin",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorMessage = ""
                    successMessage = ""
                    if (isLoginTab) {
                        viewModel.login(username, password) { success, msg ->
                            if (success) {
                                successMessage = msg
                                onDismiss()
                            } else {
                                errorMessage = msg
                            }
                        }
                    } else {
                        if (password.length < 6) {
                            errorMessage = "Mật khẩu phải chứa ít nhất 6 ký tự."
                            return@Button
                        }
                        if (!password.any { it.isDigit() }) {
                            errorMessage = "Mật khẩu phải chứa ít nhất 1 chữ số."
                            return@Button
                        }
                        if (!password.any { it.isUpperCase() || !it.isLetterOrDigit() }) {
                            errorMessage = "Mật khẩu phải chứa ít nhất 1 chữ in hoa hoặc ký tự đặc biệt."
                            return@Button
                        }
                        val role = if (isAdminRole) "ADMIN" else "USER"
                        viewModel.register(username, password, role, fullName) { success, msg ->
                            if (success) {
                                successMessage = msg
                                onDismiss()
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoginTab) "Đăng nhập" else "Đăng ký thành viên")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Hủy bỏ")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: FootballViewModel) {
    val posts by viewModel.postsState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val teams by viewModel.teamsState.collectAsStateWithLifecycle()

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<PostEntity?>(null) }
    var teamFilterId by remember { mutableStateOf<Int?>(null) } // null = All

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Search/Filter Horizontal list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BÀI VIẾT CỘNG ĐỒNG 📣",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )

            if (currentUser != null) {
                Button(
                    onClick = { showCreatePostDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tạo bài viết", fontSize = 12.sp)
                }
            }
        }

        // Horizontal filter tags
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                val isAllSelected = teamFilterId == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = { teamFilterId = null },
                    label = { Text("Tất cả", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
            items(teams) { team ->
                val isSelected = teamFilterId == team.id
                FilterChip(
                    selected = isSelected,
                    onClick = { teamFilterId = team.id },
                    label = { Text(team.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        AsyncImage(
                            model = team.flagUrl,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp, 11.dp)
                        )
                    }
                )
            }
        }

        if (currentUser == null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vui lòng đăng nhập để có thể đăng bài viết, chia sẻ hình ảnh và bình luận cùng mọi người!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        val filteredPosts = if (teamFilterId == null) {
            posts
        } else {
            posts.filter { it.teamId == teamFilterId }
        }

        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chưa có bài viết nào thuộc chủ đề này. Hãy là người đầu tiên chia sẻ!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPosts) { post ->
                    PostCard(
                        post = post,
                        currentUser = currentUser,
                        onPostClick = { selectedPostForDetail = post },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        onLikeClick = { viewModel.toggleLikePost(post) },
                        onSaveClick = { viewModel.toggleSavePost(post) }
                    )
                }
            }
        }
    }

    if (showCreatePostDialog) {
        CreatePostDialog(
            viewModel = viewModel,
            teams = teams,
            onDismiss = { showCreatePostDialog = false }
        )
    }

    selectedPostForDetail?.let { post ->
        PostDetailDialog(
            post = post,
            viewModel = viewModel,
            currentUser = currentUser,
            onDismiss = { selectedPostForDetail = null }
        )
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    currentUser: UserEntity?,
    onPostClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (post.authorRole == "ADMIN") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (post.authorRole == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (post.authorRole == "ADMIN") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Admin", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text(
                        text = if (post.teamId > 0) "Chủ đề: Đội tuyển ${post.teamName}" else "Chủ đề chung",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                if (currentUser?.role == "ADMIN" || currentUser?.fullName == post.authorName) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa bài viết", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post content
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Comments button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPostClick() }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Bình luận",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bình luận",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Like Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLikeClick() }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Thích",
                            tint = if (post.isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (post.likesCount > 0) post.likesCount.toString() else "Thích",
                            fontSize = 12.sp,
                            color = if (post.isLiked) Color.Red else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSaveClick() }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Lưu",
                            tint = if (post.isSaved) MaterialTheme.colorScheme.secondary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (post.savesCount > 0) post.savesCount.toString() else "Lưu",
                            fontSize = 12.sp,
                            color = if (post.isSaved) MaterialTheme.colorScheme.secondary else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(post.timestamp)),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    viewModel: FootballViewModel,
    teams: List<TeamEntity>,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTeamId by remember { mutableStateOf(0) } // 0 = General
    var selectedTeamName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Quick image suggestions from unsplash for beautiful out-of-the-box styling
    val imageSuggestions = listOf(
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop", 
        "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=500&auto=format&fit=crop", 
        "https://images.unsplash.com/photo-1579952365111-3958b45683c4?w=500&auto=format&fit=crop", 
        "https://images.unsplash.com/photo-1431324155629-1a6edd1d226a?w=500&auto=format&fit=crop"  
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("TẠO BÀI VIẾT MỚI", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề bài viết") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Nội dung chia sẻ...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Team tags to categorize
                Text("Chọn chủ đề:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTeamId == 0,
                            onClick = { 
                                selectedTeamId = 0
                                selectedTeamName = ""
                            },
                            label = { Text("Chung", fontSize = 11.sp) }
                        )
                    }
                    items(teams) { team ->
                        FilterChip(
                            selected = selectedTeamId == team.id,
                            onClick = {
                                selectedTeamId = team.id
                                selectedTeamName = team.name
                            },
                            label = { Text(team.name, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Ảnh đính kèm (URL hoặc chọn mẫu nhanh):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Đường dẫn ảnh URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(imageSuggestions) { url ->
                        val isSelected = imageUrl == url
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp, 40.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { imageUrl = url },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        errorMessage = "Vui lòng nhập đầy đủ tiêu đề và nội dung."
                    } else {
                        viewModel.createPost(selectedTeamId, selectedTeamName, title, content, imageUrl) { success ->
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = "Lỗi khi tạo bài viết."
                            }
                        }
                    }
                }
            ) {
                Text("Đăng bài")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun PostDetailDialog(
    post: PostEntity,
    viewModel: FootballViewModel,
    currentUser: UserEntity?,
    onDismiss: () -> Unit
) {
    val comments by viewModel.getCommentsForPost(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CHI TIẾT BÀI VIẾT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Author & category
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(post.authorName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (post.authorRole == "ADMIN") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(Admin)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = if (post.teamId > 0) "Chủ đề: Đội tuyển ${post.teamName}" else "Chủ đề chung",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(post.content, style = MaterialTheme.typography.bodyLarge)

                        if (post.imageUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "BÌNH LUẬN (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có bình luận nào. Hãy gửi bình luận đầu tiên!", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.Gray.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comment.authorName.take(1).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (comment.authorRole == "ADMIN") {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Admin", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                                }

                                if (currentUser?.role == "ADMIN" || currentUser?.fullName == comment.authorName) {
                                    IconButton(
                                        onClick = { viewModel.deleteComment(comment.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Xóa bình luận", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add comment input
                if (currentUser != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Viết bình luận của bạn...", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.addComment(post.id, 0, commentText) { success ->
                                    if (success) commentText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Vui lòng đăng nhập để tham gia bình luận.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun TeamForumView(
    team: TeamEntity,
    viewModel: FootballViewModel
) {
    val comments by viewModel.getCommentsForTeam(team.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "HỘI CỔ ĐỘNG VIÊN ĐỘI TUYỂN ${team.name.uppercase()} ",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (comments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có cuộc thảo luận nào về tuyển ${team.name}. Hãy bình luận đầu tiên để tiếp lửa cho đội tuyển!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(comments) { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (comment.authorRole == "ADMIN") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.Gray.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = comment.authorName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = if (comment.authorRole == "ADMIN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (comment.authorRole == "ADMIN") {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Admin", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(comment.timestamp)),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        if (currentUser?.role == "ADMIN" || currentUser?.fullName == comment.authorName) {
                            IconButton(onClick = { viewModel.deleteComment(comment.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa bình luận", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Comment Input row
        if (currentUser != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Viết bình luận cho ${team.name}...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.addComment(0, team.id, commentText) { success ->
                            if (success) commentText = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Vui lòng đăng nhập để bình luận.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: FootballViewModel,
    onShowAuth: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val posts by viewModel.postsState.collectAsStateWithLifecycle()
    val likedPosts = posts.filter { it.isLiked }
    val savedPosts = posts.filter { it.isSaved }
    var selectedTab by remember { mutableStateOf("liked") } // "liked", "saved"
    var selectedProfilePostDetail by remember { mutableStateOf<PostEntity?>(null) }

    // State for local toggles that connect to account syncing
    var notificationEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var alertOnGoalsOnly by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper block containing profile header and login/logout action
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentUser != null && currentUser!!.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.fullName ?: "Khách hàng",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (currentUser != null) "@${currentUser!!.username} (${currentUser!!.role})" else "Chưa đăng nhập",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Top-right login/logout button
                    if (currentUser == null) {
                        Button(
                            onClick = onShowAuth,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("profile_login_btn")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = "Đăng nhập", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đăng nhập", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("profile_logout_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Đăng xuất", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đăng xuất", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (currentUser == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hãy kết nối tài khoản để đồng bộ dữ liệu cá nhân của bạn, nhận thông báo đẩy về các trận đấu trực tiếp và lưu các tùy chọn yêu thích .",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đã kết nối tài khoản",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }

        // Favorites / Customization checklist section
        Text(
            text = "TÙY CHỌN & ĐỒNG BỘ CÁ NHÂN",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sync data row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tự động đồng bộ yêu thích",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tự động gửi các tùy chỉnh và bài đăng yêu thích .",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { if (currentUser != null) autoSyncEnabled = it },
                        enabled = currentUser != null,
                        modifier = Modifier.testTag("switch_auto_sync")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                // Goals notification row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Thông báo diễn biến trận đấu",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Nhận thông báo đẩy về diễn biến (bàn thắng, thẻ phạt, chấn thương) .",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        modifier = Modifier.testTag("switch_notifications")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                // Alarm filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chỉ nhận thông báo bàn thắng",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Bỏ qua các tình huống thẻ phạt và thẻ đỏ, chỉ nhận bàn thắng (Goals).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = alertOnGoalsOnly,
                        onCheckedChange = { alertOnGoalsOnly = it },
                        modifier = Modifier.testTag("switch_goals_only")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                // Sound notification row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Âm thanh thông báo",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Phát âm thanh cổ vũ khi ghi bàn hoặc tiếng còi phạt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        modifier = Modifier.testTag("switch_sounds")
                    )
                }
            }
        }

        if (currentUser == null) {
            // Guest Warning Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bạn đang trải nghiệm với tư cách người dùng. Các tùy chọn yêu thích sẽ không được lưu vĩnh viễn và không thể đồng bộ hóa giữa các thiết bị nếu không đăng nhập.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
            // Logged in benefits card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Xin chào ${currentUser!!.fullName}! Bạn đã kết nối đồng bộ thành công với máy chủ World Cup Live. Tùy chọn yêu thích và các bài đăng thảo luận của bạn hiện đã được lưu trữ an toàn trên tài khoản.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Liked and Saved Posts Section
        Text(
            text = "BÀI VIẾT ĐÃ THÍCH & ĐÃ LƯU ",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedTab = "liked" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "liked") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == "liked") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Đã thích (${likedPosts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { selectedTab = "saved" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == "saved") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedTab == "saved") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Đã lưu (${savedPosts.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        val displayPosts = if (selectedTab == "liked") likedPosts else savedPosts

        if (displayPosts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == "liked") "Chưa có bài viết nào được yêu thích" else "Chưa có bài viết nào được lưu trữ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayPosts.forEach { post ->
                    PostCard(
                        post = post,
                        currentUser = currentUser,
                        onPostClick = { selectedProfilePostDetail = post },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        onLikeClick = { viewModel.toggleLikePost(post) },
                        onSaveClick = { viewModel.toggleSavePost(post) }
                    )
                }
            }
        }
    }

    selectedProfilePostDetail?.let { post ->
        PostDetailDialog(
            post = post,
            viewModel = viewModel,
            currentUser = currentUser,
            onDismiss = { selectedProfilePostDetail = null }
        )
    }
}
