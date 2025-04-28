package com.divyanshu.dailysphere

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.divyanshu.dailysphere.adapter.NewsAdapter
import com.divyanshu.dailysphere.model.NewsResponse
import com.divyanshu.dailysphere.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerCountry: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private var currentPage = 1
    private var isLoading = false
    private var currentQuery = "top"
    private var selectedLanguage = "en"
    private var selectedCountry = "in"

    // to be removed
    private val apiKey = ""

    private val categories = listOf(
        "Top Headlines", "Technology", "Sports", "Business", "Health", "Science", "Entertainment"
    )

    private val languages = listOf(
        "English (en)", "Hindi (hi)", "Spanish (es)", "French (fr)", "German (de)"
    )

    private val countries = listOf(
        "USA (us)", "India (in)", "UK (gb)", "Canada (ca)", "Australia (au)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerCountry = findViewById(R.id.spinnerCountry)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSpinners()
        setupSearchView()
        setupSwipeRefresh()

        fetchNews(currentQuery, isNewSearch = true)

        setupPagination()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(listOf())
        recyclerView.adapter = newsAdapter
    }

    private fun setupSpinners() {
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter

        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = countryAdapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentQuery = when (categories[position]) {
                    "Top Headlines" -> "top"
                    else -> categories[position].lowercase()
                }
                fetchNews(currentQuery, isNewSearch = true)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedLanguage = languages[position].takeLast(3).replace("(", "").replace(")", "")
                fetchNews(currentQuery, isNewSearch = true)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCountry = countries[position].takeLast(3).replace("(", "").replace(")", "")
                fetchNews(currentQuery, isNewSearch = true)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    currentQuery = query
                    fetchNews(currentQuery, isNewSearch = true)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            fetchNews(currentQuery, isNewSearch = true)
        }
    }

    private fun setupPagination() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)

                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()

                if (!isLoading && lastVisibleItem == newsAdapter.itemCount - 1) {
                    currentPage++
                    fetchNews(currentQuery, isNewSearch = false)
                }
            }
        })
    }

    private fun fetchNews(query: String, isNewSearch: Boolean) {
        isLoading = true
        if (isNewSearch) {
            currentPage = 1
            showLoading(true)
        }

        RetrofitInstance.api.getNews(
            apiKey = apiKey,
            query = query,
            language = selectedLanguage,
            country = selectedCountry,
            page = currentPage
        ).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newArticles = response.body()?.results ?: emptyList()
                    if (isNewSearch) {
                        newsAdapter.updateNews(newArticles)
                    } else {
                        newsAdapter.appendNews(newArticles)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch news", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
                showLoading(false)
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                isLoading = false
                swipeRefreshLayout.isRefreshing = false
                showLoading(false)
                Toast.makeText(this@MainActivity, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}