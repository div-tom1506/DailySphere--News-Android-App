package com.divyanshu.dailysphere

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.divyanshu.dailysphere.adapter.NewsAdapter
import com.divyanshu.dailysphere.model.NewsResponse
import com.divyanshu.dailysphere.network.RetrofitInstance
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerCountry: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noInternetLayout: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var scrollToTopBtn: ImageButton

    private var nextPageToken: String? = null
    private var isLoading = false
    private var currentQuery = "top"
    private var selectedLanguage = "en"
    private var selectedCountry: String? = null

    private val apiKey = BuildConfig.NEWS_API_KEY

    private val categories = listOf(
        "Top Headlines",
        "Technology",
        "Sports",
        "Business",
        "Health",
        "Science",
        "Entertainment"
    )
    private val languages = listOf("English (en)", "Hindi (hi)")
    private val countries =
        listOf("", "India (in)", "USA (us)", "UK (gb)", "Canada (ca)", "Australia (au)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate: Initializing views")

        searchView = findViewById(R.id.searchView)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerCountry = findViewById(R.id.spinnerCountry)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        noInternetLayout = findViewById(R.id.noInternetLayout)
        retryButton = findViewById(R.id.retryButton)
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn)

        setupRecyclerView()
        setupSpinners()
        setupSearchView()
        setupSwipeRefresh()
        setupPagination()
        setupScrollToTop()

        retryButton.setOnClickListener {
            Log.d("MainActivity", "Retry button clicked")
            fetchNews(currentQuery, isNewSearch = true)
        }

        fetchNews(currentQuery, isNewSearch = true)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(listOf())
        recyclerView.adapter = newsAdapter
        Log.d("MainActivity", "RecyclerView set up")
    }

    private fun setupSpinners() {
        spinnerCategory.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, categories).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        spinnerLanguage.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, languages).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        spinnerCountry.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, countries).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentQuery =
                    if (categories[position] == "Top Headlines") "top" else categories[position].lowercase()
                Log.d("MainActivity", "Category selected: $currentQuery")
                fetchNews(currentQuery, isNewSearch = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = languages[position].takeLast(3).replace("(", "").replace(")", "")
                Log.d("MainActivity", "Language selected: $selectedLanguage")
                fetchNews(currentQuery, isNewSearch = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCountry =
                    if (position == 0) null else countries[position].takeLast(3).replace("(", "")
                        .replace(")", "")
                Log.d("MainActivity", "Country selected: $selectedCountry")
                fetchNews(currentQuery, isNewSearch = true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    currentQuery = it
                    Log.d("MainActivity", "Search submitted: $currentQuery")
                    fetchNews(currentQuery, isNewSearch = true)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d("MainActivity", "Swipe to refresh triggered")
            fetchNews(currentQuery, isNewSearch = true)
        }
    }

    private fun setupPagination() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()

                if (!isLoading && lastVisibleItem == newsAdapter.itemCount - 1 && nextPageToken != null) {
                    Log.d("MainActivity", "Fetching next page: $nextPageToken")
                    fetchNews(currentQuery, isNewSearch = false)
                }

                // Scroll to top button animation
                if (layoutManager.findFirstVisibleItemPosition() > 5) {
                    if (scrollToTopBtn.visibility != View.VISIBLE) {
                        scrollToTopBtn.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .withStartAction { scrollToTopBtn.visibility = View.VISIBLE }
                            .start()
                    }
                } else {
                    if (scrollToTopBtn.isVisible) {
                        scrollToTopBtn.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction { scrollToTopBtn.visibility = View.GONE }
                            .start()
                    }
                }
            }
        })
    }

    private fun setupScrollToTop() {
        scrollToTopBtn.setOnClickListener {
            Log.d("MainActivity", "Scroll to top clicked")
            recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun fetchNews(query: String, isNewSearch: Boolean) {
        Log.d(
            "MainActivity",
            "Fetching news: query=$query, newSearch=$isNewSearch, pageToken=$nextPageToken"
        )

        if (!isNetworkAvailable()) {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            swipeRefreshLayout.visibility = View.GONE
            noInternetLayout.visibility = View.VISIBLE
            Log.e("NewsFetch", "No internet connection")
            return
        }

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()
        noInternetLayout.visibility = View.GONE
        swipeRefreshLayout.visibility = View.VISIBLE

        isLoading = true
        if (isNewSearch) nextPageToken = null

        val call = RetrofitInstance.api.getNews(
            apiKey = apiKey,
            query = query,
            language = selectedLanguage,
            country = selectedCountry,
            page = nextPageToken
        )

        Log.d(
            "NewsFetch",
            "Fetching news: query=$query, lang=$selectedLanguage, country=$selectedCountry, page=$nextPageToken"
        )

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                isLoading = false

                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    val newArticles = newsResponse?.results ?: emptyList()
                    nextPageToken = newsResponse?.nextPage
                    Log.d(
                        "MainActivity",
                        "Fetched ${newArticles.size} articles, nextPageToken=$nextPageToken"
                    )
                    if (isNewSearch) {
                        newsAdapter.updateNews(newArticles)
                    } else {
                        newsAdapter.appendNews(newArticles)
                    }
                } else {
                    Log.e(
                        "NewsFetch",
                        "Response failed: code=${response.code()}, message=${response.message()}"
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch news. Code: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                isLoading = false
                Log.e("NewsFetch", "Error: ${t.localizedMessage}", t)
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong. Check logs.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
