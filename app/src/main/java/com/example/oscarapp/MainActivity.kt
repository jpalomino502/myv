package com.example.oscarapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
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


    private lateinit var requestWritePermissionLauncher: ActivityResultLauncher<IntentSenderRequest>

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

        // Solicitar permisos necesarios
        requestNecessaryPermissions()

        // Start DataSyncService from here
        val intent = Intent(this, DataSyncService::class.java)
        startService(intent)
        Log.d("MainActivity", "DataSyncService started from MainActivity")

        // Inicializar el lanzador de solicitud de permiso de escritura
        requestWritePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para solicitar permisos necesarios
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

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), STORAGE_PERMISSION_REQUEST_CODE)
        }

    }

    // Manejar el resultado de la solicitud de permisos
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
            STORAGE_PERMISSION_REQUEST_CODE -> {
                val readPermissionGranted = permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE) >= 0 &&
                        grantResults[permissions.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)] == PackageManager.PERMISSION_GRANTED
                if (readPermissionGranted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        requestWritePermission()
                    } else {
                        Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
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
            // En versiones anteriores a Android 11 (API 30), no se necesita la solicitud especial
            Toast.makeText(this, "Permiso de almacenamiento concedido", Toast.LENGTH_SHORT).show()
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
}
