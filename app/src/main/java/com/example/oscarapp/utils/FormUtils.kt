package com.example.oscarapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.EditText
import com.example.oscarapp.models.Ticket
import java.text.SimpleDateFormat
import java.util.*

object FormUtils {

    private const val REQUEST_IMAGE_CAPTURE = 1
    private const val REQUEST_IMAGE_SELECT = 2

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
    }

    fun showPhotoDialog(context: Context) {
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

    fun takePhoto(context: Context) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? Activity)?.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    fun selectPhoto(context: Context) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(context.packageManager) != null) {
            (context as? Activity)?.startActivityForResult(intent, REQUEST_IMAGE_SELECT)
        }
    }
}