package com.divyanshu.dailysphere

import android.os.Bundle
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.RecyclerView
import com.divyanshu.dailysphere.adapter.NewsAdapter
import com.divyanshu.dailysphere.ui.theme.DailySphereTheme
import org.intellij.lang.annotations.Language

class MainActivity : ComponentActivity() {

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerCountry: Spinner

    private var currentPage = 1
    private var isLoading = false
    private var currentQuery = "technology"

    private val apiKey = ""

    private val languages = mapOf("English" to "en", "Hindi" to "hi", "Spanish" to "es")
    private val countries = mapOf("United States" to "us", "India" to "in", "Germany" to "de")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }
}