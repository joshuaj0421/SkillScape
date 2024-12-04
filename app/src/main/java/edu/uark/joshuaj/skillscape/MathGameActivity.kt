package edu.uark.joshuaj.skillscape

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        correctSound = MediaPlayer.create(this, R.raw.reward_sound)
        incorrectSound = MediaPlayer.create(this, R.raw.x_sound)

        startGame()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString()
            if (userAnswer.isNotEmpty()) {
                val isCorrect = userAnswer.toInt() == currentAnswer
                if (isCorrect) {
                    correctSound.start()
                    score++
                    scoreTextView.text = "Score: $score"
                    showAnswerFeedback(true)
                } else {
                    incorrectSound.start()
                    showAnswerFeedback(false)
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
                timerTextView.text = "Time Left: ${millisUntilFinished / 1000}"
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
        val num1 = Random.nextInt(1, 16)
        val num2 = Random.nextInt(1, 16)
        val operator = listOf("+", "-", "*", "/").random()

        when (operator) {
            "+" -> {
                currentAnswer = num1 + num2
                mathProblemTextView.text = "$num1 + $num2"
            }
            "-" -> {
                if (num1 < num2) {
                    generateNewProblem()
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
                    generateNewProblem()
                }
            }
        }
    }

    private fun showAnswerFeedback(isCorrect: Boolean) {
        val feedbackView = submitButton

        if (isCorrect) {
            feedbackView.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            feedbackView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale))
        } else {
            feedbackView.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            feedbackView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake))
        }

        Handler().postDelayed({ resetFeedback() }, 1000)
    }

    private fun resetFeedback() {
        submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
    }

    private fun showFinalScorePopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Game Over")
        dialogBuilder.setMessage("Your final score is: $score")
        dialogBuilder.setPositiveButton("View Leaderboard") { _, _ ->
            updateLeaderboard()
        }
        dialogBuilder.setNegativeButton("Restart") { _, _ ->
            restartGame()
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

    private fun updateLeaderboard() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""
        val email = currentUser?.email ?: ""

        if (uid.isBlank() || email.isBlank()) {
            Toast.makeText(this, "Please log in to submit your score to the leaderboard.", Toast.LENGTH_SHORT).show()
            return
        }

        val username = email.substringBefore("@")
        val leaderboardDAO = LeaderboardDAO()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                leaderboardDAO.updateUserScore("math_game", uid, username, score)
                navigateToLeaderboard()
            } catch (e: Exception) {
                Log.e("MathGameActivity", "Error updating leaderboard: ${e.message}")
                Toast.makeText(this@MathGameActivity, "Error updating leaderboard.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLeaderboard() {
        val intent = Intent(this@MathGameActivity, LeaderboardActivity::class.java)
        intent.putExtra("gameId", "math_game")
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        correctSound.release()
        incorrectSound.release()
    }
}
