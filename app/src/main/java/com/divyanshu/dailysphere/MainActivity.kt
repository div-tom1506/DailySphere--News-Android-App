package com.divyanshu.dailysphere

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.divyanshu.dailysphere.adapter.CategoryAdapter
import com.divyanshu.dailysphere.adapter.NewsAdapter
import com.divyanshu.dailysphere.model.CategoryItem
import com.divyanshu.dailysphere.model.NewsResponse
import com.divyanshu.dailysphere.network.RetrofitInstance
import com.divyanshu.dailysphere.worker.BreakingNewsWorkScheduler
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var noInternetLayout: LinearLayout
    private lateinit var retryButton: Button
    private lateinit var scrollToTopBtn: ImageButton
    private lateinit var settingsButton: ImageButton

    private var nextPageToken: String? = null
    private var isLoading = false
    private var currentQuery = "top"
    private var selectedLanguage = "en"
    private var selectedCountry: String? = null

    private val apiKey = BuildConfig.NEWS_API_KEY

    private val categories = listOf(
        CategoryItem("Top Headlines", true),
        CategoryItem("World"),
        CategoryItem("Sports"),
        CategoryItem("Politics"),
        CategoryItem("Education"),
        CategoryItem("Business"),
        CategoryItem("Entertainment"),
        CategoryItem("Technology"),
        CategoryItem("Health"),
        CategoryItem("Science"),
        CategoryItem("Tourism")
    )
    private val languages = listOf("English (en)", "Hindi (hi)")
    private val countries =
        listOf("", "India (in)", "USA (us)", "UK (gb)", "Canada (ca)", "Australia (au)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Requesting Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        // for scheduling notification worker
        BreakingNewsWorkScheduler.scheduleBreakingNewsWorker(this)
        // for immediate testing the notification worker
//         BreakingNewsWorkScheduler.triggerOneTimeBreakingNewsWorker(this)

        Log.d("MainActivity", "onCreate: Initializing views")

        searchView = findViewById(R.id.editTextSearch)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        noInternetLayout = findViewById(R.id.noInternetLayout)
        retryButton = findViewById(R.id.retryButton)
        scrollToTopBtn = findViewById(R.id.scrollToTopBtn)
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        settingsButton = findViewById(R.id.settingsButton)

        setupRecyclerView()
        setupCategoryRecyclerView()
        setupSettingsDialog()
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

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            currentQuery =
                if (selectedCategory == "Top Headlines") "top" else selectedCategory.lowercase()
            fetchNews(currentQuery, isNewSearch = true)
        }
        categoryRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryRecyclerView.adapter = categoryAdapter
    }

    private fun setupSettingsDialog() {
        settingsButton.setOnClickListener {
            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.dialog_language_country, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Select Language & Country")
                .setPositiveButton("Apply") { _, _ ->
                    val spinnerLang = dialogView.findViewById<Spinner>(R.id.dialogSpinnerLanguage)
                    val spinnerCountry = dialogView.findViewById<Spinner>(R.id.dialogSpinnerCountry)
                    selectedLanguage =
                        languages[spinnerLang.selectedItemPosition].takeLast(3).replace("(", "")
                            .replace(")", "")
                    selectedCountry =
                        if (spinnerCountry.selectedItemPosition == 0) null else countries[spinnerCountry.selectedItemPosition].takeLast(
                            3
                        ).replace("(", "").replace(")", "")
                    fetchNews(currentQuery, isNewSearch = true)
                }
                .setNegativeButton("Cancel", null)
                .create()

            val spinnerLang = dialogView.findViewById<Spinner>(R.id.dialogSpinnerLanguage)
            val spinnerCountry = dialogView.findViewById<Spinner>(R.id.dialogSpinnerCountry)

            spinnerLang.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, languages).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            spinnerCountry.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, countries).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

            dialog.show()
        }
    }

    private fun setupSearchView() {
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchView.text.toString().trim()
                if (query.isNotEmpty()) {
                    currentQuery = query
                    fetchNews(currentQuery, isNewSearch = true)
                }
                true
            } else {
                false
            }
        }
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

                if (!isLoading && lastVisibleItem == newsAdapter.itemCount - 1 && !nextPageToken.isNullOrEmpty()) {
                    Log.d("MainActivity", "Fetching next page: $nextPageToken")
                    fetchNews(currentQuery, isNewSearch = false)
                }

                // Scroll to top button animation
                if (layoutManager.findFirstVisibleItemPosition() > 5) {
                    if (!scrollToTopBtn.isVisible) {
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
