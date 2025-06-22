package etf.ri.rma.newsfeedapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import etf.ri.rma.newsfeedapp.data.FilterState
import etf.ri.rma.newsfeedapp.data.AppModule
import etf.ri.rma.newsfeedapp.ui.theme.NewsFeedAppTheme
import etf.ri.rma.newsfeedapp.screen.NewsNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppModule.init(this)
        FilterState.reset()
        setContent {
            NewsFeedAppTheme {
                NewsNavGraph()
            }
        }
    }
}
