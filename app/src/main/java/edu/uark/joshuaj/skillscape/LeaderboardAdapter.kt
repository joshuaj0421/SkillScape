package edu.uark.joshuaj.skillscape

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(private val entries: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_entry, parent, false)
        return LeaderboardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(position + 1, entry)
    }

    override fun getItemCount(): Int = entries.size

    inner class LeaderboardViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val rankTextView: TextView = itemView.findViewById(R.id.rank_text_view)
        private val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        private val scoreTextView: TextView = itemView.findViewById(R.id.score_text_view)

        fun bind(rank: Int, entry: LeaderboardEntry) {
            rankTextView.text = "$rank."
            usernameTextView.text = entry.username
            scoreTextView.text = entry.score.toString()
        }
    }
}
