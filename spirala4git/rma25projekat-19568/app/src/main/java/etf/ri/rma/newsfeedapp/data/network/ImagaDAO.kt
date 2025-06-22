package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidImageURLException

class ImagaDAO {
    private lateinit var apiService: ImagaApiService
    private val cache = mutableMapOf<String, List<String>>()

    fun setApiService(service: ImagaApiService) {
        this.apiService = service
    }

    suspend fun getTags(imageURL: String): List<String> {
        if (!imageURL.startsWith("http")) throw InvalidImageURLException()
        cache[imageURL]?.let { return it }

        val response = apiService.getTags(imageURL)
        val tags = response.result.tags.mapNotNull { it.tag["en"] }

        cache[imageURL] = tags
        return tags
    }
}
