package com.example.data.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface WikipediaService {
    @GET("page/summary/{title}")
    suspend fun getPlayerSummary(
        @Path("title") title: String
    ): WikipediaResponse

    companion object {
        private const val BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

        fun create(): WikipediaService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
            return retrofit.create(WikipediaService::class.java)
        }
    }
}
