package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO representing the response from Wikipedia page summary API
@JsonClass(generateAdapter = true)
data class WikipediaResponse(
    @Json(name = "title") val title: String?,
    @Json(name = "displaytitle") val displayTitle: String?,
    @Json(name = "extract") val extract: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "thumbnail") val thumbnail: WikiThumbnail?
)

@JsonClass(generateAdapter = true)
data class WikiThumbnail(
    @Json(name = "source") val source: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?
)

// DTOs representing Client-Server request and response data (Request / Response architecture)
@JsonClass(generateAdapter = true)
data class MatchHighlightResponse(
    @Json(name = "match_id") val matchId: Int,
    @Json(name = "title") val title: String,
    @Json(name = "video_url") val videoUrl: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "views") val views: String
)

@JsonClass(generateAdapter = true)
data class LiveMatchResponse(
    @Json(name = "match_id") val matchId: Int,
    @Json(name = "home_team_id") val homeTeamId: Int,
    @Json(name = "away_team_id") val awayTeamId: Int,
    @Json(name = "home_score") val homeScore: Int,
    @Json(name = "away_score") val awayScore: Int,
    @Json(name = "minute") val minute: Int,
    @Json(name = "status") val status: String,
    @Json(name = "latest_event") val latestEvent: String? // e.g. "GOAL! Lionel Messi 42'"
)

@JsonClass(generateAdapter = true)
data class RefreshDataRequest(
    @Json(name = "client_id") val clientId: String,
    @Json(name = "last_sync_timestamp") val lastSyncTimestamp: Long,
    @Json(name = "requested_league") val requestedLeague: String = "WORLD_UP_2026"
)

@JsonClass(generateAdapter = true)
data class UpdateScoreRequest(
    @Json(name = "match_id") val matchId: Int,
    @Json(name = "home_score") val homeScore: Int,
    @Json(name = "away_score") val awayScore: Int,
    @Json(name = "secret_token") val secretToken: String
)
