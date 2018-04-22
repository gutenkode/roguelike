package de.gutenko.roguelike.habittracker.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import de.gutenko.roguelike.R
import kotlinx.android.synthetic.main.activity_stats.pager
import kotlinx.android.synthetic.main.activity_stats.tabLayout
import kotlinx.android.synthetic.main.activity_stats.toolbar

class StatsActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        userId = intent.getStringExtra(userIdKey)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tabLayout.tabMode = TabLayout.MODE_FIXED
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        pager.adapter = StatsPagerAdapter(
            supportFragmentManager,
            userId
        )

        tabLayout.setupWithViewPager(pager)
    }

    class StatsPagerAdapter(fragmentManager: FragmentManager, private val userId: String) :
        FragmentPagerAdapter(fragmentManager) {

        private val titles = listOf("Habits", "Goals", "Map", "Inventory")

        override fun getPageTitle(position: Int): String {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return HabitFragment.newInstance(userId)
        }

        override fun getCount(): Int = 4
    }

    companion object {
        private val userIdKey = "userIdKey"

        fun launchIntent(context: Context, userId: String): Intent {
            return Intent(context, StatsActivity::class.java).apply {
                putExtra(userIdKey, userId)
            }
        }
    }
}
