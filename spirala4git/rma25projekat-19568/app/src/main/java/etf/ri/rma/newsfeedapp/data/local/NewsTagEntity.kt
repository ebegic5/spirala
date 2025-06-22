package etf.ri.rma.newsfeedapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "NewsTags")
data class NewsTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val newsId: Int,
    val tagsId: Int
)
