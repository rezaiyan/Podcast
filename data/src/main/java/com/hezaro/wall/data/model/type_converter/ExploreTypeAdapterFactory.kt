package com.hezaro.wall.data.model.type_converter

import com.google.gson.JsonElement
import com.hezaro.wall.data.model.Explore

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 3:59 PM.
 */
internal class ExploreTypeAdapterFactory private constructor() :
    CustomizedTypeAdapterFactory<Explore>(Explore::class.java) {

    override fun beforeWrite(source: Explore, toSerialize: JsonElement) {
//        val custom = toSerialize.asJsonObject.get("custom").asJsonObject
//        custom.add("size", JsonPrimitive(custom.entrySet().size))
    }

    override fun afterRead(deserialized: JsonElement) {
//        val custom = deserialized.asJsonObject.get("custom").asJsonObject
//        custom.remove("size")
    }
}
