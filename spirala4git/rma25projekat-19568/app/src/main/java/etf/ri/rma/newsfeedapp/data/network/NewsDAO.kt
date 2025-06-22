package etf.ri.rma.newsfeedapp.data.network

import etf.ri.rma.newsfeedapp.data.NewsData
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.model.NewsItem
import etf.ri.rma.newsfeedapp.data.network.exception.InvalidUUIDException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class NewsDAO {

    private lateinit var apiService: NewsApiService
    private val allStories = mutableListOf<NewsItem>().apply {
        addAll(NewsData.getInitialNews())
    }

    private val cacheByCategory = HashMap<String, Pair<Long, List<NewsItem>>>()
    private val similarCache = mutableMapOf<String, List<NewsItem>>()

    fun setApiService(service: NewsApiService) {
        this.apiService = service
    }

    private fun mapToUserCategory(apiCategory: String): String = when (apiCategory.lowercase()) {
        "politics" -> "Politika"
        "sports" -> "Sport"
        "science", "tech" -> "Nauka/tehnologija"
        else -> apiCategory
    }



    private fun formatDate(publishedAt: String?): String {
        if (publishedAt == null) return "01-01-1970"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val datePart = publishedAt.substring(0, 10)
            outputFormat.format(inputFormat.parse(datePart)!!)
        } catch (e: Exception) {
            "01-01-1970"
        }
    }

    suspend fun getTopStoriesByCategory(category: String?): List<NewsItem> {
        if (category.isNullOrBlank()) throw IllegalArgumentException("Kategorija je null ili prazna")
        val safeCategory = category.lowercase(Locale.getDefault())

        val now = System.currentTimeMillis()
        val cached = cacheByCategory[safeCategory]
        if (cached != null && now - cached.first < 30_000) {
            return cached.second
        }

        val response = apiService.getTopStories(
            category = safeCategory,
            language = "en",
            token = "AtgJbTjArQaWowMg9XUNYL1Az79qbovVu6oWCQv4"
        )

        val fetched = response.data
            .filter { it.publishedAt != null }
            .take(3)
            .map {
                val bestCategory = resolveBestCategory(it.categories)
                NewsItem(
                    uuid = it.uuid,
                    title = it.title,
                    snippet = it.snippet,
                    imageUrl = it.imageUrl,
                    category = mapToUserCategory(bestCategory),
                    isFeatured = true,
                    source = it.source,
                    publishedDate = formatDate(it.publishedAt),
                    imageTags = arrayListOf()
                )
            }



        val fetchedIds = fetched.map { it.uuid }.toSet()
        val userCategory = fetched.firstOrNull()?.category ?: mapToUserCategory(safeCategory)

        fetched.forEach { newStory ->
            val index = allStories.indexOfFirst { it.uuid == newStory.uuid }
            if (index == -1) {
                allStories.add(0, newStory)
            } else {
                allStories[index] = allStories[index].copy(isFeatured = true)
            }
        }

        for (i in allStories.indices) {
            val story = allStories[i]
            if (story.category.equals(userCategory, ignoreCase = true) &&
                story.uuid !in fetchedIds &&
                story.isFeatured
            ) {
                allStories[i] = story.copy(isFeatured = false)
            }
        }

        cacheByCategory[safeCategory] = Pair(now, fetched)
        return fetched
    }


    suspend fun getSimilarStories(uuid: String): List<NewsItem> {
        if (!uuid.matches(Regex("^[a-fA-F0-9\\-]{36}$"))) {
            throw InvalidUUIDException()
        }

        similarCache[uuid]?.let { return it }

        val response = apiService.getSimilarStories(
            uuid = uuid,
            apiToken = "AtgJbTjArQaWowMg9XUNYL1Az79qbovVu6oWCQv4"
        )

        val fetched = response.data
            .filter { it.publishedAt != null }
            .take(2)
            .map {
                val bestCategory = it.categories.firstOrNull()?.lowercase() ?: "general"
                NewsItem(
                    uuid = it.uuid,
                    title = it.title,
                    snippet = it.snippet,
                    imageUrl = it.imageUrl,
                    category = mapToUserCategory(bestCategory),
                    isFeatured = false,
                    source = it.source,
                    publishedDate = formatDate(it.publishedAt),
                    imageTags = arrayListOf()
                )
            }


        fetched.forEach {
            if (allStories.none { s -> s.uuid == it.uuid }) {
                allStories.add(it)
            }
        }

        similarCache[uuid] = fetched
        return fetched
    }

    private fun resolveBestCategory(categories: List<String>?): String {
        if (categories == null) return "general"

        val lower = categories.map { it.lowercase(Locale.getDefault()) }

        val priority = listOf("politics", "sports", "science", "tech")
        return priority.firstOrNull { it in lower } ?: lower.firstOrNull() ?: "general"
    }




    fun getAllStories(): List<NewsItem> = allStories

    private fun absDiffDate(date1: String?, date2: String?): Long {
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return try {
            val d1 = format.parse(date1 ?: "")?.time ?: 0
            val d2 = format.parse(date2 ?: "")?.time ?: 0
            kotlin.math.abs(d1 - d2)
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
