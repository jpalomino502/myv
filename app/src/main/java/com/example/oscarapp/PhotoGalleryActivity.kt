package com.example.oscarapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.oscarapp.adapters.PhotoAdapter

class PhotoGalleryActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private val REQUEST_PERMISSION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        gridView = findViewById(R.id.gridView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PhotoGalleryActivity", "Requesting READ_EXTERNAL_STORAGE permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        } else {
            Log.d("PhotoGalleryActivity", "READ_EXTERNAL_STORAGE permission already granted")
            loadImages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("PhotoGalleryActivity", "READ_EXTERNAL_STORAGE permission granted")
                loadImages()
            } else {
                Log.d("PhotoGalleryActivity", "READ_EXTERNAL_STORAGE permission denied")
            }
        }
    }

    private fun loadImages() {
        Log.d("PhotoGalleryActivity", "Loading images")
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        if (cursor == null) {
            Log.d("PhotoGalleryActivity", "Cursor is null")
            return
        }

        Log.d("PhotoGalleryActivity", "Cursor count: ${cursor.count}")
        val imageUris = mutableListOf<Uri>()
        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val imageUri = Uri.withAppendedPath(uri, id.toString())
                Log.d("PhotoGalleryActivity", "Found image URI: $imageUri")
                imageUris.add(imageUri)
            }
        }

        Log.d("PhotoGalleryActivity", "Total images found: ${imageUris.size}")
        val adapter = PhotoAdapter(this, imageUris)
        gridView.adapter = adapter
    }

}
