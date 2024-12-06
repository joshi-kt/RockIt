package com.example.rockit.models

import kotlin.time.Duration

data class Song (
    val id : String = "",
    val name : String = "",
    val duration: Long = 0,
    val explicitContent : Boolean = false,
    val artists : Artists? = null,
    val image : List<DownloadModel>? = null,
    val downloadUrl : List<DownloadModel>? = null
)

data class Artists(
    val primary : List<Artist>
)

data class Artist(
    val name : String
)

data class DownloadModel(
    val quality: String,
    val url : String
)

enum class PlaybackState {
    RESUME,
    PAUSE,
    NEXT,
    PREVIOUS
}