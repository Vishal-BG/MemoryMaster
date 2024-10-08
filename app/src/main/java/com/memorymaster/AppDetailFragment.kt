package com.memorymaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class AppDetailFragment : Fragment() {

    companion object {
        private const val ARG_APP_INFO = "arg_app_info"

        fun newInstance(appInfo: AppInfo): AppDetailFragment {
            return AppDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_APP_INFO, appInfo)
                }
            }
        }
    }

    private lateinit var appInfo: AppInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appInfo = arguments?.getSerializable(ARG_APP_INFO) as? AppInfo
            ?: throw IllegalArgumentException("AppInfo must be provided")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_app_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
    }

    private fun setupViews(view: View) {
        view.findViewById<ImageView>(R.id.appIconImageView).setImageDrawable(getAppIcon())
        view.findViewById<TextView>(R.id.appNameTextView).text = appInfo.appName
        view.findViewById<TextView>(R.id.packageNameTextView).text = appInfo.packageName
        view.findViewById<TextView>(R.id.memoryUsageTextView).text = "Memory Usage: ${formatMemoryUsage(appInfo.memoryUsage)}"
        view.findViewById<TextView>(R.id.usageTimeTextView).text = "Usage Time: ${formatUsageTime(appInfo.usageTime)}"
        view.findViewById<TextView>(R.id.batteryConsumptionTextView).text = "Battery Consumption: ${formatBatteryConsumption(appInfo.batteryConsumption)}"

        setupPermissionsViews(view)
    }

    private fun setupPermissionsViews(view: View) {
        val (systemPermissions, hardwarePermissions) = categorizePermissions(appInfo.permissions)

        val systemPermissionsCard = view.findViewById<MaterialCardView>(R.id.systemPermissionsCard)
        val hardwarePermissionsCard = view.findViewById<MaterialCardView>(R.id.hardwarePermissionsCard)

        if (systemPermissions.isNotEmpty()) {
            systemPermissionsCard.visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.systemPermissionsTextView).text = systemPermissions.joinToString("\n") {
                "• $it: ${getPermissionDescription(it)}"
            }
        } else {
            systemPermissionsCard.visibility = View.GONE
        }

        if (hardwarePermissions.isNotEmpty()) {
            hardwarePermissionsCard.visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.hardwarePermissionsTextView).text = hardwarePermissions.joinToString("\n") {
                "• $it: ${getPermissionDescription(it)}"
            }
        } else {
            hardwarePermissionsCard.visibility = View.GONE
        }
    }

    private fun getAppIcon() = try {
        requireContext().packageManager.getApplicationIcon(appInfo.packageName)
    } catch (e: Exception) {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_time)
    }

    private fun formatMemoryUsage(bytes: Long): String {
        val mb = bytes / (1024 * 1024)
        return "${mb}MB"
    }

    private fun formatUsageTime(milliseconds: Long): String {
        val minutes = milliseconds / 60000
        return "${minutes} minutes"
    }

    private fun formatBatteryConsumption(percentage: Float): String {
        return "${String.format("%.1f", percentage)}%"
    }

    private fun categorizePermissions(permissions: List<String>): Pair<List<String>, List<String>> {
        val systemPermissions = mutableListOf<String>()
        val hardwarePermissions = mutableListOf<String>()

        permissions.forEach { permission ->
            when {
                permission.startsWith("android.permission.") -> {
                    val simplifiedPermission = permission.removePrefix("android.permission.")
                    when {
                        isHardwarePermission(simplifiedPermission) -> hardwarePermissions.add(simplifiedPermission)
                        else -> systemPermissions.add(simplifiedPermission)
                    }
                }
                else -> systemPermissions.add(permission)
            }
        }

        return Pair(systemPermissions, hardwarePermissions)
    }

    private fun isHardwarePermission(permission: String): Boolean {
        val hardwareRelatedPermissions = listOf(
            "CAMERA", "RECORD_AUDIO", "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "BLUETOOTH", "BLUETOOTH_ADMIN", "NFC", "USE_FINGERPRINT", "USE_BIOMETRIC",
            "BODY_SENSORS", "ACTIVITY_RECOGNITION"
        )
        return permission in hardwareRelatedPermissions
    }

    private fun getPermissionDescription(permission: String): String {
        return when (permission) {
            "CAMERA" -> "Allows access to the device's camera."
            "RECORD_AUDIO" -> "Allows recording audio from the device's microphone."
            "ACCESS_FINE_LOCATION" -> "Allows access to precise location information."
            "ACCESS_COARSE_LOCATION" -> "Allows access to approximate location information."
            "READ_EXTERNAL_STORAGE" -> "Allows reading from external storage."
            "WRITE_EXTERNAL_STORAGE" -> "Allows writing to external storage."
            "INTERNET" -> "Allows the app to access the internet."
            // Add more permissions and their descriptions as needed
            else -> "Allows access to ${permission.toLowerCase().replace('_', ' ')}."
        }
    }
}