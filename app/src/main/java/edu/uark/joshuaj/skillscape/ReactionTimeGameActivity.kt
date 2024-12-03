package edu.uark.joshuaj.skillscape

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ReactionTimeGameActivity : AppCompatActivity() {

    private var startTime: Long = 0
    private var reactionTimes = mutableListOf<Long>()
    private var attempts = 0
    private val maxAttempts = 3
    private var isWaitingForPress = false
    private var isWaitingForContinue = false // New flag to wait for press after displaying reaction time
    private var isRedScreen = true // Track if the screen is red
    private var gameStarted = false // Track if the game has started
    private lateinit var gameText: TextView
    private lateinit var gameTextBold: TextView
    private lateinit var gameLayout: LinearLayout
    private var gameFrozen: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_time_game)

        // Initialize the TextViews from the layout
        gameText = findViewById(R.id.gameText)
        gameTextBold = findViewById(R.id.gameTitle)
        gameLayout = findViewById(R.id.gameLayout)

        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_light))

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
                    isWaitingForContinue = true // Now wait for press to continue the game
                } else if (isWaitingForContinue) {
                    // User pressed again to continue to the next round
                    attempts++
                    isWaitingForContinue = false // No longer waiting for continue press
                    runTestCycle() // Proceed to the next test
                } else if (isRedScreen) {
                    // User pressed too soon (screen is red)
                    gameTextBold.text = "Too soon! Wait for green"
                    gameText.text = "Try again"
                    gameFrozen = !gameFrozen
                    if(!gameFrozen){
                        // reset round
                        // Reset the round and run the test again
                        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                        gameTextBold.text = "Wait for green"
                        gameText.text = ""
                        isRedScreen = true // Set the screen to red
                        isWaitingForPress = false // Reset waiting for press flag

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

    fun startGame() {
        attempts = 0
        reactionTimes.clear()
        runTestCycle()
    }

    // Run the reaction time test up to maxAttempts
    fun runTestCycle() {
        if (attempts < maxAttempts) {
            reactionTimeTest()
        } else {
            // Once all attempts are done, display the results
            gameTextBold.text = "Game Over!"
            gameText.text = "Your average reaction time was: ${reactionTimes.average().toInt()}ms"
        }
    }

    // Perform a single reaction time test
    fun reactionTimeTest() {
        val timeToSleep = (1000..5000).random()  // Random sleep time between 1 and 5 seconds

        gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        gameTextBold.text = "Wait for green"
        gameText.text = ""
        isRedScreen = true // Set the screen to red

        // Introduce a delay before changing the background to green
        android.os.Handler(mainLooper).postDelayed({
            if(!gameFrozen){
                // After the delay, change background to green and prompt the user to press
                gameLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                gameTextBold.text = "Press now!"  // Set bold text
                gameText.text = ""  // Set normal text
                startTime = System.currentTimeMillis()  // Start the timer

                isWaitingForPress = true // Now waiting for the user to press the screen
                isRedScreen = false // The screen is no longer red
            }

        }, timeToSleep.toLong()) // Random delay before green background appears
    }
}