package etf.ri.rma.newsfeedapp.model

import com.google.gson.annotations.SerializedName

data class NewsApiItem(
    val uuid: String,
    val title: String,
    val snippet: String,
    @SerializedName("image_url") val imageUrl: String?,
    val categories: List<String>,
    val source: String,
    @SerializedName("published_at") val publishedAt: String
)
