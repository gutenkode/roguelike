package de.gutenko.roguelike.habittracker.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.ActivityKey
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.BuildConfig
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.data.habits.Habit
import de.gutenko.roguelike.habittracker.data.habits.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import de.gutenko.roguelike.habittracker.notifications.HabitNotificationBroadcastReceiver
import kotlinx.android.synthetic.main.activity_stats.drawerLayout
import kotlinx.android.synthetic.main.activity_stats.pager
import kotlinx.android.synthetic.main.activity_stats.tabLayout
import kotlinx.android.synthetic.main.activity_stats.toolbar
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@Subcomponent
interface StatsActivitySubcomponent : AndroidInjector<StatsActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<StatsActivity>()
}

@Module(subcomponents = [StatsActivitySubcomponent::class])
abstract class StatsActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(StatsActivity::class)
    abstract fun bindStatsActivityInjectorFactory(builder: StatsActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}

class StatsActivity : AppCompatActivity() {
    private lateinit var userId: String

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var habitCompletionRepository: HabitCompletionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        userId = intent.getStringExtra(userIdKey)
        val markHabitAsDone = intent.getStringExtra(markHabitAsDoneKey)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // TODO: Make this work
        if (markHabitAsDone != null) {
            habitCompletionRepository.addCompletion(userId, markHabitAsDone, LocalDate.now())
                .subscribe()
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pager.adapter = StatsPagerAdapter(
            supportFragmentManager,
            userId
        )


        toolbar.inflateMenu(R.menu.stats_menu)

        tabLayout.setupWithViewPager(pager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(habitChannelId, habitChannelName)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmScheduler = LoggingAlarmScheduler(alarmManager)

        if (BuildConfig.DEBUG) {
            Log.d("StatsActivity", "Debugging")
        }

        habitRepository.observeUserHabits(userId)
            .subscribe { habits ->
                habits
                    .filter { it.timeOfDay != null }
                    .forEach { habit ->
                        val habitTime = habit.timeForToday()

                        val pendingIntent =
                            PendingIntent.getActivity(
                                applicationContext,
                                0,
                                // TODO: link to specific habit somehow in intent
                                launchIntent(this, userId, null).apply {
                                    action = habit.id
                                },
                                0
                            )

                        val doneIntent = PendingIntent.getActivity(
                            applicationContext, 0, launchIntent(this, userId, habit.id).apply {
                                action = habit.id
                            },
                            0
                        )

                        val intent = HabitNotificationBroadcastReceiver.launchIntent(
                            this,
                            habit.name,
                            "Remember to ${habit.name}",
                            R.drawable.ic_videogame_asset_black_24dp,
                            tapIntent = pendingIntent,
                            notificationChannel = habitChannelId,
                            doneIntent = doneIntent
                        ).apply {
                            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                        }

                        alarmScheduler.scheduleRecurringIntent(
                            PendingIntent.getBroadcast(
                                this,
                                0,
                                intent,
                                PendingIntent.FLAG_ONE_SHOT
                            ),
                            habitTime.toDateTime().millis,
                            TimeUnit.DAYS.toMillis(1)
                        )
                    }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(channelId: String, channelName: String) {
        val channel =
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "description"
                enableLights(true)
                lightColor = Color.BLUE
            }

        val notificationManager =
            getSystemService<NotificationManager>(NotificationManager::class.java)

        notificationManager.createNotificationChannel(channel)
    }

    private fun Habit.timeForToday(): LocalDateTime {
        val timeOfDay = timeOfDay!!

        return LocalDateTime.now()
            .withHourOfDay(timeOfDay.hours)
            .withMinuteOfHour(timeOfDay.minutes)
    }

    class StatsPagerAdapter(fragmentManager: FragmentManager, private val userId: String) :
        FragmentPagerAdapter(fragmentManager) {

        private val titles = listOf("Habits", "Goals", "Map", "Inventory")

        override fun getPageTitle(position: Int): String {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> HabitFragment.newInstance(userId)
                1 -> GoalsFragment.newInstance(userId)
                2 -> GoalsFragment.newInstance(userId)
                3 -> HabitFragment.newInstance(userId)
                else -> throw IllegalStateException("Attempted to navigate too far")
            }
        }

        override fun getCount(): Int = 4
    }

    companion object {
        const val habitChannelId = "3000"
        const val habitChannelName = "Habit updates"

        private const val userIdKey = "userIdKey"
        private const val markHabitAsDoneKey = "markHabitAsDoneKey"

        fun launchIntent(context: Context, userId: String, markHabitAsDone: String?): Intent {
            return Intent(context, StatsActivity::class.java).apply {
                putExtra(userIdKey, userId)
                putExtra(markHabitAsDoneKey, markHabitAsDone)
            }
        }
    }
}
