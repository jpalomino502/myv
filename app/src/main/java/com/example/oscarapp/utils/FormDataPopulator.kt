package com.example.oscarapp.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.widget.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object FormDataPopulator {

    fun populateServicios(activity: Activity, container: LinearLayout) {
        try {
            val serviciosArray = JSONArray(JsonData.JSON_DATA)
            Log.d("FormDataPopulator", "Servicios JSON: $serviciosArray")

            for (i in 0 until serviciosArray.length()) {
                val servicio = serviciosArray.getJSONObject(i)
                Log.d("FormDataPopulator", "Procesando servicio: ${servicio.optString("servicio")}")
                addServicioSection(activity, container, servicio)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun populateEncuesta(activity: Activity, container: LinearLayout) {
        try {
            val encuestaArray = JSONArray(JsonData.INFORME_SERVICIOS_JSON)

            for (i in 0 until encuestaArray.length()) {
                val item = encuestaArray.getJSONObject(i)
                val label = item.optString("label", "Sin etiqueta")
                val options = item.optJSONArray("options") ?: JSONArray()

                val encuestaLayout = LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 0, 0, 16)
                    elevation = 2f
                }
                val questionLabel = TextView(activity).apply {
                    text = label
                    textSize = 18f
                    setTextColor(Color.BLACK)
                }
                encuestaLayout.addView(questionLabel)

                val radioGroup = RadioGroup(activity).apply {
                    orientation = RadioGroup.HORIZONTAL
                }

                for (j in 0 until options.length()) {
                    val option = options.getJSONObject(j)
                    val radioButton = RadioButton(activity).apply {
                        text = option.optString("label", "Opción")
                        isChecked = option.optBoolean("checked", false)
                        setTextColor(Color.BLACK)
                        buttonTintList = ColorStateList.valueOf(Color.BLACK)
                    }
                    radioGroup.addView(radioButton)
                }

                encuestaLayout.addView(radioGroup)
                container.addView(encuestaLayout)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun populateEquipoProteccion(activity: Activity, container: LinearLayout) {
        try {
            val equipoArray = JSONArray(JsonData.EQUIPO_JSON)

            val equipoHeader = TextView(activity).apply {
                text = "Equipos de Protección"
                textSize = 18f
                setTextColor(Color.BLACK)
                setPadding(0, 16, 0, 16)
            }
            container.addView(equipoHeader)

            for (i in 0 until equipoArray.length()) {
                val item = equipoArray.getJSONObject(i)
                val name = item.optString("name", "Sin nombre")
                val checked = item.optBoolean("checked", false)

                val equipoLayout = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 0, 0, 16)
                }

                val checkBox = CheckBox(activity).apply {
                    text = name
                    isChecked = checked
                    setTextColor(Color.BLACK)
                    buttonTintList = ColorStateList.valueOf(Color.BLACK)
                }

                equipoLayout.addView(checkBox)
                container.addView(equipoLayout)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addServicioSection(activity: Activity, container: LinearLayout, servicio: JSONObject) {
        try {
            val servicioName = servicio.optString("servicio", "Sin nombre")
            Log.d("FormDataPopulator", "Añadiendo sección para servicio: $servicioName")

            val plagasArray = servicio.optJSONArray("plagas") ?: JSONArray()
            val metodosArray = servicio.optJSONArray("metodos") ?: JSONArray()
            val gradosArray = servicio.optJSONArray("grados") ?: JSONArray()

            val servicioLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, 16)
                elevation = 2f
            }

            val servicioHeader = TextView(activity).apply {
                text = servicioName
                textSize = 20f
                setTextColor(Color.BLACK)
            }
            servicioLayout.addView(servicioHeader)

            if (plagasArray.length() > 0) {
                addSectionItems(activity, servicioLayout, plagasArray, "Plagas", servicioName)
            }
            if (metodosArray.length() > 0) {
                addSectionItems(activity, servicioLayout, metodosArray, "Métodos", servicioName)
            }
            if (gradosArray.length() > 0) {
                addGradosSection(activity, servicioLayout, gradosArray)
            }

            container.addView(servicioLayout)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addSectionItems(
        activity: Activity,
        layout: LinearLayout,
        items: JSONArray,
        sectionName: String,
        servicioName: String
    ) {
        val sectionLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }
        val sectionHeader = TextView(activity).apply {
            text = sectionName
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        sectionLayout.addView(sectionHeader)

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val itemName = item.optString("name", "Sin nombre")
            val itemChecked = item.optBoolean("checked", false)
            val itemValue = item.optString("value", "")

            val itemLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, 8)
            }

            if (itemName == "Cantidad") {
                val cantidadEditText = EditText(activity).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    hint = "Cantidad"
                    setPadding(16, 16, 16, 16)
                    setTextColor(Color.BLACK)
                    setHintTextColor(Color.BLACK)
                    setText("")
                }
                itemLayout.addView(cantidadEditText)

                val imageView = ImageView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        400
                    )
                    setPadding(16, 16, 16, 16)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                sectionLayout.addView(imageView)

                if (servicioName == "Servicio 10") {
                    val fotoButton = Button(activity).apply {
                        text = "Tomar o Seleccionar Foto"
                        setOnClickListener {
                            FormUtils.showPhotoDialog(activity, imageView) { base64Image ->
                                // Aquí puedes manejar la imagen en formato base64
                            }
                        }
                        setPadding(16, 16, 16, 16)
                        setBackgroundColor(Color.BLACK)
                        setTextColor(Color.WHITE)
                    }
                    itemLayout.addView(fotoButton)
                }
            } else if (itemName != "Foto") {
                val checkBox = CheckBox(activity).apply {
                    text = itemName
                    isChecked = itemChecked
                    setTextColor(Color.BLACK)
                    buttonTintList = ColorStateList.valueOf(Color.BLACK)
                }
                itemLayout.addView(checkBox)
            }
            sectionLayout.addView(itemLayout)
        }
        layout.addView(sectionLayout)
    }

    private fun addGradosSection(activity: Activity, layout: LinearLayout, gradosArray: JSONArray) {
        val gradosLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 16)
        }
        val gradosHeader = TextView(activity).apply {
            text = "Grados"
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        gradosLayout.addView(gradosHeader)

        val radioGroup = RadioGroup(activity).apply {
            orientation = RadioGroup.HORIZONTAL
        }

        for (i in 0 until gradosArray.length()) {
            val item = gradosArray.getJSONObject(i)
            val itemName = item.optString("name", "Sin nombre")
            val itemChecked = item.optBoolean("checked", false)

            val radioButton = RadioButton(activity).apply {
                text = itemName
                isChecked = itemChecked
                setTextColor(Color.BLACK)
                buttonTintList = ColorStateList.valueOf(Color.BLACK)
            }

            radioGroup.addView(radioButton)
        }

        gradosLayout.addView(radioGroup)
        layout.addView(gradosLayout)
    }
}
