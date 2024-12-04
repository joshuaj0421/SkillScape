package edu.uark.joshuaj.skillscape

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
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

        // Game picker images
        val image1 = findViewById<ImageView>(R.id.image1)
        val image2 = findViewById<ImageView>(R.id.image2)
        val image3 = findViewById<ImageView>(R.id.image3)
        val image4 = findViewById<ImageView>(R.id.image4)
        val image5 = findViewById<ImageView>(R.id.image5)
        val image6 = findViewById<ImageView>(R.id.image6)

        // Set click listeners for each image
        image1.setOnClickListener {
            startGameActivity(ReactionTimeGameActivity::class.java)
        }
        image2.setOnClickListener {
            startGameActivity(MemorySequenceActivity::class.java)
        }
        image3.setOnClickListener {
            startGameActivity(ScrambleGameActivity::class.java)
        }
        image4.setOnClickListener {
            startGameActivity(MathGameActivity::class.java)
        }
        image5.setOnClickListener {
            startGameActivity(EstimationGameActivity::class.java)
        }
        image6.setOnClickListener {
            startGameActivity(CardFlipActivity::class.java)
        }
    }

    private fun <T> startGameActivity(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
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