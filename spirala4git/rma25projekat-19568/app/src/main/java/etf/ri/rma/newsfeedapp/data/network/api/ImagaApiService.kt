package etf.ri.rma.newsfeedapp.data.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Headers

interface ImagaApiService {
    @Headers(
        "Authorization: Basic YWNjXzY2NTZhODcwZDc1MjBmNTo4M2Q2YjA4NjE5MWI2ZTBjY2Q4MDgyMjA4MTJjNjQ3Zg=="
    )
    @GET("v2/tags")
    suspend fun getTags(
        @Query("image_url") imageUrl: String
    ): ImaggaResponse
}

data class ImaggaResponse(val result: ResultData)
data class ResultData(val tags: List<TagItem>)
data class TagItem(val tag: Map<String, String>, val confidence: Double)
