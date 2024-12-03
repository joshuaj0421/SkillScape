package edu.uark.joshuaj.skillscape

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class EstimationGameActivity : AppCompatActivity() {

    private lateinit var shapesContainer: FrameLayout
    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var rulesTextView: TextView
    private var targetShapeCount = 0

    private var totalShapes = 0
    private var targetShape: String = ""
    private var isGameOver = false
    private var currentRound = 1
    private val totalRounds = 3
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estimation_game)

        shapesContainer = findViewById(R.id.shapesContainer)
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        rulesTextView = findViewById(R.id.rulesTextView)

        showRulesPopup()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString()
            if (userAnswer.isNotEmpty()) {
                val userGuess = userAnswer.toInt()
                val difference = kotlin.math.abs(userGuess - targetShapeCount) // Calculate the difference
                val pointsAwarded = kotlin.math.max(10 - (difference * 2), 0) // Deduct 2 points per difference, min 0

                // Update the total score
                score += pointsAwarded

                // Show a popup with the result
                AlertDialog.Builder(this)
                    .setTitle("Your Guess")
                    .setMessage("You guessed $userGuess.\nThe correct answer was $targetShapeCount.\nYou earned $pointsAwarded points!")
                    .setPositiveButton("OK") { dialog, _ ->
                        // Clear input and hide UI elements
                        answerEditText.text.clear()
                        questionTextView.visibility = TextView.GONE
                        answerEditText.visibility = EditText.GONE
                        submitButton.visibility = Button.GONE

                        // Check if there are more rounds or end the game
                        if (currentRound < totalRounds) {
                            currentRound++
                            startGame()
                        } else {
                            showGameOverDialog()
                        }

                        dialog.dismiss() // Close the dialog
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun showRulesPopup() {
        rulesTextView.visibility = TextView.VISIBLE

        AlertDialog.Builder(this)
            .setTitle("Game Rules")
            .setMessage(
                "A random number of shapes will appear for 3 seconds. Guess how many of a specific shape were displayed.\n\n" +
                        "Scoring:\n" +
                        "- Correct answer: 10 points\n" +
                        "- 1 away: 8 points\n" +
                        "- 2 away: 6 points\n" +
                        "- 3 away: 4 points\n" +
                        "- 4 away: 2 points\n" +
                        "- More than 4 away: 0 points\n\n" +
                        "You will play $totalRounds rounds. Try to get the highest score possible!"
            )
            .setPositiveButton("Start Game") { _, _ ->
                rulesTextView.visibility = TextView.GONE
                startGame()
            }
            .setCancelable(false)
            .show()
    }

    private fun startGame() {
        if (isGameOver) return

        val numberOfShapes = Random.nextInt(14, 25)
        totalShapes = numberOfShapes
        targetShape = listOf("circle", "square", "triangle").random()

        generateShapes(numberOfShapes)

        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                shapesContainer.removeAllViews()
                questionTextView.text = "How many $targetShape(s) were on the screen?"
                questionTextView.visibility = TextView.VISIBLE
                answerEditText.visibility = EditText.VISIBLE
                submitButton.visibility = Button.VISIBLE
            }
        }.start()
    }

    private fun generateShapes(numberOfShapes: Int) {
        shapesContainer.removeAllViews()

        val placedShapes = mutableListOf<Pair<Int, Int>>() // List to track the positions of shapes
        targetShapeCount = 0 // Reset target shape count

        for (i in 0 until numberOfShapes) {
            val shapeType = listOf("circle", "square", "triangle").random()
            val shapeView = when (shapeType) {
                "circle" -> createCircle()
                "square" -> createSquare()
                "triangle" -> createTriangle()
                else -> createCircle()
            }

            if (shapeType == targetShape) {
                targetShapeCount++ // Increment if it matches the target shape
            }

            var xPos: Int
            var yPos: Int
            var tries = 0

            do {
                xPos = Random.nextInt(0, shapesContainer.width - 100)
                yPos = Random.nextInt(0, shapesContainer.height - 100)
                tries++
            } while (overlapsWithExistingShapes(xPos, yPos, placedShapes) && tries < 100)

            if (tries == 100) {
                // Break the loop to avoid infinite attempts in small spaces
                break
            }

            placedShapes.add(Pair(xPos, yPos)) // Save the new shape's position

            val params = FrameLayout.LayoutParams(100, 100).apply {
                leftMargin = xPos
                topMargin = yPos
            }
            shapeView.layoutParams = params

            shapesContainer.addView(shapeView)
        }
    }

    private fun overlapsWithExistingShapes(x: Int, y: Int, placedShapes: List<Pair<Int, Int>>): Boolean {
        val shapeSize = 100 // Assuming shapes are 100x100
        for ((existingX, existingY) in placedShapes) {
            if (x < existingX + shapeSize &&
                x + shapeSize > existingX &&
                y < existingY + shapeSize &&
                y + shapeSize > existingY
            ) {
                return true
            }
        }
        return false
    }

    private fun createCircle(): ImageView {
        val circle = ImageView(this)
        circle.setImageResource(R.drawable.circle_shape)
        return circle
    }

    private fun createSquare(): ImageView {
        val square = ImageView(this)
        square.setImageResource(R.drawable.square_shape)
        return square
    }

    private fun createTriangle(): ImageView {
        val triangle = ImageView(this)
        triangle.setImageResource(R.drawable.triangle_shape)
        return triangle
    }

    private fun showGameOverDialog() {
        isGameOver = true

        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your total score is $score points!")
            .setPositiveButton("Play Again") { _, _ ->
                restartGame()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartGame() {
        isGameOver = false
        currentRound = 1
        score = 0
        startGame()
    }
}