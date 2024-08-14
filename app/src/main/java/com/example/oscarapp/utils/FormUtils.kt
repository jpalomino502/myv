package com.example.oscarapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import com.example.oscarapp.R
import com.example.oscarapp.models.Ticket
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FormUtils {
    const val REQUEST_IMAGE_SELECT = 2
    private const val PERMISSION_REQUEST_CODE = 100
    private var serviceImageView: ImageView? = null
    private var serviceImageCallback: ((String) -> Unit)? = null
    private var additionalImageView: ImageView? = null
    private var additionalImageCallback: ((String) -> Unit)? = null

    fun autofillForm(
        context: Context,
        serviceControlEditText: EditText,
        razonSocialEditText: EditText,
        direccionEditText: EditText,
        nitEditText: EditText,
        telefonoEditText: EditText,
        celularEditText: EditText,
        fechaEditText: EditText,
        tipoDeServiciosEditText: EditText,
        productoEditText: EditText,
        nombre_tecnico: EditText,
        autorizacionClienteEditText: EditText,
        recibiClienteEditText: EditText,
        serviceRadioGroup: RadioGroup,
        ticket: Ticket,
        fechaproximoEditText: EditText,
        fecharealizarEditText: EditText,
        tipoPagoRadioGroup: RadioGroup,
        fechavencimientoEditText: EditText,
        dosificacionEditText: EditText,
        concentracionEditText: EditText,
        cantidadEditText: EditText,
        valortotalEditText: EditText,
        nombre_asesorEditText: EditText
    ) {
        // Formatos de fecha
        val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Establece la fecha actual en el campo de fecha (solo fecha)
        val currentDate = dateOnlyFormat.format(Date())
        fechaEditText.setText(currentDate)
        // Configura los campos como no editables
        fechaEditText.isFocusable = false
        fechaEditText.isFocusableInTouchMode = false
        fechaEditText.isEnabled = false

        // Establece los valores para los otros campos
        serviceControlEditText.setText(ticket.numTicket)
        razonSocialEditText.setText(ticket.cliente?.razonSocial)
        direccionEditText.setText(ticket.ubicacionActividad)
        nitEditText.setText(ticket.cliente?.nitCc)
        telefonoEditText.setText(ticket.cliente?.telefono)
        celularEditText.setText(ticket.cliente?.celular)
        tipoDeServiciosEditText.setText(ticket.titulo)
        dosificacionEditText.setText(ticket.diligencias.firstOrNull()?.dosificacion)
        concentracionEditText.setText(ticket.diligencias.firstOrNull()?.concentracion)
        cantidadEditText.setText(ticket.diligencias.firstOrNull()?.cantidad)
        valortotalEditText.setText(ticket.diligencias.firstOrNull()?.valortotal)
        nombre_asesorEditText.setText(ticket.diligencias.firstOrNull()?.nombre_asesor)
        productoEditText.setText(ticket.diligencias.firstOrNull()?.producto)
        autorizacionClienteEditText.setText(ticket.diligencias.firstOrNull()?.autorizacion_cliente)
        recibiClienteEditText.setText(ticket.cliente?.nombre)

        // Configura campos como no editables
        listOf(
            serviceControlEditText,
            razonSocialEditText,
            direccionEditText,
            nitEditText,
            telefonoEditText,
            celularEditText,
            tipoDeServiciosEditText,
            serviceRadioGroup
        ).forEach {
            it.isFocusable = false
            it.isFocusableInTouchMode = false
            it.isEnabled = false
        }

        // Asegúrate de que diligencias no está vacío
        val diligencia = ticket.diligencias.firstOrNull()

        // Establece el valor para el campo de fecha realizar (fecha y hora)
        fecharealizarEditText.setText(
            diligencia?.fechaRealizar?.let { dateTimeFormat.format(it) } ?: "Fecha no disponible"
        )

        // Establece el valor para el campo de fecha próximo (fecha y hora)
        fechaproximoEditText.setText(
            diligencia?.fechaProximo?.let { dateTimeFormat.format(it) } ?: "Fecha no disponible"
        )

        // Establece el valor para el campo de fecha de vencimiento (solo fecha)
        fechavencimientoEditText.setText(
            diligencia?.fechaVencimiento?.let { dateOnlyFormat.format(it) } ?: "Fecha no disponible"
        )

        // Establece el valor para el campo de nombre técnico
        nombre_tecnico.setText(diligencia?.nombreTecnico ?: "Nombre no disponible")

        // Configura el RadioGroup basado en el tipo de servicio
        val tipoServicio = diligencia?.tipoServicio
        when (tipoServicio) {
            "servicio" -> serviceRadioGroup.check(R.id.service)
            "refuerzo" -> serviceRadioGroup.check(R.id.reinforcement)
            else -> serviceRadioGroup.clearCheck()
        }

        // Configura el RadioGroup para el tipo de pago
        val tipoPago = diligencia?.tipoPago
        when (tipoPago) {
            "credito" -> tipoPagoRadioGroup.check(R.id.valorcredito)
            "contado" -> tipoPagoRadioGroup.check(R.id.valorcontado)
            else -> tipoPagoRadioGroup.clearCheck()
        }
    }

    fun showPhotoDialog(activity: Activity, imageView: ImageView?, callback: (String) -> Unit) {
        this.serviceImageView = imageView
        this.serviceImageCallback = callback
        val options = arrayOf("Seleccionar desde Explorador de Archivos", "Cancelar")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Seleccione una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    openFileExplorer(activity)
                }
                1 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    fun showAdditionalPhotoDialog(activity: Activity, imageView: ImageView?, callback: (String) -> Unit) {
        this.additionalImageView = imageView
        this.additionalImageCallback = callback
        val options = arrayOf("Seleccionar desde Explorador de Archivos", "Cancelar")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Seleccione una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    openFileExplorer(activity)
                }
                1 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openFileExplorer(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activity.startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), REQUEST_IMAGE_SELECT)
    }

    fun handleImageResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_SELECT -> {
                    val imageUri: Uri? = data?.data
                    Log.d("FormUtils", "Selected image URI: $imageUri")
                    imageUri?.let {
                        val imageBitmap = decodeSampledBitmapFromUri(it, 800, 800, activity)
                        if (imageBitmap != null) {
                            Log.d("FormUtils", "Loaded image bitmap from URI")
                            val base64Image = bitmapToBase64(imageBitmap)

                            // Guardar siempre en "foto_novedad"
                            saveBase64ToPreferences(activity, base64Image, "foto_novedad")

                            // Handle the service image
                            if (serviceImageView != null) {
                                serviceImageView?.setImageBitmap(imageBitmap)
                                serviceImageCallback?.invoke(base64Image)
                                serviceImageView = null // Reset variable after use
                                serviceImageCallback = null // Reset callback after use
                            }
                            // Handle the additional image
                            else if (additionalImageView != null) {
                                additionalImageView?.setImageBitmap(imageBitmap)
                                additionalImageCallback?.invoke(base64Image)
                                additionalImageView = null // Reset variable after use
                                additionalImageCallback = null // Reset callback after use
                            } else {
                                Log.e("FormUtils", "No imageView found for the result")
                            }
                        } else {
                            Log.e("FormUtils", "Failed to decode bitmap from URI")
                        }
                    } ?: Log.e("FormUtils", "No URI received from file explorer")
                }
            }
        } else {
            Log.e("FormUtils", "Result code was not OK: $resultCode")
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int, context: Context): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun saveBase64ToPreferences(context: Context, base64Image: String, key: String) {
        val sharedPreferences = context.getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, base64Image).apply()
    }

    fun getBase64Image(context: Context, key: String): String? {
        val sharedPreferences = context.getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }
}
