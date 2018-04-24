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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_player.*
import javax.inject.Inject

@Subcomponent
interface PlayerActivitySubcomponent : AndroidInjector<PlayerActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<PlayerActivity>()
}

@Module(subcomponents = [PlayerActivitySubcomponent::class])
abstract class PlayerActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(PlayerActivity::class)
    abstract fun bindPlayerActivityInjectorFactory(builder: PlayerActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}

class PlayerActivity : AppCompatActivity() {
    @Inject
    lateinit var playerRepository: PlayerRepository

    private lateinit var userId: String
    private lateinit var presenter: PlayerPresenter

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)

        userId = intent.getStringExtra(userIdKey)
        presenter =
                PlayerPresenter(
                    playerRepository,
                    userId
                )
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable.add(
            presenter.viewStates()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (playerName, attack, agility, endurance, intelligence) ->
                    toolbar.title = playerName
                    attackBar.progress = attack.progress
                    agilityBar.progress = agility.progress
                    enduranceBar.progress = endurance.progress
                    intelligenceBar.progress = intelligence.progress
                }
        )
    }

    override fun onStop() {
        super.onStop()

        compositeDisposable.clear()
    }

    companion object {
        private const val userIdKey = "userIdKey"

        fun launchIntent(context: Context, userId: String): Intent =
            Intent(context, PlayerActivity::class.java).apply {
                putExtra(userIdKey, userId)
            }
    }
}