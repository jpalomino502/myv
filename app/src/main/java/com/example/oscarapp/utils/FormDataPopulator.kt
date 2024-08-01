package com.example.oscarapp.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.widget.*
import androidx.core.view.children
import com.example.oscarapp.R
import org.json.JSONArray
import org.json.JSONException

object FormDataPopulator {

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
