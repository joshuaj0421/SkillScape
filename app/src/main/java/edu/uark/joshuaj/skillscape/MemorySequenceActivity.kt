package edu.uark.joshuaj.skillscape

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import kotlin.random.Random

class MemorySequenceActivity : AppCompatActivity() {

    private lateinit var buttons: Array<Button>
    private lateinit var scoreTextView: TextView
    private val sequence = mutableListOf<Int>()
    private val userInput = mutableListOf<Int>()
    private var currentIndex = 0
    private var score = 0
    private val handler = Handler()
    private var isGameOver = false
    private val defaultColor = Color.parseColor("#F5F5DC") // Cream color (Beige)

    private val buttonHighlightColors = arrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.CYAN,
        Color.MAGENTA, Color.YELLOW, Color.LTGRAY, Color.DKGRAY, Color.WHITE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_sequence)

        // Initialize UI components
        scoreTextView = findViewById(R.id.scoreTextView)
        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // Initialize buttons with a default gray color
        buttons = Array(9) { index ->
            val button = Button(this)
            button.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(index % 3, 1, 1f)
                rowSpec = GridLayout.spec(index / 3, 1, 1f)
                setMargins(16, 16, 16, 16) // Increased space between buttons for premium look
            }
            button.background = createRoundedBackground(defaultColor)
            button.setOnClickListener { onButtonClicked(index) }
            button.text = " " // Empty text for better aesthetics
            button.setTextColor(Color.WHITE) // White text color for visibility
            button.textSize = 22f // Larger text size for a premium feel
            button.setPadding(0, 0, 0, 0) // No padding for uniform button shape
            button.isAllCaps = true // Capitalize text for a stronger look
            gridLayout.addView(button)
            button
        }

        startNewGame()
    }

    private fun createRoundedBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f // Rounded corners
            setColor(color)
            setStroke(4, Color.BLACK) // Use a constant black border
        }
    }

    private fun startNewGame() {
        sequence.clear()
        userInput.clear()
        currentIndex = 0
        score = 0
        isGameOver = false
        updateScore()
        enableButtons(true)
        generateNextSequence()
    }

    private fun generateNextSequence() {
        sequence.add(Random.nextInt(0, 9))
        userInput.clear()
        currentIndex = 0
        // Disable buttons while the sequence is being shown
        enableButtons(false)
        // Delay before showing the new sequence
        handler.postDelayed({
            showSequence()
        }, 500) // 1 second delay
    }

    private fun showSequence() {
        var delay = 0L
        for (index in sequence) {
            handler.postDelayed({
                highlightButton(index, true) // Highlight with unique color
            }, delay)
            delay += 700 // Longer duration for each highlight
        }
        handler.postDelayed({
            resetButtonColors()
            // Re-enable buttons after the sequence is shown
            enableButtons(true)
        }, delay)
    }

    private fun highlightButton(index: Int, isHighlightingSequence: Boolean) {
        val highlightColor = if (isHighlightingSequence) buttonHighlightColors[index] else defaultColor
        buttons[index].background = createRoundedBackground(highlightColor)
        handler.postDelayed({
            buttons[index].background = createRoundedBackground(defaultColor)
        }, 500) // Highlight duration (500 ms)
    }

    private fun resetButtonColors() {
        buttons.forEach {
            it.background = createRoundedBackground(defaultColor)
        }
    }

    private fun onButtonClicked(index: Int) {
        if (isGameOver) return

        // Highlight button with its unique color when clicked
        buttons[index].background = createRoundedBackground(buttonHighlightColors[index])
        handler.postDelayed({
            buttons[index].background = createRoundedBackground(defaultColor)
        }, 300) // Click highlight duration (300 ms)

        // Add user input
        userInput.add(index)

        // Validate user input
        if (index == sequence[currentIndex]) {
            currentIndex++
            if (currentIndex == sequence.size) {
                score++
                updateScore()

                // Wait for user highlight to go away before showing the next sequence
                handler.postDelayed({
                    generateNextSequence()
                }, 1000) // Wait 1 second before showing the new sequence
            }
        } else {
            endGame()
        }
    }

    private fun updateScore() {
        scoreTextView.text = "Score: $score"
    }

    private fun endGame() {
        isGameOver = true
        enableButtons(false)
        scoreTextView.text = "Final Score: $score"
    }

    private fun enableButtons(enable: Boolean) {
        buttons.forEach { it.isEnabled = enable }
    }
}