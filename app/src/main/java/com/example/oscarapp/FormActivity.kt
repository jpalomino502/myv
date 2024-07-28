package com.example.oscarapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oscarapp.models.Ticket
import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.network.ApiService
import com.example.oscarapp.network.RetrofitClient
import com.example.oscarapp.utils.DateTimeUtils
import com.example.oscarapp.utils.FormDataPopulator
import com.example.oscarapp.utils.FormDataStorage
import com.example.oscarapp.utils.FormUtils
import com.squareup.moshi.Moshi
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
    private lateinit var serviciosContainer: LinearLayout
    private lateinit var encuestaContainer: LinearLayout
    private lateinit var equipoProteccionContainer: LinearLayout
    private lateinit var serviceControlEditText: EditText
    private lateinit var razonSocialEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var signatureView: SignatureView
    private lateinit var nitEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button
    private lateinit var celularEditText: EditText
    private lateinit var tipoDeServiciosEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

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
        btnClear = findViewById(R.id.btn_clear)
        btnSave = findViewById(R.id.btnSave)
        celularEditText = findViewById(R.id.celular)

        btnClear.setOnClickListener {
            signatureView.clear()
        }

        btnSave.setOnClickListener {
            if (isFormValid()) {
                saveFormData()
            }
        }

        DateTimeUtils.setupDatePicker(fechaEditText, this)
        DateTimeUtils.setupTimePicker(horaIngresoEditText, this)
        DateTimeUtils.setupTimePicker(horaSalidaEditText, this)
        DateTimeUtils.setupDatePicker(fechaVencimientoEditText, this)
        DateTimeUtils.setupDateTimePicker(fechaRealizarEditText, this)
        DateTimeUtils.setupDateTimePicker(fechaProximoEditText, this)

        serviciosContainer = findViewById(R.id.servicios_container)
        encuestaContainer = findViewById(R.id.encuesta_container)
        equipoProteccionContainer = findViewById(R.id.equipo_proteccion_container)

        FormDataPopulator.populateServicios(this, serviciosContainer)
        FormDataPopulator.populateEncuesta(this, encuestaContainer)
        FormDataPopulator.populateEquipoProteccion(this, equipoProteccionContainer)

        val ticket = intent.getParcelableExtra<Ticket>("ticket")
        ticket?.let {
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
                it
            )
        }
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
            else -> true
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
        val equiposJsonString = FormDataStorage.obtenerEquiposSeleccionados(equipoProteccionContainer)
        val serviciosJsonString = FormDataStorage.obtenerServiciosSeleccionados(serviciosContainer)

        val titulo = tipoDeServiciosEditText.text.toString()
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
            equiposjson = equiposJsonString,
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
            ulr_ratones = "",
            firma = signatureView.getSignatureBitmap()?.let { encodeToBase64(it) } ?: "",
            observaciones3 = "",
            informeserviciojson = encuestaJsonString,
            serviciosjson = serviciosJsonString,
            titulo = titulo,
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

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@FormActivity, "Datos enviados exitosamente", Toast.LENGTH_SHORT).show()
                        clearLocalData()
                        finish()
                    } else {
                        Toast.makeText(this@FormActivity, "Error al enviar datos: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        saveDataLocally(serviceRequest)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@FormActivity, "Excepción al enviar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                saveDataLocally(serviceRequest)
            }
        }
    }

    private fun saveDataLocally(serviceRequest: ServiceRequest) {
        val json = serviceRequest.toJson()

        val sharedPreferences = getSharedPreferences("local_data_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putString("service_request_data", json)
        editor.apply()

        runOnUiThread {
            Toast.makeText(this, "Datos guardados localmente", Toast.LENGTH_SHORT).show()
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
}

fun ServiceRequest.toJson(): String {
    val moshi = Moshi.Builder().build()
    val jsonAdapter = moshi.adapter(ServiceRequest::class.java)
    return jsonAdapter.toJson(this)
}