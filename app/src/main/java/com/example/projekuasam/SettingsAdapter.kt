package com.example.projekuasam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter(
    private val settingsList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSettingName: TextView = itemView.findViewById(R.id.tvSettingName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val settingName = settingsList[position]
        holder.tvSettingName.text = settingName
        holder.itemView.setOnClickListener { onItemClick(settingName) }
    }

    override fun getItemCount(): Int = settingsList.size
}