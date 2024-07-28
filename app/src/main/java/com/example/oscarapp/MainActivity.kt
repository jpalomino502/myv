package com.example.oscarapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oscarapp.adapters.TicketAdapter
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

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statusTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var searchBar: EditText
    private var call: Call<TicketResponse>? = null

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

        // Solicitar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // Permiso ya concedido
            //openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido
                //openCamera()
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        // Aquí puedes inicializar la funcionalidad de la cámara
        Toast.makeText(this, "Cámara abierta", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop called")
        call?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }

    private fun fetchTickets() {
        val savedTickets = getSavedTickets()
        val userId = sharedPreferences.getString("userId", "") ?: ""

        if (savedTickets.isNotEmpty()) {
            adapter = TicketAdapter(savedTickets) { ticket ->
                openFormActivity(ticket)
            }
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
                            val tickets = it.tickets
                            adapter = TicketAdapter(tickets) { ticket ->
                                openFormActivity(ticket)
                            }
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
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun logout() {
        sharedPreferences.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
