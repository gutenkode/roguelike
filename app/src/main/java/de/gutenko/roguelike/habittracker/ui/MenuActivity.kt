package de.gutenko.roguelike.habittracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import de.gutenko.roguelike.R
import de.gutenko.roguelike.loop.MainActivity
import kotlinx.android.synthetic.main.activity_menu.newGameButton
import kotlinx.android.synthetic.main.activity_menu.statsButton

class MenuActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        userId = intent.getStringExtra(userIdKey)

        newGameButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        statsButton.setOnClickListener {
            startActivity(StatsActivity.launchIntent(this, userId, markHabitAsDone = null))
        }
    }

    companion object {
        private val userIdKey = "USER_ID_KEY"

        fun launchIntent(context: Context, userId: String): Intent {
            return Intent(context, MenuActivity::class.java).apply {
                putExtra(userIdKey, userId)
            }
        }
    }
}
