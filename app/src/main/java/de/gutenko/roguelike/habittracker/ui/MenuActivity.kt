package de.gutenko.roguelike.habittracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.ActivityKey
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.R
import de.gutenko.roguelike.loop.MainActivity
import kotlinx.android.synthetic.main.activity_menu.newGameButton
import kotlinx.android.synthetic.main.activity_menu.statsButton
import javax.inject.Inject

@Subcomponent
interface MenuActivitySubcomponent : AndroidInjector<MenuActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MenuActivity>()
}

@Module(subcomponents = [MenuActivitySubcomponent::class])
abstract class MenuActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(MenuActivity::class)
    abstract fun bindMenuActivityInjectorFactory(builder: MenuActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}


class MenuActivity : AppCompatActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepository

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        userId = intent.getStringExtra(userIdKey)

        playerRepository.hasPlayer(userId)
            .subscribe { hasPlayer ->
                if (hasPlayer) {
                    newGameButton.setOnClickListener {
                        startActivity(Intent(this, MainActivity::class.java))
                    }

                    statsButton.setOnClickListener {
                        startActivity(
                            StatsActivity.launchIntent(
                                this,
                                userId,
                                markHabitAsDone = null
                            )
                        )
                    }

                } else {
                    startActivity(CreatePlayerActivity.newInstance(this, userId))
                }
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
