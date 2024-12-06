package com.example.rockit.data.network

import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class JSONObjectConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return if (type == JSONObject::class.java) {
            Converter<ResponseBody, JSONObject> { responseBody ->
                try {
                    JSONObject(responseBody.string())
                } catch (e: JSONException) {
                    throw IOException("Failed to parse JSON", e)
                } finally {
                    responseBody.close()
                }
            }
        } else {
            null
        }
    }
}