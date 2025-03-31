package com.example.fuelcostcalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var fuelInputContainer: LinearLayout
    private lateinit var btnSelectFuel: Button
    private lateinit var btnCalculate: Button
    private lateinit var btnAddFuel: Button
    private lateinit var tvResult: TextView

    // Maps to store references to EditText fields
    val priceFields = mutableMapOf<String, EditText>()
    val consumptionFields = mutableMapOf<String, EditText>()

    companion object {
        const val EXTRA_FUEL_TYPE = "fuel_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fuelInputContainer = findViewById(R.id.fuelInputContainer)
        btnSelectFuel = findViewById(R.id.btnSelectFuel)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnAddFuel = findViewById(R.id.btnAddFuel)
        tvResult = findViewById(R.id.tvResult)

        createFuelInputFields()
        loadSavedConsumptionValues()
        FuelManager.loadFuels(this)

        btnSelectFuel.setOnClickListener {
            val intent = Intent(this, FuelSelectionActivity::class.java)
            selectFuelLauncher.launch(intent)
        }

        btnCalculate.setOnClickListener {
            calculateMostEconomicalFuel()
            saveConsumptionValues()
        }

        btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }

        val btnClearAll = findViewById<Button>(R.id.btnClearAll)
        btnClearAll.setOnClickListener {
            clearAllInputs()
        }

    }

    override fun onResume() {
        super.onResume()
        createFuelInputFields()
        loadSavedConsumptionValues()
    }

    private fun createFuelInputFields() {
        fuelInputContainer.removeAllViews()
        priceFields.clear()
        consumptionFields.clear()

        for (fuel in FuelManager.availableFuels) {
            if (!fuel.isVisible) continue

            val fuelCard = layoutInflater.inflate(R.layout.item_fuel_input, fuelInputContainer, false)

            val tvFuelName = fuelCard.findViewById<TextView>(R.id.tvFuelName)
            tvFuelName.text = fuel.name

            val etPrice = fuelCard.findViewById<EditText>(R.id.etPrice)
            val etConsumption = fuelCard.findViewById<EditText>(R.id.etConsumption)

            val btnHide = fuelCard.findViewById<Button>(R.id.btnHide)
            btnHide.setOnClickListener {
                FuelManager.toggleFuelVisibility(fuel.id)
                createFuelInputFields()
            }

            etPrice.addTextChangedListener(DecimalInputTextWatcher(etPrice))
            etConsumption.addTextChangedListener(DecimalInputTextWatcher(etConsumption))

            priceFields[fuel.id] = etPrice
            consumptionFields[fuel.id] = etConsumption

            fuelInputContainer.addView(fuelCard)
        }

        if (FuelManager.availableFuels.any { !it.isVisible }) {
            val showHiddenCard = layoutInflater.inflate(R.layout.item_show_hidden, fuelInputContainer, false)
            val showHiddenButton = showHiddenCard.findViewById<Button>(R.id.btnShowHidden)
            showHiddenButton.setOnClickListener {
                showHiddenFuelsDialog()
            }
            fuelInputContainer.addView(showHiddenCard)
        }
    }

    private fun showHiddenFuelsDialog() {
        val hiddenFuels = FuelManager.availableFuels.filter { !it.isVisible }
        val fuelNames = hiddenFuels.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Combustíveis ocultos")
            .setItems(fuelNames) { _, position ->
                FuelManager.toggleFuelVisibility(hiddenFuels[position].id)
                createFuelInputFields() // Refresh UI
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearAllInputs() {
        for (editText in priceFields.values) {
            editText.text.clear()
        }

        for (editText in consumptionFields.values) {
            editText.text.clear()
        }

        tvResult.text = ""
        tvResult.visibility = View.GONE
    }

    private fun showAddFuelDialog() {
        val editText = EditText(this)
        editText.hint = "Nome do combustível"

        AlertDialog.Builder(this)
            .setTitle("Adicionar Novo Combustível")
            .setView(editText)
            .setPositiveButton("Adicionar") { _, _ ->
                val fuelName = editText.text.toString().trim()
                if (fuelName.isNotEmpty()) {
                    FuelManager.addCustomFuel(fuelName)
                    FuelManager.saveFuels(this)
                    createFuelInputFields()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun calculateMostEconomicalFuel() {
        val fuelCosts = mutableListOf<Pair<String, Double>>()

        for (fuel in FuelManager.availableFuels) {
            if (!fuel.isVisible) continue

            val priceField = priceFields[fuel.id] ?: continue
            val consumptionField = consumptionFields[fuel.id] ?: continue

            val priceText = priceField.text.toString().replace(",", ".")
            val consumptionText = consumptionField.text.toString().replace(",", ".")

            val price = try {
                priceText.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }

            val consumption = try {
                consumptionText.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }


            if (price > 0 && consumption > 0) {
                val costPerKm = price / consumption
                fuelCosts.add(Pair(fuel.id, costPerKm))
            } else {
                tvResult.text = getString(R.string.invalid_input)
                return
            }
        }

        if (fuelCosts.isEmpty()) {
            tvResult.text = getString(R.string.invalid_input)
            return
        }
        fuelCosts.sortBy { it.second }

        val mostEconomicalFuel = FuelManager.getFuelById(fuelCosts.first().first)

        val resultBuilder = StringBuilder()
        resultBuilder.append("${mostEconomicalFuel?.name} é mais econômico!\n\n")

        for (fuelCost in fuelCosts) {
            val fuel = FuelManager.getFuelById(fuelCost.first)
            resultBuilder.append("Custo por km (${fuel?.name}): R$ ${String.format("%.2f", fuelCost.second)}\n")
        }

        tvResult.text = resultBuilder.toString()
        tvResult.visibility = View.VISIBLE
    }

    private fun saveConsumptionValues() {
        val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        for (fuel in FuelManager.availableFuels) {
            val consumptionField = consumptionFields[fuel.id] ?: continue
            val priceField = priceFields[fuel.id] ?: continue

            val consumption = consumptionField.text.toString()
            val price = priceField.text.toString()

            if (consumption.isNotEmpty()) {
                editor.putString("${fuel.id}_consumption", consumption)
            }

            if (price.isNotEmpty()) {
                editor.putString("${fuel.id}_price", price)
            }
        }

        editor.apply()
    }

    private fun loadSavedConsumptionValues() {
        val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)

        for (fuel in FuelManager.availableFuels) {
            val consumptionField = consumptionFields[fuel.id] ?: continue
            val priceField = priceFields[fuel.id] ?: continue

            val savedConsumption = sharedPreferences.getString("${fuel.id}_consumption", "")
            val savedPrice = sharedPreferences.getString("${fuel.id}_price", "")

            if (!savedConsumption.isNullOrEmpty()) {
                consumptionField.setText(savedConsumption)
            }

            if (!savedPrice.isNullOrEmpty()) {
                priceField.setText(savedPrice)
            }
        }
    }

    private val selectFuelLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val fuelId = result.data?.getStringExtra(EXTRA_FUEL_TYPE) ?: return@registerForActivityResult

            val consumptionField = consumptionFields[fuelId] ?: return@registerForActivityResult
            consumptionField.requestFocus()

            if (consumptionField.text.isEmpty()) {
                val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
                val savedConsumption = sharedPreferences.getString("${fuelId}_consumption", "")
                if (!savedConsumption.isNullOrEmpty()) {
                    consumptionField.setText(savedConsumption)
                }
            }
        }
    }
}