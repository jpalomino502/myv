package com.example.oscarapp.utils

import android.content.Context
import android.widget.Toast
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ServiceUtils {
    fun sendPendingServiceRequests(context: Context) {
        val sharedPreferences = context.getSharedPreferences("local_data_prefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("service_request_data_list", null)

        if (json != null) {
            val moshi = Moshi.Builder()
                .add(DateJsonAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
            val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
            val serviceRequests = listAdapter.fromJson(json) ?: mutableListOf()

            CoroutineScope(Dispatchers.IO).launch {
                serviceRequests.forEach { serviceRequest ->
                    try {
                        val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                        val response = apiService.sendServiceRequest(serviceRequest)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                serviceRequests.remove(serviceRequest)
                                Toast.makeText(context, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // Actualizar los datos locales después de intentar enviar todos los formularios
                withContext(Dispatchers.Main) {
                    val updatedJson = moshi.adapter<MutableList<ServiceRequest>>(Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)).toJson(serviceRequests)
                    val editor = sharedPreferences.edit()
                    editor.putString("service_request_data_list", updatedJson)
                    editor.apply()
                }
            }
        }
    }
}