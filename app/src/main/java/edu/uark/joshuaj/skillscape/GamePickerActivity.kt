package edu.uark.joshuaj.skillscape

import android.content.Intent
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

        // Set click listeners for each square button
        square1.setOnClickListener {
            // Start the ReactionTimeGame activity when square 1 is clicked
            val intent = Intent(this, ReactionTimeGameActivity::class.java)
            startActivity(intent)
        }
        square2.setOnClickListener {
        }
        square3.setOnClickListener {
        }
        square4.setOnClickListener {
        }
        square5.setOnClickListener {
        }
        square6.setOnClickListener {
        }
    }
}