package edu.uark.joshuaj.skillscape

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var userRankTextView: TextView
    private lateinit var userScoreTextView: TextView
    private lateinit var homeButton: Button

    private var listenerRegistration: ListenerRegistration? = null

    private val gameId: String by lazy {
        intent.getStringExtra("gameId") ?: "default_game_id"
    }

    private lateinit var leaderboardDAO: LeaderboardDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        leaderboardRecyclerView = findViewById(R.id.leaderboard_recycler_view)
        userRankTextView = findViewById(R.id.user_rank_text_view)
        userScoreTextView = findViewById(R.id.user_score_text_view)
        homeButton = findViewById(R.id.home_button)

        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize LeaderboardDAO instance
        leaderboardDAO = LeaderboardDAO()

        loadLeaderboard()
        loadUserRankAndScore()

        homeButton.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, GamePickerActivity::class.java)
        // Clear the back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Optionally, call finish() if you want to close the current activity
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadUserRankAndScore()
    }


    private fun loadLeaderboard() {
        val query = leaderboardDAO.getTopScores(gameId)
        listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                // Handle error
                Log.e("LeaderboardActivity", "Error fetching leaderboard: ${e?.message}")
                return@addSnapshotListener
            }

            val leaderboardEntries = snapshot.documents.map { doc ->
                val username = doc.getString("username") ?: "Unknown"
                val score = doc.getLong("score")?.toInt() ?: 0
                LeaderboardEntry(
                    uid = doc.id, // Use the document ID as uid
                    username = username,
                    score = score
                )
            }

            leaderboardRecyclerView.adapter = LeaderboardAdapter(leaderboardEntries)
        }
    }

    private fun loadUserRankAndScore() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""
        val email = currentUser?.email ?: ""

        if (uid.isBlank() || email.isBlank()) {
            // Handle unauthenticated user or missing email
            userRankTextView.text = "Please log in to see your rank."
            userScoreTextView.text = ""
            return
        }

        // Extract username from email
        val username = email.substringBefore("@")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val (rank, score) = leaderboardDAO.getUserRankAndScore(gameId, uid)
                userRankTextView.text = "Your Rank: $rank"
                userScoreTextView.text = "Your High Score: $score"
            } catch (e: Exception) {
                // Handle error
                Log.e("LeaderboardActivity", "Error fetching user rank and score: ${e.message}")
                userRankTextView.text = "Error fetching your rank."
                userScoreTextView.text = ""
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
