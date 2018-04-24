package de.gutenko.roguelike.habittracker

import android.app.Activity
import android.app.Application
import android.support.v4.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import de.gutenko.roguelike.habittracker.di.AppComponent
import de.gutenko.roguelike.habittracker.di.DaggerAppComponent
import de.gutenko.roguelike.habittracker.di.FirebaseAppModule
import net.danlew.android.joda.JodaTimeAndroid
import javax.inject.Inject

@Suppress("unused")
class RoguelikeApplication : Application(), HasSupportFragmentInjector, HasActivityInjector {
    @Inject
    lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)

        appComponent = DaggerAppComponent.builder()
            .firebaseAppModule(FirebaseAppModule)
            .build()

        appComponent.inject(this)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingFragmentInjector
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }
}