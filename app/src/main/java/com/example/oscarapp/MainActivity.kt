package com.example.oscarapp

import TicketAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oscarapp.models.Ticket
import com.example.oscarapp.models.TicketResponse
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statusTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var searchBar: EditText
    private var call: Call<TicketResponse>? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 5000

    private val networkReceiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        statusTextView = findViewById(R.id.statusTextView)
        logoutButton = findViewById(R.id.logoutButton)
        searchBar = findViewById(R.id.searchBar)

        logoutButton.setOnClickListener {
            logout()
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fetchTickets()
        checkNetworkStatus()

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        startPeriodicUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
        stopPeriodicUpdates()
    }

    private fun startPeriodicUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchTickets()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun stopPeriodicUpdates() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        // No longer calling sendPendingServiceRequests here
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }

    private fun fetchTickets() {
        val savedTickets = getSavedTickets()
        val userId = sharedPreferences.getString("userId", "") ?: ""

        val localTickets = getLocalTicketIds()
        Log.d("FetchTickets", "Local ticket IDs: ${localTickets.joinToString(",")}")

        if (savedTickets.isNotEmpty()) {
            val filteredTickets = savedTickets.filter { it.estado != "cerrado" }
            adapter = TicketAdapter(filteredTickets, { ticket ->
                openFormActivity(ticket)
            }, localTickets)
            recyclerView.adapter = adapter
        }

        val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
        call = apiService.getTickets(userId)

        call?.enqueue(object : Callback<TicketResponse> {
            override fun onResponse(call: Call<TicketResponse>, response: Response<TicketResponse>) {
                if (response.isSuccessful) {
                    val ticketResponse = response.body()
                    ticketResponse?.let {
                        runOnUiThread {
                            val tickets = it.tickets.filter { ticket -> ticket.estado != "cerrado" }
                            adapter = TicketAdapter(tickets, { ticket ->
                                openFormActivity(ticket)
                            }, localTickets)
                            recyclerView.adapter = adapter
                            saveTickets(tickets)
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Exception: ${t.message}")
            }
        })
    }

    private fun saveTickets(tickets: List<Ticket>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(tickets)
        editor.putString("savedTickets", json)
        editor.apply()
    }

    private fun getSavedTickets(): List<Ticket> {
        val gson = Gson()
        val json = sharedPreferences.getString("savedTickets", null)
        val type = object : TypeToken<List<Ticket>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun openFormActivity(ticket: Ticket) {
        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra("ticket", ticket)
        startActivity(intent)
    }

    private fun checkNetworkStatus() {
        val isOnline = isNetworkConnected()
        statusTextView.text = if (isOnline) "Estado: En línea" else "Estado: Fuera de línea"
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            activeNetwork?.isConnectedOrConnecting == true
        }
    }

    private fun logout() {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("NetworkReceiver", "Network status changed")
            if (isNetworkConnected()) {
                Log.d("NetworkReceiver", "Network is connected, triggering DataSyncService")
                val serviceIntent = Intent(this@MainActivity, DataSyncService::class.java)
                startService(serviceIntent)
            } else {
                Log.d("NetworkReceiver", "Network is not connected")
            }
            checkNetworkStatus()
        }
    }

    private fun getLocalTicketIds(): List<String> {
        val sharedPreferences = getSharedPreferences("local_data_prefs", Context.MODE_PRIVATE)
        val idsString = sharedPreferences.getString("localTicketIds", "")
        Log.d("GetLocalTicketIds", "IDs from SharedPreferences: $idsString")
        return idsString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
}
