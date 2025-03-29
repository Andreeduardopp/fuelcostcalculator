package com.example.fuelcostcalculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fuelcostcalculator.FuelSelectionActivity

class MainActivity : AppCompatActivity() {
    private lateinit var etGasolinePrice: EditText
    private lateinit var etEthanolPrice: EditText
    private lateinit var etGasolineConsumption: EditText
    private lateinit var etEthanolConsumption: EditText
    private lateinit var btnSelectFuel: Button
    private lateinit var btnCalculate: Button
    private lateinit var tvResult: TextView

    companion object {
        const val REQUEST_FUEL_TYPE = 1
        const val EXTRA_FUEL_TYPE = "fuel_type"
        const val FUEL_TYPE_GASOLINE = "gasoline"
        const val FUEL_TYPE_ETHANOL = "ethanol"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        etGasolinePrice = findViewById(R.id.etGasolinePrice)
        etEthanolPrice = findViewById(R.id.etEthanolPrice)
        etGasolineConsumption = findViewById(R.id.etGasolineConsumption)
        etEthanolConsumption = findViewById(R.id.etEthanolConsumption)
        btnSelectFuel = findViewById(R.id.btnSelectFuel)
        btnCalculate = findViewById(R.id.btnCalculate)
        tvResult = findViewById(R.id.tvResult)

        loadSavedConsumptionValues()
        btnSelectFuel.setOnClickListener {
            val intent = Intent(this, FuelSelectionActivity::class.java)
            selectFuelLauncher.launch(intent)
        }

        btnCalculate.setOnClickListener {
            calculateMostEconomicalFuel()
            saveConsumptionValues()
        }
}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FUEL_TYPE && resultCode == RESULT_OK) {
            val fuelType = data?.getStringExtra(EXTRA_FUEL_TYPE)
            if (fuelType == FUEL_TYPE_GASOLINE) {
                etGasolineConsumption.requestFocus()
            } else if (fuelType == FUEL_TYPE_ETHANOL) {
                etEthanolConsumption.requestFocus()
            }
        }
    }

    // Calculate which fuel is more economical
    private fun calculateMostEconomicalFuel() {
        // Get values from input fields
        val gasolinePrice = etGasolinePrice.text.toString().toDoubleOrNull() ?: 0.0
        val ethanolPrice = etEthanolPrice.text.toString().toDoubleOrNull() ?: 0.0
        val gasolineConsumption = etGasolineConsumption.text.toString().toDoubleOrNull() ?: 0.0
        val ethanolConsumption = etEthanolConsumption.text.toString().toDoubleOrNull() ?: 0.0

        // Validate inputs
        if (gasolinePrice <= 0 || ethanolPrice <= 0 || gasolineConsumption <= 0 || ethanolConsumption <= 0) {
            tvResult.text = getString(R.string.invalid_input)
            return
        }

        // Calculate cost per kilometer for each fuel type
        val gasolineCostPerKm = gasolinePrice / gasolineConsumption
        val ethanolCostPerKm = ethanolPrice / ethanolConsumption

        // Determine which fuel is more economical
        val result = if (ethanolCostPerKm < gasolineCostPerKm) {
            getString(R.string.ethanol_more_economical)
        } else {
            getString(R.string.gasoline_more_economical)
        }

        val ratio = (ethanolPrice / gasolinePrice) * 100
        val percentage = String.format("%.1f", ratio)

        // Show result with details
        val detailedResult = "$result\n\n" +
                getString(R.string.cost_per_km_gasoline, String.format("%.2f", gasolineCostPerKm)) + "\n" +
                getString(R.string.cost_per_km_ethanol, String.format("%.2f", ethanolCostPerKm)) + "\n\n" +
                getString(R.string.price_ratio, percentage)

        tvResult.text = detailedResult
    }

    private val selectFuelLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val fuelType = result.data?.getStringExtra(EXTRA_FUEL_TYPE)
            if (fuelType == FUEL_TYPE_GASOLINE) {
                etGasolineConsumption.requestFocus()

                val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
                val savedGasolineConsumption = sharedPreferences.getString("gasoline_consumption", "")
                if (!savedGasolineConsumption.isNullOrEmpty() && etGasolineConsumption.text.isEmpty()) {
                    etGasolineConsumption.setText(savedGasolineConsumption)
                }
            } else if (fuelType == FUEL_TYPE_ETHANOL) {
                etEthanolConsumption.requestFocus()

                val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
                val savedEthanolConsumption = sharedPreferences.getString("ethanol_consumption", "")
                if (!savedEthanolConsumption.isNullOrEmpty() && etEthanolConsumption.text.isEmpty()) {
                    etEthanolConsumption.setText(savedEthanolConsumption)
                }
            }
        }
    }

    private fun saveConsumptionValues() {
        val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get current values from EditText fields
        val gasolineConsumption = etGasolineConsumption.text.toString()
        val ethanolConsumption = etEthanolConsumption.text.toString()

        // Save only non-empty values
        if (gasolineConsumption.isNotEmpty()) {
            editor.putString("gasoline_consumption", gasolineConsumption)
        }

        if (ethanolConsumption.isNotEmpty()) {
            editor.putString("ethanol_consumption", ethanolConsumption)
        }

        editor.apply()
    }

    private fun loadSavedConsumptionValues() {
        val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)

        val savedGasolineConsumption = sharedPreferences.getString("gasoline_consumption", "")
        val savedEthanolConsumption = sharedPreferences.getString("ethanol_consumption", "")

        if (!savedGasolineConsumption.isNullOrEmpty()) {
            etGasolineConsumption.setText(savedGasolineConsumption)
        }

        if (!savedEthanolConsumption.isNullOrEmpty()) {
            etEthanolConsumption.setText(savedEthanolConsumption)
        }
    }


}
