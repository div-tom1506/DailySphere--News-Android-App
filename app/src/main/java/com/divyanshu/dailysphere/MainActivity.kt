package com.divyanshu.dailysphere

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.divyanshu.dailysphere.adapter.NewsAdapter
import com.divyanshu.dailysphere.model.NewsResponse
import com.divyanshu.dailysphere.network.RetrofitInstance


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

        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerCountry = findViewById(R.id.spinnerCountry)
        recyclerView = findViewById(R.id.recyclerView)
        val searchView: SearchView = findViewById(R.id.searchView)

        spinnerLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages.keys.toList())
        spinnerCountry.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries.keys.toList())

        recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(listOf())
        recyclerView.adapter = newsAdapter

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty())
                    fetchNews(query, isNewSearch = true)
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })

        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (lm.findLastVisibleItemPosition() == newsAdapter.itemCount - 1 && !isLoading) {
                    currentPage++
                    fetchNews(currentQuery)
                }
            }
        })

        fetchNews(currentQuery, isNewSearch = true)
    }

    private fun fetchNews(query: String, isNewSearch: Boolean = false) {
        isLoading = true
        if(isNewSearch) {
            currentPage = 1
            currentQuery = query
        }

        val lang = languages[spinnerLanguage.selectedItem.toString()]?: "en"
        val country = countries[spinnerCountry.selectedItem.toString()]?: "us"

        RetrofitInstance.api.getNews(apiKey, query, lang, country, currentPage)
            .enqueue(object: Callback<NewsResponse> {
                override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                    if(response.isSuccessful) {
                        val newArticles = response.body()?.results ?: emptyList()
                        if(isNewSearch)
                            newsAdapter.updateNews(newArticles)
                        else
                            newsAdapter.appendNews(newArticles)
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

