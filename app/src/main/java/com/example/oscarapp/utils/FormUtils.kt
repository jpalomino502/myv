package com.example.oscarapp.utils

import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        nombreTecnico: EditText,
        autorizacionClienteEditText: EditText,
        recibiClienteEditText: EditText,
        serviceRadioGroup: RadioGroup,
        ticket: Ticket
    ) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        fechaEditText.setText(currentDate)

        serviceControlEditText.setText(ticket.numTicket)
        razonSocialEditText.setText(ticket.cliente?.razonSocial)
        direccionEditText.setText(ticket.ubicacionActividad)
        nitEditText.setText(ticket.cliente?.nitCc)
        telefonoEditText.setText(ticket.cliente?.telefono)
        celularEditText.setText(ticket.cliente?.celular)
        tipoDeServiciosEditText.setText(ticket.titulo)
        productoEditText.setText(ticket.producto)
        autorizacionClienteEditText.setText(ticket.cliente?.nombre)
        recibiClienteEditText.setText(ticket.cliente?.nombre)

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")
        nombreTecnico.setText(userName)

        when (ticket.tipoServicio) {  // Cambiado a tipoServicio
            "servicio" -> serviceRadioGroup.check(R.id.service)
            "refuerzo" -> serviceRadioGroup.check(R.id.reinforcement)
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
                        val inputStream = activity.contentResolver.openInputStream(it)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        if (imageBitmap != null) {
                            Log.d("FormUtils", "Loaded image bitmap from URI")
                            val base64Image = bitmapToBase64(imageBitmap)

                            // Handle the service image
                            if (serviceImageView != null) {
                                serviceImageView?.setImageBitmap(imageBitmap)
                                saveBase64ToPreferences(activity, base64Image, "base64_image")
                                serviceImageCallback?.invoke(base64Image)
                                serviceImageView = null // Reset variable after use
                                serviceImageCallback = null // Reset callback after use
                            }
                            // Handle the additional image
                            else if (additionalImageView != null) {
                                additionalImageView?.setImageBitmap(imageBitmap)
                                saveBase64ToPreferences(activity, base64Image, "foto_novedad") // Save with key "foto_novedad"
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
