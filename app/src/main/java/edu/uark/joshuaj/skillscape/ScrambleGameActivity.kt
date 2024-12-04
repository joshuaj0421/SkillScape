package edu.uark.joshuaj.skillscape

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils

class ScrambleGameActivity : AppCompatActivity() {

    private lateinit var scrambledWordTextView: TextView
    private lateinit var guessEditText: EditText
    private lateinit var submitGuessButton: Button
    private lateinit var skipButton: Button
    private lateinit var statusTextView: TextView

    private val wordPool = listOf(
        "apple", "banana", "grape", "orange", "peach", "cherry", "melon", "berry",
        "kiwi", "lemon", "plum", "mango", "lime", "fig", "apricot", "papaya",
        "guava", "coconut", "pineapple", "raspberry", "strawberry", "blueberry",
        "nectarine", "carrot", "pepper", "potato", "tomato", "onion", "broccoli",
        "spinach", "lettuce", "zucchini", "pumpkin", "squash", "beet", "radish",
        "celery", "okra", "basil", "mint", "thyme", "parsley", "sage", "rosemary"
    ).toMutableList() // Mutable list for dynamic replacement
    private val words = mutableListOf<String>() // Words for the current game
    private var currentWordIndex = 0
    private var startTime: Long = 0
    private var endTime: Long = 0

    private val currentWord: String
        get() = words[currentWordIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scramble_game)

        scrambledWordTextView = findViewById(R.id.scrambledWordTextView)
        guessEditText = findViewById(R.id.guessEditText)
        submitGuessButton = findViewById(R.id.submitGuessButton)
        skipButton = findViewById(R.id.skipButton)
        statusTextView = findViewById(R.id.statusTextView)

        // Start the game
        startGame()

        submitGuessButton.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_button))
            checkGuess()
        }

        skipButton.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_button))
            skipCurrentWord()
        }
    }

    private fun startGame() {
        currentWordIndex = 0
        words.addAll(wordPool.shuffled().take(3)) // Randomly choose 3 initial words
        startTime = SystemClock.elapsedRealtime()
        showNextWord()
    }

    private fun showNextWord() {
        if (currentWordIndex < words.size) {
            val scrambledWord = scrambleWord(currentWord)
            scrambledWordTextView.text = scrambledWord
            scrambledWordTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
            guessEditText.text.clear()
            statusTextView.text = "Unscramble the word!"
        } else {
            endTime = SystemClock.elapsedRealtime()
            showFinalScore()
        }
    }

    private fun scrambleWord(word: String): String {
        val characters = word.toCharArray()
        do {
            characters.shuffle()
        } while (String(characters) == word) // Ensure the scrambled word is different
        return String(characters)
    }

    private fun checkGuess() {
        val userGuess = guessEditText.text.toString().trim()

        if (userGuess.equals(currentWord, ignoreCase = true)) {
            currentWordIndex++
            showNextWord()
        } else {
            statusTextView.text = "Incorrect! Try again."
        }
    }

    private fun skipCurrentWord() {
        // Deduct 30 seconds from elapsed time
        startTime -= 30 * 1000 // Subtract 30 seconds (30,000 ms)

        // Replace the skipped word with a new one
        val newWord = getNewWord()
        words[currentWordIndex] = newWord

        showNextWord()
        statusTextView.text = "You skipped! 30 seconds added to your time."
    }

    private fun getNewWord(): String {
        // Select a new word that is not already in the current game
        val remainingWords = wordPool - words
        return remainingWords.random()
    }

    private fun showFinalScore() {
        val timeTaken = (endTime - startTime) / 1000 // seconds
        statusTextView.text = "Congratulations! Time: $timeTaken seconds"
        scrambledWordTextView.text = ""
        guessEditText.isEnabled = false
        submitGuessButton.isEnabled = false
        skipButton.isEnabled = false
    }
}
