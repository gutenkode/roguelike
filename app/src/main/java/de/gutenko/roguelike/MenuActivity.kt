package de.gutenko.roguelike

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import de.gutenko.roguelike.loop.MainActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        newGameButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }
}
