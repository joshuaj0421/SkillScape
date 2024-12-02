package edu.uark.joshuaj.skillscape

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import androidx.core.content.ContextCompat

class MathGameActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var mathProblemTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var scoreTextView: TextView

    private var score = 0
    private var currentAnswer = 0
    private val gameDuration = 30000L // 30 seconds
    private val tickInterval = 1000L // 1 second

    // MediaPlayer instances for the sound effects
    private lateinit var correctSound: MediaPlayer
    private lateinit var incorrectSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_game)

        timerTextView = findViewById(R.id.timerTextView)
        mathProblemTextView = findViewById(R.id.mathProblemTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        scoreTextView = findViewById(R.id.scoreTextView)

        // Initialize the sound effects
        correctSound = MediaPlayer.create(this, R.raw.reward_sound)  // Correct sound
        incorrectSound = MediaPlayer.create(this, R.raw.x_sound)    // Incorrect sound

        startGame()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString()
            if (userAnswer.isNotEmpty()) {
                if (userAnswer.toInt() == currentAnswer) {
                    // Play the correct answer sound
                    correctSound.start()
                    score++
                    scoreTextView.text = "Score: $score"
                } else {
                    // Play the incorrect answer sound
                    incorrectSound.start()
                }
            }
            answerEditText.text.clear()
            generateNewProblem()
        }
    }

    private fun startGame() {
        score = 0
        scoreTextView.text = "Score: $score"

        object : CountDownTimer(gameDuration, tickInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Time Left: $secondsLeft"
            }

            override fun onFinish() {
                timerTextView.text = "Time's Up!"
                showFinalScorePopup()
                submitButton.isEnabled = false
            }
        }.start()

        generateNewProblem()
    }

    private fun generateNewProblem() {
        val num1 = Random.nextInt(1, 16) // Random number between 1 and 15
        val num2 = Random.nextInt(1, 16)

        // Ensure num1 is greater than or equal to num2 when subtraction is involved
        val operator = listOf("+", "-", "*", "/").random()

        when (operator) {
            "+" -> {
                currentAnswer = num1 + num2
                mathProblemTextView.text = "$num1 + $num2"
            }
            "-" -> {
                // Ensure num1 >= num2 for subtraction
                if (num1 < num2) {
                    generateNewProblem() // Retry if num1 < num2
                    return
                }
                currentAnswer = num1 - num2
                mathProblemTextView.text = "$num1 - $num2"
            }
            "*" -> {
                currentAnswer = num1 * num2
                mathProblemTextView.text = "$num1 × $num2"
            }
            "/" -> {
                // Ensure division results in a whole number
                if (num1 % num2 == 0) {
                    currentAnswer = num1 / num2
                    mathProblemTextView.text = "$num1 ÷ $num2"
                } else {
                    generateNewProblem() // Retry if division doesn't result in an integer
                }
            }
        }
    }

    private fun showFinalScorePopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Game Over")
        dialogBuilder.setMessage("Your final score is: $score")
        dialogBuilder.setPositiveButton("Restart") { _, _ ->
            restartGame()
        }
        dialogBuilder.setNegativeButton("Exit") { _, _ ->
            finish()
        }
        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
    }

    private fun restartGame() {
        score = 0
        scoreTextView.text = "Score: $score"
        submitButton.isEnabled = true
        startGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer resources when the activity is destroyed
        correctSound.release()
        incorrectSound.release()
    }
}