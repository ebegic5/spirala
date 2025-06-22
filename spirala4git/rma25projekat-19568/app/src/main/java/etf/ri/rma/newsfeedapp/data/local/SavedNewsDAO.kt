package etf.ri.rma.newsfeedapp.data.local

import androidx.room.*
import etf.ri.rma.newsfeedapp.model.NewsItem

@Dao
interface SavedNewsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNews(news: NewsItem): Long

    @Query("SELECT * FROM News WHERE uuid = :uuid LIMIT 1")
    fun getByUuid(uuid: String): NewsItem?

    @Query("SELECT * FROM News")
    fun getAll(): List<NewsItem>

    @Query("SELECT * FROM News WHERE category = :category")
    fun getByCategory(category: String): List<NewsItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM Tags WHERE value = :value LIMIT 1")
    fun getTagByValue(value: String): TagEntity?

    @Insert
    fun insertNewsTag(cross: NewsTagEntity)

    @Query("SELECT Tags.value FROM Tags INNER JOIN NewsTags ON Tags.id = NewsTags.tagsId WHERE NewsTags.newsId = :newsId")
    fun getTagsForNews(newsId: Int): List<String>

    @Query("SELECT N.* FROM News N JOIN NewsTags NT ON N.id = NT.newsId JOIN Tags T ON NT.tagsId = T.id WHERE T.value IN (:tags) GROUP BY N.id ORDER BY N.published_at DESC")
    fun getNewsForTags(tags: List<String>): List<NewsItem>

    @Transaction
    fun saveNews(news: NewsItem): Boolean {
        if (getByUuid(news.uuid) != null) return false
        return insertNews(news) != -1L
    }

    @Transaction
    fun allNews(): List<NewsItem> {
        return getAll().map { it.copy(imageTags = ArrayList(getTagsForNews(it.id))) }
    }

    @Transaction
    fun getNewsWithCategory(category: String): List<NewsItem> {
        return getByCategory(category).map { it.copy(imageTags = ArrayList(getTagsForNews(it.id))) }
    }

    @Transaction
    fun addTags(tags: List<String>, newsId: Int): Int {
        var newCount = 0
        for (tagValue in tags) {
            val existing = getTagByValue(tagValue)
            val tagId = existing?.id ?: run {
                val id = insertTag(TagEntity(value = tagValue))
                if (id != -1L) newCount++
                id.toInt()
            }
            insertNewsTag(NewsTagEntity(newsId = newsId, tagsId = tagId))
        }
        return newCount
    }

    @Transaction
    fun getTags(newsId: Int): List<String> = getTagsForNews(newsId)

    @Transaction
    fun getSimilarNews(tags: List<String>): List<NewsItem> {
        return getNewsForTags(tags).map { it.copy(imageTags = ArrayList(getTagsForNews(it.id))) }
    }
}
