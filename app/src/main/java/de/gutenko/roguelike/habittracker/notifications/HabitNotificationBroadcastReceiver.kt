package de.gutenko.roguelike.habittracker.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import de.gutenko.roguelike.R

class HabitNotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HNS", "IT WOOOOOOOOOOOOOOOOOOOOOOOOOOORKS")
        val title = intent.getStringExtra(titleKey)!!
        val content = intent.getStringExtra(contentKey)!!
        val startIntent = intent.getParcelableExtra<PendingIntent>(startIntentKey)!!
        val doneIntent = intent.getParcelableExtra<PendingIntent>(doneIntentKey)!!
        val notificationChannel = intent.getStringExtra(notificationChannelKey)!!

        val smallIcon =
            if (intent.hasExtra(smallIconKey))
                intent.getIntExtra(smallIconKey, -1)
            else
                throw IllegalArgumentException()

        require(smallIcon != -1)

        val builder = NotificationCompat.Builder(context, notificationChannel)

        val notification = builder
            .setChannelId(notificationChannel)
            .setContentTitle(title)
            .setContentIntent(startIntent)
            .setContentText(content)
            .addAction(R.drawable.ic_videogame_asset_black_24dp, "DONE", doneIntent)
            .setSmallIcon(smallIcon)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val titleKey = "titleKey"
        private const val contentKey = "contentKey"
        private const val smallIconKey = "smallIconKey"
        private const val startIntentKey = "startIntentKey"
        private const val notificationChannelKey = "notificationChannelKey"
        private const val doneIntentKey = "doneIntentKey"

        fun launchIntent(
            context: Context,
            title: String,
            content: String, @DrawableRes smallIcon: Int,
            tapIntent: PendingIntent,
            notificationChannel: String,
            doneIntent: PendingIntent
        ): Intent {
            return Intent(context, HabitNotificationBroadcastReceiver::class.java).apply {
                putExtra(titleKey, title)
                putExtra(contentKey, content)
                putExtra(smallIconKey, smallIcon)
                putExtra(startIntentKey, tapIntent)
                putExtra(notificationChannelKey, notificationChannel)
                putExtra(doneIntentKey, doneIntent)
            }
        }
    }
}