package com.example.fuelcostcalculator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.fuelcostcalculator.MainActivity
import com.example.fuelcostcalculator.R

class FuelSelectionActivity : AppCompatActivity() {
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_selection)

        listView = findViewById(R.id.lvFuelOptions)

        val fuelOptions = arrayOf(
            getString(R.string.gasoline),
            getString(R.string.ethanol)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fuelOptions)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val fuelType = if (position == 0) {
                MainActivity.FUEL_TYPE_GASOLINE
            } else {
                MainActivity.FUEL_TYPE_ETHANOL
            }

            // Load saved consumption value based on selected fuel type
            val sharedPreferences = getSharedPreferences("FuelPreferences", MODE_PRIVATE)
            val consumptionKey = if (position == 0) "gasoline_consumption" else "ethanol_consumption"
            val savedConsumption = sharedPreferences.getString(consumptionKey, "")

            // Return the selected fuel type and saved consumption to MainActivity
            val intent = Intent()
            intent.putExtra(MainActivity.EXTRA_FUEL_TYPE, fuelType)
            intent.putExtra("saved_consumption", savedConsumption)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
