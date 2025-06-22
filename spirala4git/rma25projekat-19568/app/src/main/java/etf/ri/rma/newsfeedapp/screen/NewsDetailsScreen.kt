package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import etf.ri.rma.newsfeedapp.data.AppModule
import etf.ri.rma.newsfeedapp.model.NewsItem
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewsDetailsScreen(navController: NavController, newsId: String) {
    val allNews = AppModule.savedNewsDAO.allNews()
    val news = allNews.find { it.uuid == newsId }

    if (news == null) {
        Text("Vijest nije pronađena")
        return
    }

    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var similar by remember { mutableStateOf<List<NewsItem>>(emptyList()) }

    LaunchedEffect(news.imageUrl) {
        val cached = AppModule.savedNewsDAO.getTags(news.id)
        if (cached.isNotEmpty()) {
            tags = cached
        } else {
            news.imageUrl?.let { url ->
                try {
                    val fetched = AppModule.imagaDAO.getTags(url)
                    tags = fetched
                    AppModule.savedNewsDAO.addTags(fetched, news.id)
                    println(">>> TAGOVI ZA $url: $fetched")
                } catch (e: Exception) {
                    println(">>> GRESKA U TAGOVIMA: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(news.uuid) {
        try {
            similar = AppModule.newsDAO.getSimilarStories(news.uuid)
            println(">>> Similar stories: ${'$'}{similar.size}")
        } catch (_: Exception) {
            val cachedTags = AppModule.savedNewsDAO.getTags(news.id).take(2)
            if (cachedTags.isNotEmpty()) {
                similar = AppModule.savedNewsDAO.getSimilarNews(cachedTags)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(news.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("details_title"))
        Spacer(Modifier.height(4.dp))
        news.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "slika vijesti",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = news.snippet,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
            modifier = Modifier.testTag("details_snippet")
        )

        Spacer(Modifier.height(4.dp))
        Text(news.category, modifier = Modifier.testTag("details_category"))
        Spacer(Modifier.height(2.dp))
        Text(news.source, modifier = Modifier.testTag("details_source"))
        Spacer(Modifier.height(2.dp))
        Text(news.publishedDate, modifier = Modifier.testTag("details_date"))

        Spacer(Modifier.height(16.dp))

        if (similar.isNotEmpty()) {
            Text("Slične vijesti:", style = MaterialTheme.typography.titleSmall)
            similar.forEachIndexed { idx, rNews ->
                Text(
                    rNews.title,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            navController.navigate("/details/${rNews.uuid}") {
                                popUpTo("/details/${news.uuid}") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        .testTag("related_news_title_${idx + 1}")
                )
            }
        } else {
            Text("Nema sličnih vijesti.", style = MaterialTheme.typography.bodySmall)
        }

        if (tags.isNotEmpty()) {
            Text(
                text = "Tagovi slike:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.take(5).forEach { tag ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = tag,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }


        Spacer(Modifier.weight(1f))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_close_button")
        ) {
            Text("Zatvori")
        }
    }
}



fun absDiffDate(date1: String, date2: String): Long {
    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return try {
        val d1 = format.parse(date1)
        val d2 = format.parse(date2)
        kotlin.math.abs((d1?.time ?: 0) - (d2?.time ?: 0))
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}
