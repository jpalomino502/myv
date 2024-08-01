package com.example.oscarapp.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.core.view.children
import com.example.oscarapp.R
import org.json.JSONArray
import org.json.JSONException

object FormDataPopulator {

    fun populateServicios(activity: Activity, serviciosContainer: LinearLayout, serviciosJson: String) {
        try {
            val serviciosArray = JSONArray(serviciosJson)
            serviciosContainer.removeAllViews()

            for (i in 0 until serviciosArray.length()) {
                val servicioObj = serviciosArray.getJSONObject(i)
                val servicioName = servicioObj.optString("servicio", "Servicio Sin Nombre")

                // Añadir el nombre del servicio como un título
                val servicioTitle = TextView(activity).apply {
                    text = servicioName
                    textSize = 20f
                    setPadding(16, 16, 16, 8)
                    setTextColor(Color.BLACK)
                }
                serviciosContainer.addView(servicioTitle)

                // Añadir los elementos de la sección
                val itemsArray = servicioObj.optJSONArray("plagas") ?: JSONArray()
                addSectionItems(activity, serviciosContainer, itemsArray, "Plagas", servicioName)

                // Añadir métodos
                val metodosArray = servicioObj.optJSONArray("metodos") ?: JSONArray()
                if (metodosArray.length() > 0) {
                    val metodosLayout = LinearLayout(activity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 0, 0, 16)
                    }
                    val metodosTitle = TextView(activity).apply {
                        text = "Métodos"
                        textSize = 18f
                        setPadding(16, 16, 16, 8)
                        setTextColor(Color.BLACK)
                    }
                    metodosLayout.addView(metodosTitle)

                    for (k in 0 until metodosArray.length()) {
                        val metodoObj = metodosArray.getJSONObject(k)
                        val metodoName = metodoObj.optString("name", "Método Sin Nombre")
                        val metodoChecked = metodoObj.optBoolean("checked", false)

                        val checkBox = CheckBox(activity).apply {
                            text = metodoName
                            isChecked = metodoChecked
                            isEnabled = false
                            setTextColor(Color.BLACK)
                            buttonTintList = ColorStateList.valueOf(Color.BLACK)
                            setPadding(16, 16, 16, 16)
                        }
                        metodosLayout.addView(checkBox)
                    }
                    serviciosContainer.addView(metodosLayout)
                }

                // Añadir grados
                val gradosArray = servicioObj.optJSONArray("grados") ?: JSONArray()
                if (gradosArray.length() > 0) {
                    val gradosLayout = LinearLayout(activity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 0, 0, 16)
                    }
                    val gradosTitle = TextView(activity).apply {
                        text = "Grados"
                        textSize = 18f
                        setPadding(16, 16, 16, 8)
                        setTextColor(Color.BLACK)
                    }
                    gradosLayout.addView(gradosTitle)

                    for (j in 0 until gradosArray.length()) {
                        val gradoObj = gradosArray.getJSONObject(j)
                        val gradoName = gradoObj.optString("name", "Grado Sin Nombre")
                        val gradoChecked = gradoObj.optBoolean("checked", false)

                        val radioButton = RadioButton(activity).apply {
                            text = gradoName
                            isChecked = gradoChecked
                            isEnabled = false
                            setTextColor(Color.BLACK)
                            buttonTintList = ColorStateList.valueOf(Color.BLACK)
                        }
                        gradosLayout.addView(radioButton)
                    }
                    serviciosContainer.addView(gradosLayout)
                }
            }
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

                // Añadir ImageView para mostrar la foto
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
                        text = "Seleccionar Foto"
                        setOnClickListener {
                            FormUtils.showPhotoDialog(activity, imageView) { base64Image ->
                                // Aquí puedes manejar la imagen capturada si es necesario
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


    fun populateEquipos(activity: Activity, equiposContainer: LinearLayout, equiposJson: String) {
        try {
            val equiposArray = JSONArray(equiposJson)
            equiposContainer.removeAllViews()
            val title = TextView(activity).apply {
                text = "Equipos de Protección"
                textSize = 18f
                setPadding(16, 16, 16, 8)
                setTextColor(Color.BLACK)
            }
            equiposContainer.addView(title)
            for (i in 0 until equiposArray.length()) {
                val item = equiposArray.getJSONObject(i)
                val value = item.optString("value", "Sin nombre")
                val checked = item.optBoolean("checked", false)

                val checkBox = CheckBox(activity).apply {
                    text = value
                    isChecked = checked
                    isEnabled = false
                    setTextColor(Color.BLACK)
                    buttonTintList = ColorStateList.valueOf(Color.BLACK)
                    setPadding(16, 16, 16, 16)
                }
                equiposContainer.addView(checkBox)
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

            // Añadir Button e ImageView a observacionesContainer
            activity.runOnUiThread {
                addButtonAndImageView(activity)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addButtonAndImageView(activity: Activity) {
        val observacionesContainer = activity.findViewById<LinearLayout>(R.id.observacionesContainer)

        if (observacionesContainer != null) {
            val hasImageView = observacionesContainer.children.any { it is ImageView }
            val hasButton = observacionesContainer.children.any { it is Button }

            if (!hasImageView && !hasButton) {
                val imageView = ImageView(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        400
                    ).apply {
                        setMargins(16, 16, 16, 8)
                    }
                    setPadding(16, 16, 16, 16)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }

                val button = Button(activity).apply {
                    text = "Seleccionar Foto"
                    setOnClickListener {
                        FormUtils.showAdditionalPhotoDialog(activity, imageView) { base64Image ->
                        }
                    }
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(Color.BLACK)
                    setTextColor(Color.WHITE)
                }

                observacionesContainer.addView(imageView)
                observacionesContainer.addView(button)
            }
        } else {
            Log.e("FormDataPopulator", "No se encontró el contenedor de observaciones")
        }
    }
}
