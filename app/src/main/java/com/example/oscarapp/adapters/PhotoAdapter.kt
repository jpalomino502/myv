package com.example.oscarapp.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.oscarapp.R

class PhotoAdapter(private val context: Context, private val imageUris: List<Uri>) : BaseAdapter() {

    override fun getCount(): Int {
        Log.d("PhotoAdapter", "getCount: ${imageUris.size}")
        return imageUris.size
    }

    override fun getItem(position: Int): Any = imageUris[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        val imageUri = imageUris[position]
        Log.d("PhotoAdapter", "Loading image at position $position: $imageUri")
        imageView.setImageURI(imageUri)
        return view
    }
}
