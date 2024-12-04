package edu.uark.joshuaj.skillscape

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.widget.FrameLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardFlipActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var viewLeaderboardButton: Button

    private var cards = mutableListOf<Card>()
    private var firstSelectedCard: Card? = null
    private var secondSelectedCard: Card? = null

    private var score = 0
    private var startTime = 0L
    private val handler = Handler()

    // Variable to indicate if the game is processing selected cards
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_flip)

        viewLeaderboardButton = findViewById(R.id.view_leaderboard_button)
        viewLeaderboardButton.setOnClickListener {
            navigateToLeaderboard()
        }

        gridLayout = findViewById(R.id.grid_layout)
        scoreTextView = findViewById(R.id.score_text_view)
        timeTextView = findViewById(R.id.time_text_view)

        setupCards()
        startGame()
    }

    private fun navigateToLeaderboard() {
        val intent = Intent(this@CardFlipActivity, LeaderboardActivity::class.java)
        intent.putExtra("gameId", "card_flip")
        startActivity(intent)
    }

    private fun setupCards() {
        val imageResIds = listOf(
            R.drawable.apple,
            R.drawable.banana,
            R.drawable.cherry,
            R.drawable.grape,
            R.drawable.mango,
            R.drawable.orange,
            R.drawable.pear,
            R.drawable.strawberry
        )

        val allImages = mutableListOf<Int>()
        for (image in imageResIds) {
            allImages.add(image)
            allImages.add(image) // Each image appears twice
        }

        allImages.shuffle()

        // Create card views and add them to the grid layout
        for (i in 0 until 16) {
            val cardView = CardView(this)
            val params = GridLayout.LayoutParams()
            val cardWidth = resources.getDimensionPixelSize(R.dimen.card_width)
            val cardHeight = resources.getDimensionPixelSize(R.dimen.card_height)
            params.width = cardWidth
            params.height = cardHeight
            params.setMargins(8, 8, 8, 8)
            cardView.layoutParams = params
            cardView.radius = resources.getDimension(R.dimen.card_corner_radius)
            cardView.cardElevation = 4f

            // Create ImageView for the card's image
            val imageView = ImageView(this)
            imageView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageResource(R.drawable.card_back) // Initially show the back of the card

            cardView.addView(imageView)

            val card = Card(
                id = i,
                imageResId = allImages[i],
                view = cardView,
                imageView = imageView
            )

            cardView.setOnClickListener {
                onCardClicked(card)
            }

            cards.add(card)
            gridLayout.addView(cardView)
        }
    }

    private fun startGame() {
        // Show all cards with their images
        for (card in cards) {
            card.imageView.setImageResource(card.imageResId)
            card.isFlipped = true
        }

        // Start timer
        startTime = SystemClock.elapsedRealtime()
        handler.post(updateTimerRunnable)

        // After 10 seconds, flip all cards to hide the images
        handler.postDelayed({
            for (card in cards) {
                card.imageView.setImageResource(R.drawable.card_back)
                card.isFlipped = false
            }
        }, 10000) // 10 seconds
    }

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            val elapsedSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000
            timeTextView.text = "Time: $elapsedSeconds s"

            // Subtract points over time
            if (elapsedSeconds % 5 == 0L && elapsedSeconds != 0L) {
                score -= 1
                updateScore()
            }

            handler.postDelayed(this, 1000)
        }
    }

    private fun onCardClicked(card: Card) {
        if (isProcessing) {
            // Do not allow clicks when processing
            return
        }

        if (card.isFlipped || card.isMatched) {
            // Ignore clicks on already flipped or matched cards
            return
        }

        // Flip the card to show the image
        card.imageView.setImageResource(card.imageResId)
        card.isFlipped = true

        if (firstSelectedCard == null) {
            firstSelectedCard = card
        } else if (secondSelectedCard == null) {
            secondSelectedCard = card

            // Set isProcessing to true to prevent further clicks
            isProcessing = true

            // Check for match after a short delay
            handler.postDelayed({
                checkForMatch()
            }, 1000)
        }
    }

    private fun checkForMatch() {
        if (firstSelectedCard != null && secondSelectedCard != null) {
            if (firstSelectedCard!!.imageResId == secondSelectedCard!!.imageResId) {
                // It's a match
                firstSelectedCard!!.isMatched = true
                secondSelectedCard!!.isMatched = true

                score += 10 // Gain points
                updateScore()

                // Check if all cards are matched
                if (cards.all { it.isMatched }) {
                    gameWon()
                }

                // Reset selections and processing flag
                firstSelectedCard = null
                secondSelectedCard = null
                isProcessing = false

            } else {
                // Not a match
                // Keep the cards flipped for a moment before flipping back
                handler.postDelayed({
                    // Flip both cards back to show the card back image
                    firstSelectedCard!!.imageView.setImageResource(R.drawable.card_back)
                    secondSelectedCard!!.imageView.setImageResource(R.drawable.card_back)
                    firstSelectedCard!!.isFlipped = false
                    secondSelectedCard!!.isFlipped = false

                    // Reset selections and processing flag
                    firstSelectedCard = null
                    secondSelectedCard = null
                    isProcessing = false
                }, 1000) // Delay before flipping back
            }
        }
    }

    private fun updateScore() {
        if (score < 0) score = 0
        scoreTextView.text = "Score: $score"
    }

    private fun gameWon() {
        handler.removeCallbacks(updateTimerRunnable)
        val totalTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        // Adjust score based on time
        val timeBonus = (100 - totalTime).toInt()
        val finalScore = score + if (timeBonus > 0) timeBonus else 0

        // Update the score with the final score
        score = finalScore
        updateScore()

        // Show a message to the user
        Toast.makeText(this, "Congratulations! You won the game!", Toast.LENGTH_LONG).show()

        // Make the "View Leaderboard" button visible
        findViewById<Button>(R.id.view_leaderboard_button).visibility = View.VISIBLE

        // Update the leaderboard
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
                // Update the user's score
                leaderboardDAO.updateUserScore("card_flip", uid, username, score)
            } catch (e: Exception) {
                // Handle error
                Log.e("CardFlipActivity", "Error updating user score: ${e.message}")
                Toast.makeText(this@CardFlipActivity, "Error updating leaderboard.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimerRunnable)
    }
}

data class Card(
    val id: Int,
    val imageResId: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false,
    val view: CardView,
    val imageView: ImageView
)
