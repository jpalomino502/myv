package com.example.oscarapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oscarapp.models.Ticket
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object FormUtils {
    const val REQUEST_IMAGE_CAPTURE = 1
    const val REQUEST_IMAGE_SELECT = 2
    const val PERMISSION_REQUEST_CODE = 100
    var currentPermissionRequest: Int? = null
    private var imageView: ImageView? = null
    private var imageCallback: ((String) -> Unit)? = null

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
        ticket: Ticket
    ) {
        Log.d("FormUtils", "Autofilling form with ticket data")
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        fechaEditText.setText(currentDate)

        serviceControlEditText.setText(ticket.numTicket)
        razonSocialEditText.setText(ticket.cliente?.razonSocial)
        direccionEditText.setText(ticket.cliente?.direccion)
        nitEditText.setText(ticket.cliente?.nitCc)
        telefonoEditText.setText(ticket.cliente?.telefono)
        celularEditText.setText(ticket.cliente?.telefono)
        tipoDeServiciosEditText.setText(ticket.titulo)
        productoEditText.setText(ticket.producto)
        autorizacionClienteEditText.setText(ticket.cliente?.nombre)
        recibiClienteEditText.setText(ticket.cliente?.nombre)

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")
        nombreTecnico.setText(userName)
        Log.d("FormUtils", "Form autofilled successfully")
    }

    fun showPhotoDialog(activity: Activity, imageView: ImageView?, callback: (String) -> Unit) {
        this.imageView = imageView
        this.imageCallback = callback
        val options = arrayOf("Tomar Foto", "Seleccionar desde Galería", "Cancelar")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Seleccione una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    currentPermissionRequest = REQUEST_IMAGE_CAPTURE
                    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
                    } else {
                        takePhoto(activity)
                    }
                }
                1 -> {
                    currentPermissionRequest = REQUEST_IMAGE_SELECT
                    if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
                    } else {
                        selectPhoto(activity)
                    }
                }
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    fun takePhoto(activity: Activity) {
        Log.d("FormUtils", "Taking photo")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            Log.e("FormUtils", "No activity found to handle take photo intent")
        }
    }

    fun selectPhoto(activity: Activity) {
        Log.d("FormUtils", "Selecting photo from gallery")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_SELECT)
        } else {
            Log.e("FormUtils", "No activity found to handle select photo intent")
        }
    }

    fun handleImageResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    Log.d("FormUtils", "Handling image capture result")
                    val extras = data?.extras
                    val imageBitmap = extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        imageView?.setImageBitmap(it)
                        val base64Image = bitmapToBase64(it)
                        saveBase64ToPreferences(activity, base64Image)
                        imageCallback?.invoke(base64Image)
                    } ?: run {
                        Log.e("FormUtils", "Failed to capture image")
                    }
                }
                REQUEST_IMAGE_SELECT -> {
                    Log.d("FormUtils", "Handling image selection result")
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        val inputStream = activity.contentResolver.openInputStream(it)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        imageView?.setImageBitmap(imageBitmap)
                        val base64Image = bitmapToBase64(imageBitmap)
                        saveBase64ToPreferences(activity, base64Image)
                        imageCallback?.invoke(base64Image)
                    } ?: run {
                        Log.e("FormUtils", "Failed to select image")
                    }
                }
            }
        } else {
            Log.e("FormUtils", "Image result not OK, resultCode: $resultCode")
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        Log.d("FormUtils", "Converting bitmap to base64")
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun saveBase64ToPreferences(context: Context, base64Image: String) {
        Log.d("FormUtils", "Saving base64 image to preferences")
        val sharedPreferences = context.getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("base64_image", base64Image).apply()
        Log.d("FormUtils", "Base64 image saved to preferences")
    }
}
