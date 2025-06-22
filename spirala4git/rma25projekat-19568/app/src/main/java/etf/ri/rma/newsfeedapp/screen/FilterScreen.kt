package etf.ri.rma.newsfeedapp.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import etf.ri.rma.newsfeedapp.data.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FilterScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf(FilterState.category) }
    var dateFrom by remember { mutableStateOf(FilterState.dateFrom) }
    var dateTo by remember { mutableStateOf(FilterState.dateTo) }
    var unwantedWords by remember { mutableStateOf(FilterState.unwantedWords) }
    var unwantedWordInput by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Politika", "Sport", "Nauka/tehnologija", "Sve").forEach { cat ->
                val tag = when(cat) {
                    "Politika" -> "filter_chip_pol"
                    "Sport" -> "filter_chip_spo"
                    "Nauka/tehnologija" -> "filter_chip_sci"
                    "Sve" -> "filter_chip_all"
                    else -> ""
                }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    modifier = Modifier.testTag(tag)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (dateFrom != null && dateTo != null)
                    dateFormat.format(Date(dateFrom!!)) + " - " + dateFormat.format(Date(dateTo!!))
                else
                    "Odaberi opseg datuma",
                modifier = Modifier.testTag("filter_daterange_display")
            )
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.testTag("filter_daterange_button")
            ) {
                Text("Odaberi")
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDateRangePickerState()
            Dialog(onDismissRequest = { showDatePicker = false }) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    DateRangePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .padding(4.dp)
                            .heightIn(max = 500.dp),
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp, vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Odaberite vremenski period",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        headline = {
                            val startDate = datePickerState.selectedStartDateMillis?.let {
                                dateFormat.format(Date(it))
                            } ?: "Početni datum"

                            val endDate = datePickerState.selectedEndDateMillis?.let {
                                dateFormat.format(Date(it))
                            } ?: "Krajnji datum"

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$startDate - $endDate",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Otkaži")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            val start = datePickerState.selectedStartDateMillis
                            val end = datePickerState.selectedEndDateMillis

                            if (start != null && end != null) {
                                dateFrom = start
                                dateTo = end
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = unwantedWordInput,
                onValueChange = { unwantedWordInput = it },
                label = { Text("Nepotrebna riječ") },
                modifier = Modifier.weight(1f).testTag("filter_unwanted_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val word = unwantedWordInput.trim()
                    if (word.isNotBlank() &&
                        unwantedWords.none { it.equals(word, ignoreCase = true) }) {
                        unwantedWords = unwantedWords + word
                        unwantedWordInput = ""
                    }
                },
                modifier = Modifier.testTag("filter_unwanted_add_button")
            ) {
                Text("Dodaj")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp)
                .testTag("filter_unwanted_list")
        ) {
            items(unwantedWords) { word ->
                Text(word)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                FilterState.category = selectedCategory
                FilterState.dateFrom = dateFrom
                FilterState.dateTo = dateTo
                FilterState.unwantedWords = unwantedWords
                navController.navigate("/home") {
                    popUpTo("/home") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("filter_apply_button")
        ) {
            Text("Primijeni filtere")
        }
    }
}
