package com.example.oscarapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.withContext
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.models.Ticket
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.example.oscarapp.utils.DateJsonAdapter
import com.example.oscarapp.utils.DateTimeUtils
import com.example.oscarapp.utils.FormDataPopulator
import com.example.oscarapp.utils.FormDataStorage
import com.example.oscarapp.utils.FormUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormActivity : AppCompatActivity() {

    private lateinit var fechaEditText: EditText
    private lateinit var horaIngresoEditText: EditText
    private lateinit var horaSalidaEditText: EditText
    private lateinit var fechaVencimientoEditText: EditText
    private lateinit var fechaRealizarEditText: EditText
    private lateinit var fechaProximoEditText: EditText
    private lateinit var encuestaContainer: LinearLayout
    private lateinit var equiposContainer: LinearLayout
    private lateinit var serviceControlEditText: EditText
    private lateinit var razonSocialEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var signatureView: SignatureView
    private lateinit var nitEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var nombre_tecnico: EditText
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button
    private lateinit var celularEditText: EditText
    private lateinit var tipoDeServiciosEditText: EditText
    private lateinit var productoEditText: EditText
    private lateinit var autorizacion_clienteEditText: EditText
    private lateinit var recibi_clienteEditText: EditText

    private val networkReceiver = NetworkReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        // Inicialización de vistas
        tipoDeServiciosEditText = findViewById(R.id.titulo)
        fechaEditText = findViewById(R.id.fecha)
        horaIngresoEditText = findViewById(R.id.horaingreso)
        signatureView = findViewById(R.id.signature_view)
        horaSalidaEditText = findViewById(R.id.horasalida)
        fechaVencimientoEditText = findViewById(R.id.fechavencimiento)
        fechaRealizarEditText = findViewById(R.id.fecharealizar)
        fechaProximoEditText = findViewById(R.id.fechaproximo)
        serviceControlEditText = findViewById(R.id.service_control)
        razonSocialEditText = findViewById(R.id.razon_social)
        direccionEditText = findViewById(R.id.direccion)
        nitEditText = findViewById(R.id.nit)
        telefonoEditText = findViewById(R.id.telefono)
        nombre_tecnico = findViewById(R.id.nombre_tecnico)
        btnClear = findViewById(R.id.btn_clear)
        btnSave = findViewById(R.id.btnSave)
        celularEditText = findViewById(R.id.celular)
        productoEditText = findViewById(R.id.producto)
        recibi_clienteEditText = findViewById(R.id.recibi_cliente)
        autorizacion_clienteEditText = findViewById(R.id.autorizacion_cliente)

        val serviceRadioGroup = findViewById<RadioGroup>(R.id.service_radio_group)
        encuestaContainer = findViewById(R.id.encuesta_container)
        equiposContainer = findViewById(R.id.equipo_proteccion_container)
        val serviciosContainer = findViewById<LinearLayout>(R.id.servicios_container)

        // Configuración de DateTimePickers
        DateTimeUtils.setupDatePicker(fechaEditText, this)
        DateTimeUtils.setupTimePicker(horaIngresoEditText, this)
        DateTimeUtils.setupTimePicker(horaSalidaEditText, this)
        DateTimeUtils.setupDatePicker(fechaVencimientoEditText, this)
        DateTimeUtils.setupDateTimePicker(fechaRealizarEditText, this)
        DateTimeUtils.setupDateTimePicker(fechaProximoEditText, this)

        // Configuración de botones
        btnClear.setOnClickListener {
            signatureView.clear()
        }

        btnSave.setOnClickListener {
            if (isFormValid()) {
                btnSave.isEnabled = false // Deshabilitar el botón
                saveFormData()
            }
        }

        // Obtener el Ticket desde el intent
        val ticket = intent.getParcelableExtra<Ticket>("ticket")
        ticket?.let {
            // Autofill del formulario con los datos del Ticket
            FormUtils.autofillForm(
                this,
                serviceControlEditText,
                razonSocialEditText,
                direccionEditText,
                nitEditText,
                telefonoEditText,
                celularEditText,
                fechaEditText,
                tipoDeServiciosEditText,
                productoEditText,
                nombre_tecnico,
                autorizacion_clienteEditText,
                recibi_clienteEditText,
                serviceRadioGroup,
                it
            )

            // Rellenar la encuesta
            FormDataPopulator.populateEncuesta(this, encuestaContainer)

            // Rellenar los equipos de protección
            val diligencia = it.diligencias.firstOrNull()
            diligencia?.let { d ->
                val equiposJson = d.equiposJson
                FormDataPopulator.populateEquipos(this, equiposContainer, equiposJson)

                // Rellenar los servicios
                val serviciosJson = d.serviciosJson
                FormDataPopulator.populateServicios(this, serviciosContainer, serviciosJson)
            }
        }

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }

    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isFormValid(): Boolean {
        val tipoServicioId = findViewById<RadioGroup>(R.id.service_radio_group).checkedRadioButtonId
        val firmaBitmap = signatureView.getSignatureBitmap()

        return when {
            tipoServicioId == -1 -> {
                showAlert("Por favor seleccione un tipo de servicio")
                false
            }
            firmaBitmap == null -> {
                showAlert("La firma es obligatoria")
                false
            }
            else -> {
                val firmaBase64 = encodeToBase64(firmaBitmap)
                if (firmaBase64.length < 4615) {
                    showAlert("La firma es obligatoria")
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun saveFormData() {
        val tipoServicioId = findViewById<RadioGroup>(R.id.service_radio_group).checkedRadioButtonId
        val tipoServicioSeleccionado = when (tipoServicioId) {
            R.id.service -> "servicio"
            R.id.reinforcement -> "refuerzo"
            else -> ""
        }

        val tipoPagoId = findViewById<RadioGroup>(R.id.tipo_pago_radio_group).checkedRadioButtonId
        val tipoPagoSeleccionado = when (tipoPagoId) {
            R.id.valorcredito -> "credito"
            R.id.valorcontado -> "contado"
            else -> ""
        }

        val ticket = intent.getParcelableExtra<Ticket>("ticket") ?: return

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val fechaTexto = fechaEditText.text.toString()
        val fecha: Date = try {
            dateFormat.parse(fechaTexto) ?: Date()
        } catch (e: Exception) {
            Date()
        }

        val fechavencimiento: Date? = try {
            dateFormat.parse(fechaVencimientoEditText.text.toString())
        } catch (e: Exception) {
            null
        }

        val fecharealizar: Date? = try {
            dateTimeFormat.parse(fechaRealizarEditText.text.toString())
        } catch (e: Exception) {
            null
        }

        val fechaproximo: Date? = try {
            dateTimeFormat.parse(fechaProximoEditText.text.toString())
        } catch (e: Exception) {
            null
        }

        val encuestaJsonString = FormDataStorage.obtenerEncuestaSeleccionada(encuestaContainer)

        val titulo = tipoDeServiciosEditText.text.toString()

        val sharedPreferences = getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        val base64Image = sharedPreferences.getString("base64_image", "")

        val firmaBitmap = signatureView.getSignatureBitmap()
        val firmaBase64 = firmaBitmap?.let { encodeToBase64(it) } ?: ""

        // Obtener foto_novedad desde SharedPreferences
        val fotoNovedadBase64 = sharedPreferences.getString("foto_novedad", "")

        // Condición para verificar si base64Image está presente
        val imageBase64 = if (base64Image.isNullOrEmpty()) "" else base64Image

        // Condición para verificar si foto_novedad está presente
        val fotoNovedad = if (fotoNovedadBase64.isNullOrEmpty()) "" else fotoNovedadBase64

        val serviceRequest = ServiceRequest(
            ticketId = ticket.id,
            serviceControl = ticket.numTicket,
            tipoServicio = tipoServicioSeleccionado,
            fecha = fecha,
            razonSocial = razonSocialEditText.text.toString(),
            direccion = direccionEditText.text.toString(),
            nit = nitEditText.text.toString(),
            telefono = telefonoEditText.text.toString(),
            celular = celularEditText.text.toString(),
            observaciones1 = findViewById<EditText>(R.id.observaciones1).text.toString(),
            horaingreso = horaIngresoEditText.text.toString(),
            horasalida = horaSalidaEditText.text.toString(),
            producto = findViewById<EditText>(R.id.producto).text.toString(),
            dosificacion = findViewById<EditText>(R.id.dosificacion).text.toString(),
            concentracion = findViewById<EditText>(R.id.concentracion).text.toString(),
            cantidad = findViewById<EditText>(R.id.cantidad).text.toString(),
            observaciones2 = findViewById<EditText>(R.id.observaciones2).text.toString(),
            valortotal = findViewById<EditText>(R.id.valortotal).text.toString(),
            tipopago = tipoPagoSeleccionado,
            fechavencimiento = fechavencimiento,
            fecharealizar = fecharealizar,
            fechaproximo = fechaproximo,
            autorizacion_cliente = findViewById<EditText>(R.id.autorizacion_cliente).text.toString(),
            nombre_asesor = findViewById<EditText>(R.id.nombre_asesor).text.toString(),
            recibi_cliente = findViewById<EditText>(R.id.recibi_cliente).text.toString(),
            nombre_tecnico = findViewById<EditText>(R.id.nombre_tecnico).text.toString(),
            ulr_ratones = imageBase64,
            firma = firmaBase64,
            observaciones3 = "",
            informeserviciojson = encuestaJsonString,
            titulo = titulo,
            foto_novedad = fotoNovedad
        )

        sendDataToServer(serviceRequest)
    }


    private fun encodeToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return "data:image/svg+xml;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }


    private fun sendDataToServer(serviceRequest: ServiceRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                val response = apiService.sendServiceRequest(serviceRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FormActivity, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                        clearLocalData()
                        clearBase64Image() // Limpiar base64Image después de enviar los datos
                        btnSave.isEnabled = true // Rehabilitar el botón
                        finish()
                    } else {
                        Toast.makeText(this@FormActivity, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        saveDataLocally(serviceRequest)
                        clearBase64Image() // Limpiar base64Image incluso si falla el envío
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    saveDataLocally(serviceRequest)
                    clearBase64Image() // Limpiar base64Image en caso de excepción
                }
            }
        }
    }


    private fun clearBase64Image() {
        val sharedPreferences = getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("base64_image")
            apply()
        }
    }


    private fun saveDataLocally(serviceRequest: ServiceRequest) {
        val moshi = Moshi.Builder()
            .add(DateJsonAdapter()) // Agrega este adaptador
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(ServiceRequest::class.java)
        val json = jsonAdapter.toJson(serviceRequest)

        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val existingData = sharedPreferences.getString("service_request_data_list", null)
        val serviceRequests: MutableList<ServiceRequest> = if (existingData != null) {
            val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
            val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
            listAdapter.fromJson(existingData) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        serviceRequests.add(serviceRequest)

        val updatedJson = moshi.adapter<MutableList<ServiceRequest>>(Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)).toJson(serviceRequests)
        editor.putString("service_request_data_list", updatedJson)
        editor.apply()

        runOnUiThread {
            Toast.makeText(this, "Datos guardados localmente", Toast.LENGTH_SHORT).show()
            btnSave.isEnabled = true // Rehabilitar el botón
            finish()
        }
    }

    private fun clearLocalData() {
        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Log.d("FormActivity", "Datos locales borrados de SharedPreferences")
    }

    private fun checkAndSendLocalData() {
        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        val json = sharedPreferences.getString("service_request_data_list", null)

        if (json != null) {
            val moshi = Moshi.Builder()
                .add(DateJsonAdapter()) // Agrega este adaptador
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(MutableList::class.java, ServiceRequest::class.java)
            val listAdapter = moshi.adapter<MutableList<ServiceRequest>>(type)
            val serviceRequests = listAdapter.fromJson(json) ?: mutableListOf()

            val serviceRequestsCopy = serviceRequests.toMutableList() // Crear una copia de la lista original

            CoroutineScope(Dispatchers.IO).launch {
                serviceRequestsCopy.forEach { serviceRequest ->
                    try {
                        val apiService = RetrofitClient.retrofitInstance.create(ApiService::class.java)
                        val response = apiService.sendServiceRequest(serviceRequest)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                serviceRequests.remove(serviceRequest)
                                Toast.makeText(this@FormActivity, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@FormActivity, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@FormActivity, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
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


    private inner class NetworkReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isInternetAvailable()) {
                checkAndSendLocalData()
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network: Network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities: NetworkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FormUtils.handleImageResult(this, requestCode, resultCode, data)
    }
}

fun ServiceRequest.toJson(): String {
    val moshi = Moshi.Builder()
        .add(DateJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter = moshi.adapter(ServiceRequest::class.java)
    return jsonAdapter.toJson(this)
}