package com.example.fuelcostcalculator
import android.content.Context

data class Fuel(
    val id: String,
    val name: String,
    var price: Double = 0.0,
    var consumption: Double = 0.0,
    var isVisible: Boolean = true
)

object FuelManager {
    val GASOLINE = "gasoline"
    val ETHANOL = "ethanol"
    val DIESEL = "diesel"

    val availableFuels = mutableListOf(
        Fuel(GASOLINE, "Gasolina"),
        Fuel(ETHANOL, "Etanol"),
        Fuel(DIESEL, "Diesel"),
    )

    fun getFuelById(id: String): Fuel? {
        return availableFuels.find { it.id == id }
    }

    fun getFuelByPosition(position: Int): Fuel {
        return availableFuels[position]
    }

    fun addCustomFuel(name: String): Fuel {
        val id = "custom_${System.currentTimeMillis()}"
        val newFuel = Fuel(id, name)
        availableFuels.add(newFuel)
        return newFuel
    }

    fun saveFuels(context: Context) {
        val sharedPreferences = context.getSharedPreferences("FuelPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val customFuels = availableFuels.filter { it.id.startsWith("custom_") }
        val fuelSet = customFuels.map { "${it.id},${it.name}" }.toSet()

        editor.putStringSet("custom_fuels", fuelSet)
        editor.apply()
    }

    fun loadFuels(context: Context) {
        val sharedPreferences = context.getSharedPreferences("FuelPreferences", Context.MODE_PRIVATE)
        val fuelSet = sharedPreferences.getStringSet("custom_fuels", emptySet()) ?: emptySet()

        for (fuelString in fuelSet) {
            val parts = fuelString.split(",", limit = 2)
            if (parts.size == 2) {
                val id = parts[0]
                val name = parts[1]

                // Only add if not already in the list
                if (availableFuels.none { it.id == id }) {
                    availableFuels.add(Fuel(id, name))
                }
            }
        }
    }

    fun deleteFuel(context: Context, fuelId: String) {
        val fuelToRemove = availableFuels.find { it.id == fuelId }

        if (fuelToRemove != null) {
            if (fuelId == GASOLINE || fuelId == ETHANOL || fuelId == DIESEL) {
                fuelToRemove.isVisible = false
            } else {
                availableFuels.remove(fuelToRemove)
            }

            saveFuels(context)
        }
    }

    fun toggleFuelVisibility(fuelId: String) {
        val fuel = getFuelById(fuelId)
        fuel?.let {
            it.isVisible = !it.isVisible
        }
    }

}