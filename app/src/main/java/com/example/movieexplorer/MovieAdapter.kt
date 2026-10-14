package com.example.movieexplorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MovieAdapter(
    private val movies: List<Movie>
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val posterImage: ImageView =
            itemView.findViewById(R.id.moviePoster)

        val titleText: TextView =
            itemView.findViewById(R.id.movieTitle)

        val releaseDateText: TextView =
            itemView.findViewById(R.id.movieReleaseDate)

        val ratingText: TextView =
            itemView.findViewById(R.id.movieRating)

        val overviewText: TextView =
            itemView.findViewById(R.id.movieOverview)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)

        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MovieViewHolder,
        position: Int
    ) {
        val movie = movies[position]

        holder.titleText.text = movie.title

        holder.releaseDateText.text =
            "Release date: ${movie.releaseDate}"

        holder.ratingText.text =
            "Rating: ${String.format("%.1f", movie.rating)}/10"

        holder.overviewText.text = movie.overview

        val imageUrl = movie.posterPath?.let {
            "https://image.tmdb.org/t/p/w500$it"
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.posterImage)
    }

    override fun getItemCount(): Int {
        return movies.size
    }
}
