package de.gutenko.roguelike.habittracker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.google.firebase.auth.FirebaseAuth
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.ui.SignInActivity.Request.FIREBASE_LOGIN_UI

class SignInActivity : AppCompatActivity() {
    enum class Request(val requestCode: Int) {
        FIREBASE_LOGIN_UI(123)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            val options = mutableListOf(
                EmailBuilder()
                    .setAllowNewAccounts(true)
                    .build(),
                GoogleBuilder()
                    .build()
            )

            val authUI = AuthUI.getInstance()

            val intent =
                authUI.createSignInIntentBuilder()
                    .setAvailableProviders(options)
                    .build()

            startActivityForResult(intent, 0)
        } else {
            startActivity(MenuActivity.launchIntent(this, currentUser.uid))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FIREBASE_LOGIN_UI.requestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    startActivity(
                        MenuActivity.launchIntent(
                            this,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                    )
                }
            }
        }
    }
}