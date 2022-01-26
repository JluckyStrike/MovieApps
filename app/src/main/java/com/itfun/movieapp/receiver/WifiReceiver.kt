package com.itfun.movieapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.widget.Toast

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val  info = intent?.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null && info.isConnected){
            Toast.makeText(context, "WIFI IS ENABLED", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "WIFI IS DISABLED", Toast.LENGTH_SHORT).show()
        }
    }
}