package com.example.laroullete

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

object MyBroadcast {
    fun sout(context: Context) {
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                android.util.Log.d("mytag", "Hello from broadcast")
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction("myActionInBroadcast")
        }
        context.registerReceiver(br, intentFilter)
    }
}