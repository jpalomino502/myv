package com.example.oscarapp.utils

import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.text.Normalizer

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

    fun obtenerEquiposSeleccionados(container: LinearLayout): String {
        val equiposArray = JsonArray<JsonObject>()
        for (i in 0 until container.childCount) {
            val itemView = container.getChildAt(i) as? LinearLayout ?: continue
            val checkBox = itemView.getChildAt(0) as? CheckBox ?: continue
            val itemJson = JsonObject().apply {
                put("name", "")
                put("checked", checkBox.isChecked)
                put("value", checkBox.text.toString())
            }
            equiposArray.add(itemJson)
        }
        return klaxon.toJsonString(equiposArray)
    }

    fun obtenerServiciosSeleccionados(container: LinearLayout): String {
        val serviciosArray = JsonArray<JsonObject>()
        for (i in 0 until container.childCount) {
            val servicioView = container.getChildAt(i) as? LinearLayout ?: continue
            val servicioName = (servicioView.getChildAt(0) as? TextView)?.text?.toString()?.let { formatName(it) } ?: "Sin-Nombre"
            val grados = obtenerGradosSeleccionados(servicioView)
            val metodos = obtenerSeccionItems(servicioView, "Métodos")
            val plagas = obtenerSeccionItems(servicioView, "Plagas")

            val servicioJson = JsonObject().apply {
                put("servicio", servicioName)
                put("grados", grados)
                put("metodos", metodos)

                // Cambiar 'plagas' por 'servicios' si el nombre del servicio es "12"
                if (servicioName.contains("12")) {
                    put("servicios", plagas)
                } else if (servicioName.contains("13")) {
                    put("servicios", plagas)
                } else {
                    put("plagas", plagas)
                }
            }
            serviciosArray.add(servicioJson)
        }
        return klaxon.toJsonString(serviciosArray)
    }

    private fun formatName(name: String): String {
        return name.split(' ').joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.toLowerCase().capitalize()
            } else {
                word
            }
        }.let {
            it.replaceFirstChar { char -> char.uppercase() }
        }
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace("ñ", "n")
    }

    private fun formatValue(value: String): String {
        // Elimina el texto dentro de paréntesis y los paréntesis mismos
        val cleanedValue = value.replace(Regex("\\s*\\([^)]*\\)\\s*"), "")

        val normalizedValue = normalizeText(cleanedValue.toLowerCase())
        val withHyphens = normalizedValue.replace("y", "-")
        return withHyphens
            .replace(Regex("[\\s]+"), "-")
            .replace(Regex("[-]+"), "-")
            .trim('-')
    }

    private fun obtenerSeccionItems(servicioView: LinearLayout, sectionName: String): JsonArray<JsonObject> {
        val itemsArray = JsonArray<JsonObject>()
        for (i in 1 until servicioView.childCount) {
            val sectionLayout = servicioView.getChildAt(i) as? LinearLayout ?: continue
            val sectionHeader = sectionLayout.getChildAt(0) as? TextView ?: continue
            if (sectionHeader.text == sectionName) {
                for (j in 1 until sectionLayout.childCount) {
                    val itemLayout = sectionLayout.getChildAt(j) as? LinearLayout ?: continue
                    val itemJson = JsonObject()
                    val checkBox = itemLayout.getChildAt(0) as? CheckBox
                    val editText = itemLayout.getChildAt(0) as? EditText
                    val button = itemLayout.getChildAt(0) as? Button
                    when {
                        checkBox != null -> {
                            val value = formatValue(checkBox.text.toString().trim())
                            itemJson.apply {
                                put("name", checkBox.text.toString().trim().let { formatName(it) })
                                put("checked", checkBox.isChecked)
                                put("value", value)
                            }
                            itemsArray.add(itemJson)
                        }

                        editText != null -> {
                            itemJson.apply {
                                put("name", "Cantidad")
                                put("value", editText.text.toString())
                                put("checked", false)
                            }
                            itemsArray.add(itemJson)
                        }

                        button != null && !(formatName((servicioView.getChildAt(0) as? TextView)?.text?.toString() ?: "") == "Servicio 10") -> {
                            // Excluye el botón para el servicio 10
                            itemJson.apply {
                                put("name", "Foto")
                                put("value", button.text == "Foto Seleccionada")
                            }
                            itemsArray.add(itemJson)
                        }
                    }
                }
            }
        }
        return itemsArray
    }

    private fun obtenerGradosSeleccionados(servicioView: LinearLayout): JsonArray<JsonObject> {
        val gradosArray = JsonArray<JsonObject>()
        for (i in 1 until servicioView.childCount) {
            val sectionLayout = servicioView.getChildAt(i) as? LinearLayout ?: continue
            val sectionHeader = sectionLayout.getChildAt(0) as? TextView ?: continue
            if (sectionHeader.text == "Grados") {
                val radioGroup = sectionLayout.getChildAt(1) as? RadioGroup ?: continue
                for (j in 0 until radioGroup.childCount) {
                    val radioButton = radioGroup.getChildAt(j) as? RadioButton ?: continue
                    val value = formatValue(radioButton.text.toString().trim())
                    val itemJson = JsonObject().apply {
                        put("name", radioButton.text.toString().trim().let { formatName(it) })
                        put("checked", radioButton.isChecked)
                        put("value", value)
                    }
                    gradosArray.add(itemJson)
                }
            }
        }
        return gradosArray
    }
}
