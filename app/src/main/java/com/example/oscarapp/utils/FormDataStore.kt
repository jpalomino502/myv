package com.example.oscarapp.utils

import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon

object FormDataStorage {

    private val klaxon = Klaxon()

    fun obtenerEncuestaSeleccionada(container: LinearLayout): String {
        val encuestaArray = JsonArray<JsonObject>()
        for (i in 0 until container.childCount) {
            val itemView = container.getChildAt(i) as? LinearLayout ?: continue
            val radioGroup = itemView.getChildAt(1) as? RadioGroup ?: continue
            val itemJson = JsonObject().apply {
                put("label", (itemView.getChildAt(0) as? TextView)?.text ?: "Sin etiqueta")
                val options = JsonArray<JsonObject>()
                for (j in 0 until radioGroup.childCount) {
                    val radioButton = radioGroup.getChildAt(j) as? RadioButton ?: continue
                    options.add(JsonObject().apply {
                        put("name", "")
                        put("checked", radioButton.isChecked)
                        put("value", radioButton.text.toString())
                        put("label", radioButton.text.toString())
                    })
                }
                put("options", options)
            }
            encuestaArray.add(itemJson)
        }
        return klaxon.toJsonString(encuestaArray)
    }
}
