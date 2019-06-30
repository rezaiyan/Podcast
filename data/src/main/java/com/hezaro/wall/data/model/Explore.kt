package com.hezaro.wall.data.model

import com.google.gson.annotations.JsonAdapter
import com.hezaro.wall.data.model.type_converter.ExploreTypeAdapterFactory

@JsonAdapter(value = ExploreTypeAdapterFactory::class)
class Explore(val response: MutableList<ExploreItem<Any>>)

sealed class ExploreItem<T>(
    var order: Int = -1,
    var type: String = "undefined",
    var title: String = "",
    var items: MutableList<T> = mutableListOf()
) {

    class Best : ExploreItem<Episode>()
    class Last : ExploreItem<Episode>()
    class Recommended : ExploreItem<Episode>()
    class Podcast : ExploreItem<com.hezaro.wall.data.model.Podcast>()
    class Category : ExploreItem<CategoryItem>()
}

data class CategoryItem(var id: Int, var title: String, var title_fa: String, var count: Int, var description: String)
