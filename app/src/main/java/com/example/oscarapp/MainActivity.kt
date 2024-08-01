package com.example.oscarapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oscarapp.adapters.TicketAdapter
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.models.Ticket
import com.example.oscarapp.models.TicketResponse
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.example.oscarapp.utils.DateJsonAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val STORAGE_PERMISSION_REQUEST_CODE = 101
        private const val NOTIFICATIONS_PERMISSION_REQUEST_CODE = 102
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var statusTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var searchBar: EditText
    private var call: Call<TicketResponse>? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 5000

    private lateinit var requestWritePermissionLauncher: ActivityResultLauncher<IntentSenderRequest>
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

        requestNecessaryPermissions()

        requestWritePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show()
            }
        }

        sendPendingServiceRequests()

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        // Iniciar la actualización periódica de tickets
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

    private fun requestNecessaryPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATIONS_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = mutableListOf(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uris.add(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }

            val writeRequest = MediaStore.createWriteRequest(contentResolver, uris)
            val intentSenderRequest = IntentSenderRequest.Builder(writeRequest).build()
            requestWritePermissionLauncher.launch(intentSenderRequest)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
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

    override fun onResume() {
        super.onResume()
        sendPendingServiceRequests()
    }

    private fun fetchTickets() {
        val savedTickets = getSavedTickets()
        val userId = sharedPreferences.getString("userId", "") ?: ""

        if (savedTickets.isNotEmpty()) {
            val filteredTickets = savedTickets.filter { it.estado != "cerrado" }
            adapter = TicketAdapter(filteredTickets) { ticket ->
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
                            val tickets = it.tickets.filter { ticket -> ticket.estado != "cerrado" }
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

    private fun sendPendingServiceRequests() {
        if (isNetworkConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                val serviceRequestsJson = sharedPreferences.getString("service_request_data_list", null)
                if (serviceRequestsJson != null) {
                    val moshi = Moshi.Builder()
                        .add(DateJsonAdapter())
                        .build()
                    val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
                    val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
                    val serviceRequests = listAdapter.fromJson(serviceRequestsJson) ?: mutableListOf()

                    serviceRequests.forEach { serviceRequest ->
                        try {
                            val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                            val response = apiService.sendServiceRequest(serviceRequest)

                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    Log.d("MainActivity", "Datos enviados exitosamente")
                                    removeSentData(serviceRequest)
                                } else {
                                    Toast.makeText(this@MainActivity, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    clearLocalData()
                }
            }
        }
    }

    private fun removeSentData(serviceRequest: ServiceRequest) {
        val serviceRequestsJson = sharedPreferences.getString("service_request_data_list", null)
        val moshi = Moshi.Builder()
            .add(DateJsonAdapter())
            .build()
        val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
        val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
        val serviceRequests = listAdapter.fromJson(serviceRequestsJson) ?: mutableListOf()

        serviceRequests.remove(serviceRequest)

        val updatedJson = moshi.adapter<MutableList<ServiceRequest>>(type).toJson(serviceRequests)
        sharedPreferences.edit().putString("service_request_data_list", updatedJson).apply()
    }

    private fun clearLocalData() {
        sharedPreferences.edit().clear().apply()
        Log.d("MainActivity", "Datos locales borrados de SharedPreferences")
    }

    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isNetworkConnected()) {
                sendPendingServiceRequests()
            }
            checkNetworkStatus()
        }
    }
}
