package com.example.movieexplorer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var movieRecyclerView: RecyclerView
    private lateinit var loadMoviesButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var statusText: TextView

    private val movies = mutableListOf<Movie>()
    private lateinit var movieAdapter: MovieAdapter
    private val client = OkHttpClient()

    // Replace this with your own TMDB API key.
    private val apiKey = "62901cd8f99b78a774750c1d90ae723c"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movieRecyclerView = findViewById(R.id.movieRecyclerView)
        loadMoviesButton = findViewById(R.id.loadMoviesButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        statusText = findViewById(R.id.statusText)

        movieAdapter = MovieAdapter(movies)

        movieRecyclerView.layoutManager = LinearLayoutManager(this)
        movieRecyclerView.adapter = movieAdapter

        loadMoviesButton.setOnClickListener {
            loadMovies()
        }
    }

    private fun loadMovies() {
        if (apiKey == "PASTE_YOUR_TMDB_V3_API_KEY_HERE" || apiKey.isBlank()) {
            statusText.text = "Add your TMDB API key in MainActivity.kt first."
            return
        }

        loadingProgressBar.visibility = View.VISIBLE
        loadMoviesButton.isEnabled = false
        statusText.text = "Loading movies..."

        val url =
            "https://api.themoviedb.org/3/movie/popular?api_key=$apiKey&language=en-US&page=1"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingProgressBar.visibility = View.GONE
                    loadMoviesButton.isEnabled = true
                    statusText.text = "Network error: ${e.localizedMessage ?: "Please try again."}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()

                    if (!it.isSuccessful) {
                        runOnUiThread {
                            loadingProgressBar.visibility = View.GONE
                            loadMoviesButton.isEnabled = true
                            statusText.text =
                                "TMDB error: HTTP ${it.code}. Check your API key."
                        }
                        return
                    }

                    try {
                        val json = JSONObject(responseBody ?: "{}")
                        val results = json.getJSONArray("results")
                        val loadedMovies = mutableListOf<Movie>()

                        for (i in 0 until results.length()) {
                            val item = results.getJSONObject(i)

                            loadedMovies.add(
                                Movie(
                                    title = item.optString("title", "Unknown title"),
                                    releaseDate = item.optString(
                                        "release_date",
                                        "Release date unavailable"
                                    ).ifBlank { "Release date unavailable" },
                                    rating = item.optDouble("vote_average", 0.0),
                                    overview = item.optString(
                                        "overview",
                                        "No description available."
                                    ).ifBlank { "No description available." },
                                    posterPath = item.optString("poster_path")
                                        .takeIf { path -> path.isNotBlank() && path != "null" }
                                )
                            )
                        }

                        runOnUiThread {
                            movies.clear()
                            movies.addAll(loadedMovies)
                            movieAdapter.notifyDataSetChanged()

                            loadingProgressBar.visibility = View.GONE
                            loadMoviesButton.isEnabled = true

                            statusText.text =
                                if (movies.isEmpty()) {
                                    "No movies were returned. Try again later."
                                } else {
                                    "Loaded ${movies.size} popular movies."
                                }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            loadingProgressBar.visibility = View.GONE
                            loadMoviesButton.isEnabled = true
                            statusText.text = "Could not read movie data. Please try again."
                        }
                    }
                }
            }
        })
    }
}

