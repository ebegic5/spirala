package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import etf.ri.rma.newsfeedapp.model.NewsItem

@Composable
fun NewsList(
    newsItems: List<NewsItem>,
    onNewsClick: (NewsItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news_list"),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(newsItems, key = { it.uuid }) { newsItem ->
            if (newsItem.isFeatured) {
                FeaturedNewsCard(newsItem = newsItem, onClick = { onNewsClick(newsItem) })
            } else {
                StandardNewsCard(newsItem = newsItem, onClick = { onNewsClick(newsItem) })
            }
        }
    }
}

