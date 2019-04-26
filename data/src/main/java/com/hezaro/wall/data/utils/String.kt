package com.hezaro.wall.data.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun String.isJson(): Boolean {
    try {
        JSONObject(this)
    } catch (ex: JSONException) {
        try {
            JSONArray(this)
        } catch (ex1: JSONException) {
            return false
        }
    }

    return true
}