package com.example.oscarapp

import android.Manifest
import android.app.Activity
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.oscarapp.models.Ticket
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONException

class FormActivitya : AppCompatActivity() {

    private lateinit var serviceRadioButton: RadioButton
    private lateinit var reinforcementRadioButton: RadioButton
    private lateinit var serviceControlEditText: EditText
    private lateinit var razonSocialEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var nitEditText: EditText
    private lateinit var telefonoEditText: EditText
    private lateinit var celularEditText: EditText
    private lateinit var serviciosContainer: LinearLayout
    private lateinit var encuestaContainer: LinearLayout
    private lateinit var equipoProteccionContainer: LinearLayout
    private lateinit var fechaEditText: EditText
    private lateinit var horaIngresoEditText: EditText
    private lateinit var horaSalidaEditText: EditText
    private lateinit var fechaVencimientoEditText: EditText
    private lateinit var fechaRealizarEditText: EditText
    private lateinit var fechaProximoEditText: EditText
    private lateinit var fotoImageView: ImageView
    private lateinit var signatureView: SignatureView
    private lateinit var btnClear: Button
    private var ticketId: String? = null
    private lateinit var btnSave: Button
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_SELECT_PHOTO = 2
    private val REQUEST_PERMISSION = 3
    private val gradosJson = """
        [
            {"name": "Bajo", "value": "bajo", "checked": false},
            {"name": "Medio", "value": "medio", "checked": false},
            {"name": "Alto", "value": "alto", "checked": false}
        ]
    """.trimIndent()
    private val equipoJson = """
        [
            {"name": "BRGA - BATA", "checked": false},
            {"name": "CARRETA - TAPABACOS", "checked": false},
            {"name": "GUANTES", "checked": false},
            {"name": "TAPAOIDOS", "checked": false},
            {"name": "CASCO - CORRO", "checked": false},
            {"name": "BOTAS", "checked": false},
            {"name": "GAFAS", "checked": false}
        ]
    """.trimIndent()
    private val informeOptionsJson = """
        [
            {"label": "B", "checked": false},
            {"label": "M", "checked": false},
            {"label": "NT", "checked": false},
            {"label": "O", "checked": false}
        ]
    """.trimIndent()
    private val informeServiciosJson = """
        [
            {"label": "1. Espacio entre piso y puerta superior a 5 mm.", "options": $informeOptionsJson},
            {"label": "2. Enchapes y pisos adecuados y en buen estado.", "options": $informeOptionsJson},
            
        ]
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)
        serviciosContainer = findViewById(R.id.servicios_container)
        encuestaContainer = findViewById(R.id.encuesta_container)
        equipoProteccionContainer = findViewById(R.id.equipo_proteccion_container)
        fechaEditText = findViewById(R.id.fecha)
        horaIngresoEditText = findViewById(R.id.horaingreso)
        horaSalidaEditText = findViewById(R.id.horasalida)
        fechaVencimientoEditText = findViewById(R.id.fechavencimiento)
        fechaRealizarEditText = findViewById(R.id.fecharealizar)
        fechaProximoEditText = findViewById(R.id.fechaproximo)
        fechaEditText.setupDatePicker()
        horaIngresoEditText.setupTimePicker()
        horaSalidaEditText.setupTimePicker()
        fechaVencimientoEditText.setupDatePicker()
        fechaRealizarEditText.setupDateTimePicker()
        fechaProximoEditText.setupDateTimePicker()
        signatureView = findViewById(R.id.signature_view)
        btnClear = findViewById(R.id.btn_clear)
        btnSave = findViewById(R.id.btn_save)
        serviceRadioButton = findViewById(R.id.service)
        reinforcementRadioButton = findViewById(R.id.reinforcement)
        serviceControlEditText = findViewById(R.id.service_control)
        razonSocialEditText = findViewById(R.id.razon_social)
        direccionEditText = findViewById(R.id.direccion)
        nitEditText = findViewById(R.id.nit)
        telefonoEditText = findViewById(R.id.telefono)
        celularEditText = findViewById(R.id.celular)
        setCurrentDate()
        btnClear.setOnClickListener {
            signatureView.clear()
        }
        btnSave.setOnClickListener {
            saveSignature()
        }

        val ticket = intent.getParcelableExtra<Ticket>("ticket")
        ticket?.let {
            ticketId = it.id
            Log.d("FormActivity", "Ticket ID: $ticketId")
            autofillForm(it)
        }

        val jsonData = """
    [
        {
            "servicio": "Servicio 10",
            "plagas": [
                {"name": "Roedores (Ratones)", "value": "roedores", "checked": false},
                {"name": "Foto", "value": "no disponible"},  // Ajusta "value" si es necesario
                {"name": "Cantidad", "value": "0"}
            ],
            "metodos": [
                {"name": "Trampa Tubo PVC", "value": "trampa-tubo-pvc", "checked": false},
                {"name": "Trampa Cajas Cebadero", "value": "trampa-cajas-cebadero", "checked": false},
                {"name": "Trampas de Impacto Cocodrilo", "value": "trampas-de-impacto-cocodrilo", "checked": false},
                {"name": "Trampa Adherente Bandeja 48 RNHP Bandeja *2", "value": "trampa-adherente-bandeja-48-rnhp-bandeja-2", "checked": false},
                {"name": "Trampa Adherente Gato de Papel Neopreno", "value": "trampa-adherente-gato-de-papel-neopreno", "checked": false}
            ],
            "grados": $gradosJson
        }
    ]
""".trimIndent()
        val serviciosArray = JSONArray(jsonData)

        for (i in 0 until serviciosArray.length()) {
            val servicio = serviciosArray.getJSONObject(i)
            addServicioSection(servicio)
        }

        addEquipoProteccionSection()
        addEncuestaSection()
    }
    private fun saveSignature() {
        val bitmap = signatureView.getSignatureBitmap()
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signature_${Date().time}.png"
        )
        val outputStream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        Toast.makeText(this, "Signature saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
    private fun addEncuestaSection() {
        try {
            val encuestaArray = JSONArray(informeServiciosJson)

            for (i in 0 until encuestaArray.length()) {
                val item = encuestaArray.getJSONObject(i)
                val label = item.optString("label", "Sin etiqueta")
                val options = item.optJSONArray("options") ?: JSONArray()

                val encuestaLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 0, 0, 16)
                    elevation = 2f
                }
                val questionLabel = TextView(this).apply {
                    text = label
                    textSize = 18f
                    setTextColor(resources.getColor(android.R.color.black))
                    setPadding(0, 0, 0, 8)
                    paint.isFakeBoldText = false
                }
                encuestaLayout.addView(questionLabel)

                val radioGroup = RadioGroup(this).apply {
                    orientation = RadioGroup.HORIZONTAL
                    setPadding(0, 0, 0, 8)
                }
                for (j in 0 until options.length()) {
                    val option = options.optJSONObject(j) ?: continue
                    val radioButton = RadioButton(this).apply {
                        text = option.optString("label", "Sin opción")
                        setTextColor(resources.getColor(android.R.color.black))
                        buttonTintList = ColorStateList.valueOf(resources.getColor(android.R.color.black))
                        setPadding(0, 0, 0, 0)
                    }
                    radioGroup.addView(radioButton)
                }
                encuestaLayout.addView(radioGroup)
                encuestaContainer.addView(encuestaLayout)
            }
        } catch (e: JSONException) {
            Log.e("FormActivity", "Error al procesar el JSON: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("FormActivity", "Error inesperado: ${e.message}", e)
        }
    }
    private fun EditText.setupDatePicker() {
        isFocusable = false
        isFocusableInTouchMode = false
        isClickable = true
        setOnClickListener {
            hideKeyboard()
            showDatePickerDialog(this)
        }
    }
    private fun EditText.setupTimePicker() {
        isFocusable = false
        isFocusableInTouchMode = false
        isClickable = true
        setOnClickListener {
            hideKeyboard()
            showTimePickerDialog(this)
        }
    }
    private fun EditText.setupDateTimePicker() {
        isFocusable = false
        isFocusableInTouchMode = false
        isClickable = true
        setOnClickListener {
            hideKeyboard()
            showDateTimePickerDialog(this)
        }
    }
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            }, year, month, day
        )
        datePickerDialog.show()
    }
    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(selectedTime)
            }, hour, minute, true
        )

        timePickerDialog.show()
    }
    private fun showDateTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                val selectedDateTime = StringBuilder(selectedDate)

                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                val timePickerDialog = TimePickerDialog(
                    this, { _, selectedHour, selectedMinute ->
                        val selectedTime = String.format(" %02d:%02d", selectedHour, selectedMinute)
                        selectedDateTime.append(selectedTime)
                        editText.setText(selectedDateTime.toString())
                    }, hour, minute, true
                )
                timePickerDialog.show()
            }, year, month, day
        )
        datePickerDialog.show()
    }
    private fun addServicioSection(servicio: JSONObject) {
        val servicioLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
            elevation = 2f
        }
        val tituloServicio = TextView(this).apply {
            text = servicio.getString("servicio")
            textSize = 24f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 8)
            paint.isFakeBoldText = true
        }
        servicioLayout.addView(tituloServicio)
        val plagasTitulo = TextView(this).apply {
            text = "Plagas"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 8)
            paint.isFakeBoldText = true
        }
        servicioLayout.addView(plagasTitulo)
        val plagas = servicio.getJSONArray("plagas")
        for (i in 0 until plagas.length()) {
            val plaga = plagas.getJSONObject(i)
            when (plaga.getString("name")) {
                "Cantidad" -> {
                    val cantidadEditText = EditText(this).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER
                        hint = "Cantidad"
                        setPadding(16, 16, 16, 16)
                        setTextColor(Color.BLACK)
                        setHintTextColor(Color.BLACK)
                    }
                    servicioLayout.addView(cantidadEditText)
                }
                "Foto" -> {
                    val fotoButton = Button(this).apply {
                        text = "Tomar o Seleccionar Foto"
                        setOnClickListener { showPhotoDialog() }
                        setPadding(0, 0, 0, 8)
                        setBackgroundColor(Color.BLACK)
                    }
                    servicioLayout.addView(fotoButton)
                }
                else -> {
                    val checkBox = CheckBox(this).apply {
                        text = plaga.getString("name")
                        setTextColor(resources.getColor(android.R.color.black))
                        buttonTintList =
                            ColorStateList.valueOf(resources.getColor(android.R.color.black))
                        setPadding(0, 0, 0, 0)
                    }
                    servicioLayout.addView(checkBox)
                }
            }
        }
        val metodosTitulo = TextView(this).apply {
            text = "Métodos"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 8)
            paint.isFakeBoldText = true
        }
        servicioLayout.addView(metodosTitulo)
        val metodos = servicio.getJSONArray("metodos")
        for (i in 0 until metodos.length()) {
            val metodo = metodos.getJSONObject(i)
            val checkBox = CheckBox(this).apply {
                text = metodo.getString("name")
                setTextColor(resources.getColor(android.R.color.black))
                buttonTintList = ColorStateList.valueOf(resources.getColor(android.R.color.black))
                setPadding(0, 0, 0, 0)
            }

            servicioLayout.addView(checkBox)
        }
        val gradosTitulo = TextView(this).apply {
            text = "Grados"
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 8)
            paint.isFakeBoldText = true
        }
        servicioLayout.addView(gradosTitulo)
        val grados = JSONArray(gradosJson)
        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, 0, 0, 0)
        }
        for (i in 0 until grados.length()) {
            val grado = grados.getJSONObject(i)
            val radioButton = RadioButton(this).apply {
                text = grado.getString("name")
                setTextColor(resources.getColor(android.R.color.black))
                buttonTintList = ColorStateList.valueOf(resources.getColor(android.R.color.black))
                setPadding(0, 0, 0, 0)
            }
            radioGroup.addView(radioButton)
        }
        servicioLayout.addView(radioGroup)
        serviciosContainer.addView(servicioLayout)
    }
    private fun addEquipoProteccionSection() {
        val equipoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
            elevation = 2f
        }

        val equipoTitulo = TextView(this).apply {
            text = "EQUIPO DE PROTECCIÓN PERSONAL"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 8)
            paint.isFakeBoldText = true
        }
        equipoLayout.addView(equipoTitulo)
        val equipoArray = JSONArray(equipoJson)
        for (i in 0 until equipoArray.length()) {
            val item = equipoArray.getJSONObject(i)
            val checkBox = CheckBox(this).apply {
                text = item.getString("name")
                isChecked = item.getBoolean("checked")
                setTextColor(resources.getColor(android.R.color.black))
                buttonTintList = ColorStateList.valueOf(resources.getColor(android.R.color.black))
                setPadding(0, 0, 0, 0)
            }

            equipoLayout.addView(checkBox)
        }
        equipoProteccionContainer.addView(equipoLayout)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
    private fun showPhotoDialog() {
        val options = arrayOf("Tomar Foto", "Seleccionar de Galería")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Foto")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePhoto()
                1 -> selectPhoto()
            }
        }
        builder.show()
    }
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION
            )
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
    private fun selectPhoto() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION
            )
        } else {
            val selectPictureIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (selectPictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(selectPictureIntent, REQUEST_SELECT_PHOTO)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    fotoImageView.setImageBitmap(imageBitmap)
                    saveImage(imageBitmap)
                }
                REQUEST_SELECT_PHOTO -> {
                    val selectedImageUri: Uri? = data?.data
                    selectedImageUri?.let {
                        val bitmap = getBitmapFromUri(it)
                        bitmap?.let { bmp ->
                            fotoImageView.setImageBitmap(bmp)
                            saveImage(bmp)
                        }
                    }
                }
            }
        }
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }
    private fun saveImage(bitmap: Bitmap) {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image_${Date().time}.png"
        )
        val outputStream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        Toast.makeText(this, "Image saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
    private fun autofillForm(ticket: Ticket) {
        serviceControlEditText.setText(ticket.numTicket)
        razonSocialEditText.setText(ticket.cliente?.razonSocial)
        direccionEditText.setText(ticket.cliente?.direccion)
        nitEditText.setText(ticket.cliente?.nitCc)
        telefonoEditText.setText(ticket.cliente?.telefono)
        celularEditText.setText(ticket.cliente?.telefono)
    }
    private fun setCurrentDate() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        fechaEditText.setText(currentDate)
    }
}
