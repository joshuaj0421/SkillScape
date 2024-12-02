package edu.uark.joshuaj.skillscape

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class GamePickerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "SkillScape"

        // Game picker buttons
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        // Set the username dynamically in the menu
        val usernameItem = menu?.findItem(R.id.action_username)
        val username = intent.getStringExtra("username") ?: "Player"
        usernameItem?.title = username

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Close GamePickerActivity
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
