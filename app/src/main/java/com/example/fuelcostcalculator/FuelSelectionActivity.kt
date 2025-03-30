package com.example.fuelcostcalculator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FuelSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_selection)

        recyclerView = findViewById(R.id.rvFuelOptions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = FuelAdapter(
            this,
            FuelManager.availableFuels,
            onFuelSelected = { fuel ->
                val intent = Intent()
                intent.putExtra(MainActivity.EXTRA_FUEL_TYPE, fuel.id)
                setResult(Activity.RESULT_OK, intent)
                finish()
            },
            onFuelDeleted = { fuel ->
                FuelManager.deleteFuel(this, fuel.id)
                recyclerView.adapter = FuelAdapter(
                    this,
                    FuelManager.availableFuels,
                    onFuelSelected = { selectedFuel ->
                        val intent = Intent()
                        intent.putExtra(MainActivity.EXTRA_FUEL_TYPE, selectedFuel.id)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    },
                    onFuelDeleted = { fuelToDelete ->
                        FuelManager.deleteFuel(this, fuelToDelete.id)
                    }
                )
            }
        )

        recyclerView.adapter = adapter
    }
}