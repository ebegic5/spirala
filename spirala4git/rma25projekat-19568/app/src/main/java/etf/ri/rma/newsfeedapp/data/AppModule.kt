package etf.ri.rma.newsfeedapp.data

import android.content.Context
import etf.ri.rma.newsfeedapp.data.network.NewsDAO
import etf.ri.rma.newsfeedapp.data.network.ImagaDAO
import etf.ri.rma.newsfeedapp.data.network.api.ImagaApiService
import etf.ri.rma.newsfeedapp.data.network.api.NewsApiService
import etf.ri.rma.newsfeedapp.data.local.NewsDatabase
import etf.ri.rma.newsfeedapp.data.local.SavedNewsDAO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {
    lateinit var database: NewsDatabase
        private set

    fun init(context: Context) {
        database = NewsDatabase.getDatabase(context)
    }

    val newsDAO: NewsDAO by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.thenewsapi.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val api = retrofit.create(NewsApiService::class.java)
        NewsDAO().apply { setApiService(api) }
    }

    val imagaDAO: ImagaDAO by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imagga.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ImagaApiService::class.java)
        ImagaDAO().apply { setApiService(api) }
    }

    val savedNewsDAO: SavedNewsDAO by lazy { database.savedNewsDAO() }
}
