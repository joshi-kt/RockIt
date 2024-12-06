package com.example.rockit.data.network

import retrofit2.Retrofit
import com.example.rockit.Utils.Utils.BASE_URL
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    val retrofitService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}