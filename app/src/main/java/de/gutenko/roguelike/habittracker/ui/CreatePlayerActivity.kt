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
import de.gutenko.roguelike.habittracker.data.player.Player
import kotlinx.android.synthetic.main.activity_create_player.playerName
import kotlinx.android.synthetic.main.activity_create_player.submitButton
import javax.inject.Inject

@Subcomponent
interface CreatePlayerActivitySubcomponent : AndroidInjector<CreatePlayerActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<CreatePlayerActivity>()
}

@Module(subcomponents = [CreatePlayerActivitySubcomponent::class])
abstract class CreatePlayerActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(CreatePlayerActivity::class)
    abstract fun bindPlayerActivityInjectorFactory(builder: CreatePlayerActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}

class CreatePlayerActivity : AppCompatActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepository

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_player)

        userId = intent.getStringExtra(userIdKey)

        submitButton.setOnClickListener {
            playerRepository.addPlayer(Player(userId, playerName.text.toString(), 0, 0, 0, 0))
                .subscribe {
                    startActivity(StatsActivity.launchIntent(this, userId, null))
                }
        }
    }

    companion object {
        private const val userIdKey = "userIdKey"

        fun launchIntent(context: Context, userId: String): Intent =
            Intent(context, CreatePlayerActivity::class.java).apply {
                putExtra(userIdKey, userId)
            }
    }
}