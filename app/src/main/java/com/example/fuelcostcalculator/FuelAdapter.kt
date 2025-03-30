package com.example.fuelcostcalculator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class FuelAdapter(
    private val context: Context,
    private val fuels: List<Fuel>,
    private val onFuelSelected: (Fuel) -> Unit,
    private val onFuelDeleted: (Fuel) -> Unit
) : RecyclerView.Adapter<FuelAdapter.FuelViewHolder>() {

    class FuelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFuelName: TextView = view.findViewById(R.id.tvFuelName)
        val btnDeleteFuel: ImageButton = view.findViewById(R.id.btnDeleteFuel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fuel_selection, parent, false)
        return FuelViewHolder(view)
    }

    override fun onBindViewHolder(holder: FuelViewHolder, position: Int) {
        val fuel = fuels[position]
        holder.tvFuelName.text = fuel.name

        holder.btnDeleteFuel.visibility = View.VISIBLE
        holder.btnDeleteFuel.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Excluir combustível")
                .setMessage("Tem certeza que deseja excluir o combustível ${fuel.name}?")
                .setPositiveButton("Excluir") { _, _ ->
                    onFuelDeleted(fuel)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.itemView.setOnClickListener {
            onFuelSelected(fuel)
        }
    }

    override fun getItemCount() = fuels.size
}