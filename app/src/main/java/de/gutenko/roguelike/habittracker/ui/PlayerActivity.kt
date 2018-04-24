package de.gutenko.roguelike.habittracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.ActivityKey
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.BR
import de.gutenko.roguelike.R
import de.gutenko.roguelike.databinding.AttributeCardItemBinding
import de.gutenko.roguelike.habittracker.androidLog
import de.gutenko.roguelike.habittracker.ui.PlayerPresenter.AttributeViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_player.toolBarLayout
import kotlinx.android.synthetic.main.activity_player.toolbar
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

    private lateinit var adapter: BindingListAdapter<AttributeCardItemBinding, AttributeViewState, Unit>

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userId = intent.getStringExtra(userIdKey)
        presenter =
                PlayerPresenter(
                    playerRepository,
                    userId
                )

        supportFragmentManager.beginTransaction()
            .add(R.id.goals, GoalsFragment.newInstance(userId))
            .commit()

        adapter =
                BindingListAdapter.Builder<AttributeCardItemBinding, PlayerPresenter.AttributeViewState, Unit>()
                    .identical { first, second -> first.name == second.name }
                    .same { first, second -> first == second }
                    .eventsFor { binding, i ->
                        Observable.empty()
                    }
                    .ids { it.name.hashCode().toLong() }
                    .variable(BR.attributeViewState)
                    .build(R.layout.attribute_card_item)


        val recyclerView = findViewById<RecyclerView>(R.id.attributeView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable.add(
            presenter.viewStates()
                .androidLog("Attributes")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { playerViewState ->
                    toolBarLayout.title = playerViewState.playerName

                    val (attack, agility, endurance, intelligence) = playerViewState.attributes

                    adapter.submitList(
                        listOf(
                            AttributeViewState(attack, "Attack", R.drawable.ic_gladius),
                            AttributeViewState(agility, "Agility", R.drawable.ic_running_ninja),
                            AttributeViewState(endurance, "Endurance", R.drawable.ic_atlas),
                            AttributeViewState(
                                intelligence,
                                "Intelligence",
                                R.drawable.ic_pointy_hat
                            )
                        )
                    )

                }
        )
    }

    override fun onStop() {
        super.onStop()

        compositeDisposable.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val userIdKey = "userIdKey"

        fun launchIntent(context: Context, userId: String): Intent =
            Intent(context, PlayerActivity::class.java).apply {
                putExtra(userIdKey, userId)
            }
    }
}