package edu.uark.joshuaj.skillscape

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GamePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)

        // Get references to buttons and text view
        val square1 = findViewById<Button>(R.id.square1)
        val square2 = findViewById<Button>(R.id.square2)
        val square3 = findViewById<Button>(R.id.square3)
        val square4 = findViewById<Button>(R.id.square4)
        val square5 = findViewById<Button>(R.id.square5)
        val square6 = findViewById<Button>(R.id.square6)
        val squareClickedText = findViewById<TextView>(R.id.squareClickedText)

        // Set click listeners for each square button
        square1.setOnClickListener {
            squareClickedText.text = "Square 1 clicked"
        }
        square2.setOnClickListener {
            squareClickedText.text = "Square 2 clicked"
        }
        square3.setOnClickListener {
            squareClickedText.text = "Square 3 clicked"
        }
        square4.setOnClickListener {
            squareClickedText.text = "Square 4 clicked"
        }
        square5.setOnClickListener {
            squareClickedText.text = "Square 5 clicked"
        }
        square6.setOnClickListener {
            squareClickedText.text = "Square 6 clicked"
        }
    }
}