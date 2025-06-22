package etf.ri.rma.newsfeedapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object FilterState {
    var category: String = "Sve"
    var dateFrom: Long? = null
    var dateTo: Long? = null
    var unwantedWords: List<String> = emptyList()

    fun reset() {
        category = "Sve"
        dateFrom = null
        dateTo = null
        unwantedWords = emptyList()
    }
}

