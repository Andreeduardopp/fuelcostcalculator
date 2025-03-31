package com.example.fuelcostcalculator

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Currency

class DecimalInputTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace(Regex("[^\\d]"), "")

            if (cleanString.isNotEmpty()) {
                val parsed = cleanString.toDouble() / 100
                val formatted = NumberFormat.getCurrencyInstance().apply {
                    maximumFractionDigits = 2
                    currency = Currency.getInstance("BRL")
                }.format(parsed)

                val finalFormatted = formatted.replace(Regex("[R$\\s,]"), "")

                current = finalFormatted
                editText.setText(finalFormatted)
                editText.setSelection(finalFormatted.length)
            } else {
                current = ""
                editText.setText("")
            }

            editText.addTextChangedListener(this)
        }
    }
}