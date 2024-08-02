package com.example.oscarapp.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.core.view.children
import com.example.oscarapp.R
import org.json.JSONArray
import org.json.JSONException

object FormDataPopulator {

    var modifiedServiciosArray: JSONArray? = null

    fun populateServicios(activity: Activity, serviciosContainer: LinearLayout, serviciosJson: String): String {
        try {
            val serviciosArray = JSONArray(serviciosJson)
            modifiedServiciosArray = JSONArray()
            serviciosContainer.removeAllViews()

            for (i in 0 until serviciosArray.length()) {
                val servicioObj = serviciosArray.getJSONObject(i)
                val servicioName = servicioObj.optString("servicio", "Servicio Sin Nombre")

                val servicioTitle = TextView(activity).apply {
                    text = servicioName
                    textSize = 20f
                    setPadding(16, 16, 16, 8)
                    setTextColor(Color.BLACK)
                }
                serviciosContainer.addView(servicioTitle)

                // Especial para el Servicio 12 y 13
                val itemsArrayKey = if (servicioName == "Servicio 12" || servicioName == "Servicio 13") "servicios" else "plagas"
                val itemsArray = servicioObj.optJSONArray(itemsArrayKey) ?: JSONArray()
                val sectionTitle = if (servicioName == "Servicio 12" || servicioName == "Servicio 13") "Servicios" else "Plagas"
                addSectionItems(activity, serviciosContainer, itemsArray, sectionTitle, servicioName, modifiedServiciosArray!!)

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
                            setTextColor(Color.BLACK)
                            buttonTintList = ColorStateList.valueOf(Color.BLACK)
                            setPadding(16, 16, 16, 16)
                            setOnCheckedChangeListener { _, isChecked ->
                                Log.d("FormDataPopulator", "Método $metodoName changed to: $isChecked")
                                metodoObj.put("checked", isChecked)
                                printNewJson(modifiedServiciosArray!!)
                            }
                        }
                        metodosLayout.addView(checkBox)
                    }
                    serviciosContainer.addView(metodosLayout)
                }

                val gradosArray = servicioObj.optJSONArray("grados") ?: JSONArray()
                if (gradosArray.length() > 0) {
                    val gradosLayout = LinearLayout(activity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 0, 0, 16)
                    }
                    val gradosTitle = TextView(activity).apply {
                        text = "Grados"
                        textSize = 18f
                        setPadding(16, 16, 16, 8)
                        setTextColor(Color.BLACK)
                    }
                    gradosLayout.addView(gradosTitle)

                    val radioGroup = RadioGroup(activity).apply {
                        orientation = RadioGroup.HORIZONTAL
                        setOnCheckedChangeListener { group, checkedId ->
                            for (j in 0 until gradosArray.length()) {
                                val gradoObj = gradosArray.getJSONObject(j)
                                val radioButton = group.findViewById<RadioButton>(checkedId)
                                if (radioButton != null) {
                                    val gradoName = radioButton.text.toString()
                                    gradoObj.put("checked", gradoObj.optString("name") == gradoName)
                                }
                            }
                            printNewJson(modifiedServiciosArray!!)
                        }
                    }


                    for (j in 0 until gradosArray.length()) {
                        val gradoObj = gradosArray.getJSONObject(j)
                        val gradoName = gradoObj.optString("name", "Grado Sin Nombre")
                        val gradoChecked = gradoObj.optBoolean("checked", false)

                        val radioButton = RadioButton(activity).apply {
                            text = gradoName
                            isChecked = gradoChecked
                            setTextColor(Color.BLACK)
                            buttonTintList = ColorStateList.valueOf(Color.BLACK)
                        }
                        radioGroup.addView(radioButton)
                    }
                    gradosLayout.addView(radioGroup)
                    serviciosContainer.addView(gradosLayout)
                }
                modifiedServiciosArray!!.put(servicioObj)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            return serviciosJson
        }
        printNewJson(modifiedServiciosArray!!)
        return modifiedServiciosArray.toString()
    }

    private fun addSectionItems(
        activity: Activity,
        layout: LinearLayout,
        items: JSONArray,
        sectionName: String,
        servicioName: String,
        modifiedServiciosArray: JSONArray
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
                    setText(itemValue)
                    addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            item.put("value", s.toString())
                            printNewJson(modifiedServiciosArray)
                        }
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    })
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
                        text = "Seleccionar Foto"
                        setOnClickListener {
                            FormUtils.showPhotoDialog(activity, imageView) { base64Image ->
                                // Aquí puedes manejar la imagen seleccionada si es necesario
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
                    setOnCheckedChangeListener { _, isChecked ->
                        Log.d("FormDataPopulator", "Checkbox $itemName changed to: $isChecked")
                        try {
                            item.put("checked", isChecked)
                            printNewJson(modifiedServiciosArray)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                itemLayout.addView(checkBox)
            }

            sectionLayout.addView(itemLayout)
        }
        layout.addView(sectionLayout)
    }

    private fun printNewJson(modifiedServiciosArray: JSONArray) {
        Log.d("FormDataPopulator", "Modified JSON: ${modifiedServiciosArray.toString()}")
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