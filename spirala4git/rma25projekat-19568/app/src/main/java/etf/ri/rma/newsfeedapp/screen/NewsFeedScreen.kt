package etf.ri.rma.newsfeedapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import etf.ri.rma.newsfeedapp.data.AppModule
import etf.ri.rma.newsfeedapp.data.FilterState
import etf.ri.rma.newsfeedapp.model.NewsItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NewsFeedScreen(navController: NavController) {
    val selectedCategoryState = remember { mutableStateOf(FilterState.category) }
    val scope = rememberCoroutineScope()
    var featuredNews by remember { mutableStateOf<List<NewsItem>>(emptyList()) }

    val selectedCategory = selectedCategoryState.value
    val dateFrom = FilterState.dateFrom
    val dateTo = FilterState.dateTo
    val unwantedWords = FilterState.unwantedWords

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != "Sve") {
            scope.launch {
                try {
                    val categoryParam = when (selectedCategory) {
                        "Nauka/tehnologija" -> "science,tech"
                        "Politika" -> "politics"
                        "Sport" -> "sports"
                        else -> ""
                    }

                    if (categoryParam.isNotBlank()) {
                        val combined = AppModule.newsDAO.getTopStoriesByCategory(categoryParam)
                        combined.forEach { AppModule.savedNewsDAO.saveNews(it) }
                        featuredNews = combined.shuffled().take(3)
                    }
                } catch (e: Exception) {
                    println("Greska u fetchu: ${e.message}")
                }
            }
        }
    }


    val allNews = AppModule.savedNewsDAO.allNews()

    val filteredNews: List<NewsItem> = when (selectedCategory) {
        "Sve" -> allNews
            .sortedByDescending { parseDate(it.publishedDate) }
            .map { it.copy(isFeatured = false) }

        else -> {
            val userCategories = when (selectedCategory) {
                "Nauka/tehnologija" -> listOf("Nauka/tehnologija")
                else -> listOf(selectedCategory)
            }

            val fromCategory = allNews.filter {
                userCategories.any { cat -> it.category.equals(cat, ignoreCase = true) }
            }

            val featuredIds = featuredNews.map { it.uuid }.toSet()

            val nonFeatured = fromCategory.filter { it.uuid !in featuredIds }
                .map { it.copy(isFeatured = false) }

            featuredNews + nonFeatured
        }
    }.filter { news ->
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val newsDate = try { sdf.parse(news.publishedDate)?.time } catch (_: Exception) { null }
        val from = startOfDay(dateFrom)
        val to = endOfDay(dateTo)
        (from == null || (newsDate ?: 0) >= from) &&
                (to == null || (newsDate ?: 0) <= to)
    }.filter { news ->
        unwantedWords.none { uw ->
            news.title.contains(uw, ignoreCase = true) ||
                    news.snippet.contains(uw, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        FilterSection(
            selected = selectedCategory,
            onCategorySelected = {
                selectedCategoryState.value = it
                FilterState.category = it
            },
            onMoreFilters = { navController.navigate("/filters") },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (filteredNews.isEmpty()) {
            MessageCard("Nema pronađenih vijesti u kategoriji \"$selectedCategory\"")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("news_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNews) { news ->
                    val onClick = {
                        navController.navigate("/details/${news.uuid}")
                    }
                    if (selectedCategory == "Sve") {
                        StandardNewsCard(newsItem = news.copy(isFeatured = false), onClick = onClick)
                    } else {
                        if (featuredNews.any { it.uuid == news.uuid })
                            FeaturedNewsCard(newsItem = news.copy(isFeatured = true), onClick = onClick)
                        else
                            StandardNewsCard(newsItem = news.copy(isFeatured = false), onClick = onClick)
                    }
                }
            }
        }
    }
}

fun startOfDay(millis: Long?): Long? = millis?.let {
    Calendar.getInstance().apply {
        timeInMillis = it
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun endOfDay(millis: Long?): Long? = millis?.let {
    Calendar.getInstance().apply {
        timeInMillis = it
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

fun parseDate(date: String?): Long {
    return try {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(date ?: "")?.time ?: 0
    } catch (e: Exception) {
        0
    }
}

@Composable
fun FilterSection(
    selected: String,
    onCategorySelected: (String) -> Unit,
    onMoreFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "Sve" to "filter_chip_all",
        "Politika" to "filter_chip_pol",
        "Sport" to "filter_chip_spo",
        "Nauka/tehnologija" to "filter_chip_sci"
    )

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        categories.forEach { (label, tag) ->
            FilterChip(
                selected = selected == label,
                onClick = { if (selected != label) onCategorySelected(label) },
                label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier.testTag(tag).height(40.dp),
                shape = MaterialTheme.shapes.medium,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == label,
                    borderColor = MaterialTheme.colorScheme.outline,
                    borderWidth = 1.dp
                )
            )
        }

        FilterChip(
            selected = false,
            onClick = { onMoreFilters() },
            label = { Text("Više filtera ...", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier.testTag("filter_chip_more").height(40.dp),
            shape = MaterialTheme.shapes.medium,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = MaterialTheme.colorScheme.outline,
                borderWidth = 1.dp
            )
        )
    }
}
