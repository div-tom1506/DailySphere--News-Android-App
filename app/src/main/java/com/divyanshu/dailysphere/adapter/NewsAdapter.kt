package com.divyanshu.dailysphere.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.divyanshu.dailysphere.R
import com.divyanshu.dailysphere.model.Article
import com.divyanshu.dailysphere.WebViewActivity

class NewsAdapter(private var articles: List<Article>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.newsTitle)
        val pubDate: TextView = view.findViewById(R.id.newsPubDate)
        val desc: TextView = view.findViewById(R.id.newsDescription)
        val image: ImageView = view.findViewById(R.id.newsImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.title.text = article.title
        holder.pubDate.text = article.pubDate ?: "Unknown Date"
        holder.desc.text = article.description ?: "No description available"
        Glide.with(holder.itemView.context)
            .load(article.imageUrl)
            .into(holder.image)

        // Handle item click and open WebViewActivity
        holder.itemView.setOnClickListener {
            article.link?.let { url ->
                val intent = Intent(holder.itemView.context, WebViewActivity::class.java)
                intent.putExtra("URL", url) // Pass the URL to WebViewActivity
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = articles.size

    fun updateNews(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    fun appendNews(newArticles: List<Article>) {
        articles = articles + newArticles
        notifyDataSetChanged()
    }
}
