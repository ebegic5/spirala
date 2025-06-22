package etf.ri.rma.newsfeedapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "News")
data class NewsItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String,
    val title: String,
    val snippet: String,
    @SerializedName("image_url")
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    val category: String,
    val isFeatured: Boolean,
    val source: String,
    @SerializedName("published_at")
    @ColumnInfo(name = "published_at")
    val publishedDate: String,
    @Ignore
    val imageTags: ArrayList<String> = arrayListOf()
)
