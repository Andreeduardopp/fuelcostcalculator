package com.example.fuelcostcalculator

import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityIntegrationTest {

    @Test
    fun testGasolineMoreEconomical() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.priceFields[FuelManager.GASOLINE]?.setText("5.00")
                activity.priceFields[FuelManager.ETHANOL]?.setText("4.00")
                activity.consumptionFields[FuelManager.GASOLINE]?.setText("10.0")
                activity.consumptionFields[FuelManager.ETHANOL]?.setText("7.0")

                activity.findViewById<Button>(R.id.btnCalculate).performClick()

                val resultText = activity.findViewById<TextView>(R.id.tvResult).text.toString()
                println("DEBUG: Result text is: $resultText")

                assertTrue(resultText.contains("Gasolina é mais econômico!"))
            }
        }
    }

    @Test
    fun testInvalidInputHandling() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.priceFields[FuelManager.GASOLINE]?.setText("5.00")
                activity.priceFields[FuelManager.ETHANOL]?.setText("3.00")
                activity.consumptionFields[FuelManager.GASOLINE]?.setText("") // Empty field
                activity.consumptionFields[FuelManager.ETHANOL]?.setText("7.0")

                activity.findViewById<Button>(R.id.btnCalculate).performClick()

                val resultText = activity.findViewById<TextView>(R.id.tvResult).text.toString()
                println("DEBUG: Result text is: $resultText")
                val invalidInputMessage = activity.getString(R.string.invalid_input)

                assertTrue(resultText.contains(invalidInputMessage))


            }
        }
    }
}