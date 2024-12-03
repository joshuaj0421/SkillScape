package edu.uark.joshuaj.skillscape

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import kotlin.random.Random

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

        // Initialize UI components
        timerTextView = findViewById(R.id.timerTextView)
        mathProblemTextView = findViewById(R.id.mathProblemTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        scoreTextView = findViewById(R.id.scoreTextView)

        // Initialize the sound effects
        correctSound = MediaPlayer.create(this, R.raw.reward_sound)  // Correct sound
        incorrectSound = MediaPlayer.create(this, R.raw.x_sound)    // Incorrect sound

        startGame()

        // Apply animation to the submit button on click
        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString()
            if (userAnswer.isNotEmpty()) {
                val isCorrect = userAnswer.toInt() == currentAnswer
                if (isCorrect) {
                    // Play the correct answer sound
                    correctSound.start()
                    score++
                    scoreTextView.text = "Score: $score"
                    showAnswerFeedback(true) // Show correct answer feedback
                } else {
                    // Play the incorrect answer sound
                    incorrectSound.start()
                    showAnswerFeedback(false) // Show incorrect answer feedback
                }
            }
            answerEditText.text.clear()
            generateNewProblem()

            // Ensure the button stays green
            submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green)) // Set the button color to green
        }
    }

    private fun startGame() {
        score = 0
        scoreTextView.text = "Score: $score"

        // Start countdown timer
        object : CountDownTimer(gameDuration, tickInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Time Left: $secondsLeft"

                // Animate timer text to make it stand out
                val scaleX = ObjectAnimator.ofFloat(timerTextView, "scaleX", 1f, 1.2f, 1f)
                val scaleY = ObjectAnimator.ofFloat(timerTextView, "scaleY", 1f, 1.2f, 1f)
                scaleX.duration = 500
                scaleY.duration = 500
                scaleX.start()
                scaleY.start()
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

        val operator = listOf("+", "-", "*", "/").random()

        when (operator) {
            "+" -> {
                currentAnswer = num1 + num2
                mathProblemTextView.text = "$num1 + $num2"
            }
            "-" -> {
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
                if (num1 % num2 == 0) {
                    currentAnswer = num1 / num2
                    mathProblemTextView.text = "$num1 ÷ $num2"
                } else {
                    generateNewProblem() // Retry if division doesn't result in an integer
                }
            }
        }

        // Animate math problem text to create visual excitement
        val fadeIn = ObjectAnimator.ofFloat(mathProblemTextView, "alpha", 0f, 1f)
        fadeIn.duration = 800
        fadeIn.start()
    }

    private fun showAnswerFeedback(isCorrect: Boolean) {
        val feedbackView = findViewById<View>(R.id.submitButton) // Button used for feedback

        // Set color feedback
        if (isCorrect) {
            feedbackView.setBackgroundColor(ContextCompat.getColor(this, R.color.green)) // Correct answer - Green
            val scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale) // Scale animation
            feedbackView.startAnimation(scaleAnim)
        } else {
            feedbackView.setBackgroundColor(ContextCompat.getColor(this, R.color.red)) // Incorrect answer - Red
            val shakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake) // Shake animation
            feedbackView.startAnimation(shakeAnim)
        }

        // Reset feedback after 1 second
        Handler().postDelayed({ resetFeedback() }, 1000)
    }

    private fun resetFeedback() {
        val feedbackView = findViewById<View>(R.id.submitButton)
        feedbackView.setBackgroundColor(ContextCompat.getColor(this, R.color.green)) // Reset to green
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