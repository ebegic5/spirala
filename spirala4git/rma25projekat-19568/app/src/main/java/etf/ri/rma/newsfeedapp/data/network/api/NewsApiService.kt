package etf.ri.rma.newsfeedapp.data.network.api

import etf.ri.rma.newsfeedapp.model.NewsApiItem
import retrofit2.http.GET
import retrofit2.http.Query
import etf.ri.rma.newsfeedapp.model.NewsItem
import retrofit2.http.Path

interface NewsApiService {
    @GET("v1/news/top")
    suspend fun getTopStories(
        @Query("categories") category: String,
        @Query("language") language: String,
        @Query("api_token") token: String
    ): NewsResponse

    @GET("v1/news/similar/{uuid}")
    suspend fun getSimilarStories(
        @Path("uuid") uuid: String,
        @Query("api_token") apiToken: String
    ): NewsResponse


}

data class NewsResponse(val data: List<NewsApiItem>)

