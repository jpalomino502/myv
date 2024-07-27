package com.example.oscarapp.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.util.*

object DateTimeUtils {

    fun setupDatePicker(editText: EditText, context: Context) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isClickable = true
        editText.setOnClickListener {
            hideKeyboard(editText, context)
            showDatePickerDialog(editText, context, showTimePicker = false)
        }
    }

    fun setupTimePicker(editText: EditText, context: Context) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isClickable = true
        editText.setOnClickListener {
            hideKeyboard(editText, context)
            showTimePickerDialog(editText, context)
        }
    }

    fun setupDateTimePicker(editText: EditText, context: Context) {
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isClickable = true
        editText.setOnClickListener {
            hideKeyboard(editText, context)
            showDatePickerDialog(editText, context, showTimePicker = true)
        }
    }

    private fun hideKeyboard(editText: EditText, context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    private fun showDatePickerDialog(editText: EditText, context: Context, showTimePicker: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
                if (showTimePicker) {
                    showTimePickerDialog(editText, context)
                }
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText, context: Context) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context, { _, selectedHour, selectedMinute ->
                val selectedTime = String.format(" %02d:%02d", selectedHour, selectedMinute)
                val currentText = editText.text.toString()
                editText.setText(currentText + selectedTime)
            }, hour, minute, true
        )

        timePickerDialog.show()
    }
}
