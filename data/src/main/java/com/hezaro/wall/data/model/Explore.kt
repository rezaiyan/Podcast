package com.hezaro.wall.data.model

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Explore(val response: MutableList<ExploreItem> = mutableListOf())

class DExplore(
    var episodeItems: MutableList<EpisodeItem> = mutableListOf(),
    var podcastItems: MutableList<PodcastItem> = mutableListOf(),
    var categoryItems: MutableList<CategoryItem> = mutableListOf()
) {

    val totalSize by lazy { episodeItems.size + podcastItems.size + categoryItems.size }

    private val exploreItem = mutableListOf<ExploreItem>()

    fun getMergedList(): MutableList<ExploreItem> {
        if (exploreItem.isEmpty()) {
            episodeItems.forEach {
                exploreItem.add(ExploreItem(it.title, it.type, it.order))
            }
            podcastItems.forEach {
                exploreItem.add(ExploreItem(it.title, it.type, it.order))
            }
            categoryItems.forEach {
                exploreItem.add(ExploreItem(it.title, it.type, it.order))
            }
        }
        return exploreItem
    }
}

open class ExploreItem(
    var title: String = "",
    var type: Int = -1,
    var order: Int = -1
)

class EpisodeItem(var episodes: MutableList<Episode> = mutableListOf()) : ExploreItem()
class PodcastItem(var podcasts: MutableList<Podcast> = mutableListOf()) : ExploreItem()
class CategoryItem(var categories: MutableList<Category> = mutableListOf()) : ExploreItem()

data class Category(var id: Int, var title: String, var title_fa: String, var count: Int, var description: String)

class ExploreDeserializer : JsonDeserializer<DExplore> {
    lateinit var explore: DExplore
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DExplore {
        var valueType: Type?
        explore = DExplore()
        json!!.asJsonObject.get("response").asJsonArray.forEach {
            val j = it.asJsonObject

            val order = j.get("order").asInt
            val type = j.get("type").asInt
            val title = j.get("title").asString
            val itemArray = j.get("items").asJsonArray

            when (type) {
                1 -> {
                    valueType = object : TypeToken<MutableList<Episode>>() {}.type
                    val item = EpisodeItem()
                    item.episodes = Gson().fromJson(itemArray, valueType)
                    item.type = type
                    item.title = title
                    item.order = order
                    explore.episodeItems.add(item)
                }

                2 -> {
                    valueType = object : TypeToken<MutableList<Podcast>>() {}.type
                    val item = PodcastItem()
                    item.podcasts = Gson().fromJson(itemArray, valueType)
                    item.type = type
                    item.title = title
                    item.order = order
                    explore.podcastItems.add(item)
                }
                3 -> {
                    valueType = object : TypeToken<MutableList<Category>>() {}.type
                    val item = CategoryItem()
                    item.categories = Gson().fromJson(itemArray, valueType)
                    item.type = type
                    item.title = title
                    item.order = order
                    explore.categoryItems.add(item)
                }
            }

        }


        return explore
    }
}