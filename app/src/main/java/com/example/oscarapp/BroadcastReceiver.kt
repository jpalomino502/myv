package com.example.oscarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NetworkReceiver", "Network status changed")
        if (isInternetAvailable(context)) {
            Log.d("NetworkReceiver", "Network is connected, sending pending service requests")
            val serviceIntent = Intent(context, DataSyncService::class.java)
            context.startService(serviceIntent)
        } else {
            Log.d("NetworkReceiver", "Network is not available")
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network: Network? = connectivityManager.activeNetwork
        val networkCapabilities: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
