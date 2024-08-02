package com.example.oscarapp

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.example.oscarapp.utils.DateJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataSyncService : IntentService("DataSyncService") {

    override fun onHandleIntent(intent: Intent?) {
        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("service_request_data_list", null)

        if (json != null) {
            Log.d("DataSyncService", "Data found in SharedPreferences")
            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
            val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
            val serviceRequests = listAdapter.fromJson(json) ?: mutableListOf()

            if (serviceRequests.isEmpty()) {
                Log.d("DataSyncService", "No service requests found")
                return
            }

            val serviceRequestsCopy = serviceRequests.toMutableList()

            CoroutineScope(Dispatchers.IO).launch {
                serviceRequestsCopy.forEach { serviceRequest ->
                    try {
                        Log.d("DataSyncService", "Sending request: $serviceRequest")
                        val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                        val response = apiService.sendServiceRequest(serviceRequest)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Log.d("DataSyncService", "Request successful: $serviceRequest")
                                Toast.makeText(this@DataSyncService, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                                serviceRequests.remove(serviceRequest)
                            } else {
                                Log.e("DataSyncService", "Error sending request: ${response.errorBody()?.string()}")
                                Toast.makeText(this@DataSyncService, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e("DataSyncService", "Exception sending request: ${e.message}")
                            Toast.makeText(this@DataSyncService, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    val updatedJson = moshi.adapter<MutableList<ServiceRequest>>(type).toJson(serviceRequests)
                    sharedPreferences.edit()
                        .putString("service_request_data_list", updatedJson)
                        .apply()

                    if (serviceRequests.isEmpty()) {
                        clearLocalData()
                    }
                }
            }
        } else {
            Log.d("DataSyncService", "No data found in SharedPreferences")
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
