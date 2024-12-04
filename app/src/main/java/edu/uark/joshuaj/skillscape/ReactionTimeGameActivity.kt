package edu.uark.joshuaj.skillscape

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReactionTimeGameActivity : AppCompatActivity() {

    private var startTime: Long = 0
    private var reactionTimes = mutableListOf<Long>()
    private var attempts = 0
    private val maxAttempts = 3
    private var isWaitingForPress = false
    private var isWaitingForContinue = false
    private var isRedScreen = true
    private var gameStarted = false
    private var gameFrozen: Boolean = false

    private lateinit var gameText: TextView
    private lateinit var gameTextBold: TextView
    private lateinit var gameLayout: LinearLayout
    private lateinit var viewLeaderboardButton: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_time_game)

        // Initialize UI components
        gameText = findViewById(R.id.gameText)
        gameTextBold = findViewById(R.id.gameTitle)
        gameLayout = findViewById(R.id.gameLayout)
        viewLeaderboardButton = findViewById(R.id.view_leaderboard_button)

        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))

        // Set up click listener for the View Leaderboard button
        viewLeaderboardButton.setOnClickListener {
            navigateToLeaderboard()
        }

        // Set up the screen to detect any touch (for starting the game)
        gameLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!gameStarted) {
                    startGame()
                    gameStarted = true // Prevent starting the game again
                } else if (isWaitingForPress) {
                    // User pressed when waiting for green
                    val reactionTime = System.currentTimeMillis() - startTime
                    reactionTimes.add(reactionTime)

                    // Display the reaction time and prompt to continue
                    gameTextBold.text = "Reaction Time: ${reactionTime}ms"
                    gameText.text = "Press anywhere to continue"

                    // Set flag to wait for another press to continue
                    isWaitingForPress = false
                    isWaitingForContinue = true
                } else if (isWaitingForContinue) {
                    // User pressed again to continue to the next round
                    attempts++
                    isWaitingForContinue = false
                    runTestCycle() // Proceed to the next test
                } else if (isRedScreen) {
                    // User pressed too soon (screen is red)
                    gameTextBold.text = "Too soon! Wait for green"
                    gameText.text = "Try again"
                    gameFrozen = !gameFrozen
                    if (!gameFrozen) {
                        // Reset the round and run the test again
                        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                        gameTextBold.text = "Wait for green"
                        gameText.text = ""
                        isRedScreen = true
                        isWaitingForPress = false

                        // Restart the test cycle for the next round
                        reactionTimeTest()
                    }
                }
                true // Event handled
            } else {
                false // Ignore other touch events
            }
        }
    }

    private fun startGame() {
        attempts = 0
        reactionTimes.clear()
        runTestCycle()
    }

    // Run the reaction time test up to maxAttempts
    private fun runTestCycle() {
        if (attempts < maxAttempts) {
            reactionTimeTest()
        } else {
            // Once all attempts are done, display the results
            val averageReactionTime = reactionTimes.average().toInt()
            gameTextBold.text = "Game Over!"
            gameText.text = "Your average reaction time was: ${averageReactionTime}ms"

            // Show the View Leaderboard button
            viewLeaderboardButton.visibility = View.VISIBLE

            // Update the leaderboard with the user's average reaction time
            updateLeaderboard(averageReactionTime)
        }
    }

    // Perform a single reaction time test
    private fun reactionTimeTest() {
        val timeToSleep = (1000..5000).random()  // Random sleep time between 1 and 5 seconds

        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        gameTextBold.text = "Wait for green"
        gameText.text = ""
        isRedScreen = true

        // Introduce a delay before changing the background to green
        Handler(Looper.getMainLooper()).postDelayed({
            if (!gameFrozen) {
                // After the delay, change background to green and prompt the user to press
                gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                gameTextBold.text = "Press now!"
                gameText.text = ""
                startTime = System.currentTimeMillis()

                isWaitingForPress = true
                isRedScreen = false
            }
        }, timeToSleep.toLong())
    }

    private fun updateLeaderboard(averageReactionTime: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""
        val email = currentUser?.email ?: ""

        if (uid.isBlank() || email.isBlank()) {
            // Handle unauthenticated user or missing email
            Toast.makeText(this, "Please log in to submit your score to the leaderboard.", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract username from email
        val username = email.substringBefore("@")

        // Update the leaderboard
        val leaderboardDAO = LeaderboardDAO()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // For reaction time, lower scores are better. Adjust update logic if needed.
                leaderboardDAO.updateUserScore("reaction_time", uid, username, averageReactionTime)
            } catch (e: Exception) {
                // Handle error
                Log.e("ReactionTimeGame", "Error updating leaderboard: ${e.message}")
                Toast.makeText(this@ReactionTimeGameActivity, "Error updating leaderboard.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLeaderboard() {
        val intent = Intent(this, LeaderboardActivity::class.java)
        intent.putExtra("gameId", "reaction_time")
        startActivity(intent)
    }
}
