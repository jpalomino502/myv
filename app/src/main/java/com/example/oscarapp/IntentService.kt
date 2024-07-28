package com.example.oscarapp

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.example.oscarapp.utils.DateJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataSyncService : IntentService("DataSyncService") {

    override fun onHandleIntent(intent: Intent?) {
        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("service_request_data", null)

        if (json != null) {
            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter = moshi.adapter(ServiceRequest::class.java)
            val serviceRequest = jsonAdapter.fromJson(json)

            serviceRequest?.let {
                sendDataToServer(it)
            }
        }
    }

    private fun sendDataToServer(serviceRequest: ServiceRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                val response = apiService.sendServiceRequest(serviceRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@DataSyncService, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                        clearLocalData()
                        LocalBroadcastManager.getInstance(this@DataSyncService).sendBroadcast(Intent("DATA_SYNC_SUCCESS"))
                    } else {
                        Toast.makeText(this@DataSyncService, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DataSyncService, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearLocalData() {
        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Log.d("DataSyncService", "Datos locales borrados de SharedPreferences")
    }
}
