package com.example.oscarapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.EditText
import android.widget.ImageView
import com.example.oscarapp.models.Ticket
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.io.InputStream

object FormUtils {
    private const val REQUEST_IMAGE_CAPTURE = 1
    private const val REQUEST_IMAGE_SELECT = 2
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
        nombre_tecnico: EditText,
        autorizacion_clienteEditText: EditText,
        recibi_clienteEditText: EditText,
        ticket: Ticket
    ) {
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
        autorizacion_clienteEditText.setText(ticket.cliente.nombre)
        recibi_clienteEditText.setText(ticket.cliente.nombre)

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("userName", "")
        nombre_tecnico.setText(userName)
    }

    fun showPhotoDialog(context: Context, imageView: ImageView?, callback: (String) -> Unit) {
        this.imageView = imageView
        this.imageCallback = callback
        val options = arrayOf("Tomar Foto", "Seleccionar desde Galería", "Cancelar")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccione una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePhoto(context)
                1 -> selectPhoto(context)
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun takePhoto(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? Activity)?.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun selectPhoto(context: Context) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? Activity)?.startActivityForResult(intent, REQUEST_IMAGE_SELECT)
        }
    }

    fun handleImageResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val extras = data?.extras
                    val imageBitmap = extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        imageView?.setImageBitmap(it)
                        val base64Image = bitmapToBase64(it)
                        saveBase64ToPreferences(activity, base64Image)
                        imageCallback?.invoke(base64Image)
                    }
                }
                REQUEST_IMAGE_SELECT -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        val inputStream = activity.contentResolver.openInputStream(it)
                        val imageBitmap = BitmapFactory.decodeStream(inputStream)
                        imageView?.setImageBitmap(imageBitmap)
                        val base64Image = bitmapToBase64(imageBitmap)
                        saveBase64ToPreferences(activity, base64Image)
                        imageCallback?.invoke(base64Image)
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return "data:image/svg+xml;base64,$base64String"
    }

    private fun saveBase64ToPreferences(context: Context, base64Image: String) {
        val sharedPreferences = context.getSharedPreferences("base64_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("base64_image", base64Image).apply()
    }
}
