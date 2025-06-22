package etf.ri.rma.newsfeedapp.model
import com.google.gson.annotations.SerializedName


data class NewsItem(
    val uuid: String,
    val title: String,
    val snippet: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val category: String,
    val isFeatured: Boolean,
    val source: String,
    @SerializedName("published_at")
    val publishedDate: String,
    val imageTags: ArrayList<String> = arrayListOf()
)
