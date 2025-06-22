package etf.ri.rma.newsfeedapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import etf.ri.rma.newsfeedapp.model.NewsItem

@Database(entities = [NewsItem::class, TagEntity::class, NewsTagEntity::class], version = 1)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun savedNewsDAO(): SavedNewsDAO

    companion object {
        @Volatile
        private var INSTANCE: NewsDatabase? = null

        fun getDatabase(context: Context): NewsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NewsDatabase::class.java,
                    "news-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
