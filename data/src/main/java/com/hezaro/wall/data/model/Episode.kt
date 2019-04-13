package com.hezaro.wall.data.model

data class Episode(
    var id: Int,
    var title: String,
    var description: String,
    var creator: Any,
    var votes: Int,
    val podcast_id: String,
    val podcast_title: String,
    var view: Int,
    var poster: String,
    var file_url: String,
    var published_at: String,
    var jdate: String,
    var comments_count: Int
) {

}