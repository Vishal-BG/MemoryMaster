package com.memorymaster

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.memorymaster.R

class AppListAdapter(private val onItemClick: (String) -> Unit,
                     private val onMoreInfoClick: (AppInfo) -> Unit) :
    ListAdapter<AppInfo, AppListAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view, onItemClick, onMoreInfoClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppViewHolder(itemView: View, private val onItemClick: (String) -> Unit,
                        private val onMoreInfoClick: (AppInfo) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val appIconImageView: ImageView = itemView.findViewById(R.id.appIconImageView)
        private val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        private val memoryUsageTextView: TextView = itemView.findViewById(R.id.memoryUsageTextView)
        private val usageTimeTextView: TextView = itemView.findViewById(R.id.usageTimeTextView)
        private val batteryConsumptionTextView: TextView = itemView.findViewById(R.id.batteryConsumptionTextView)
        private val permissionsChip: Chip = itemView.findViewById(R.id.permissionsChip)
        private val moreInfoButton: MaterialButton = itemView.findViewById(R.id.moreInfoButton)

        fun bind(appInfo: AppInfo) {
            appNameTextView.text = appInfo.appName
            memoryUsageTextView.text = formatMemoryUsage(appInfo.memoryUsage)
            usageTimeTextView.text = formatUsageTime(appInfo.usageTime)
            batteryConsumptionTextView.text = formatBatteryConsumption(appInfo.batteryConsumption)
            permissionsChip.text = formatPermissions(appInfo.permissions)
            itemView.setOnClickListener { onItemClick(appInfo.packageName) }
            moreInfoButton.setOnClickListener { onMoreInfoClick(appInfo) }

            // Load app icon
            val packageManager = itemView.context.packageManager
            try {
                val icon = packageManager.getApplicationIcon(appInfo.packageName)
                appIconImageView.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                // If icon not found, set a default icon
                appIconImageView.setImageResource(R.drawable.ic_time)
            }
        }

        private fun formatMemoryUsage(bytes: Long): String {
            val mb = bytes / (1024 * 1024)
            return "${mb}MB"
        }

        private fun formatUsageTime(milliseconds: Long): String {
            val minutes = milliseconds / 60000
            return "${minutes}m"
        }

        private fun formatBatteryConsumption(percentage: Float): String {
            return "${String.format("%.1f", percentage)}%"
        }

        private fun formatPermissions(permissions: List<String>): String {
            return "${permissions.size} Permissions"
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}