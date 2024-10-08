package com.memorymaster

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.memorymaster.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MemoryViewModel
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MemoryViewModel::class.java]

        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        } else {
            setupUI()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs access to your usage data to function properly. Please grant the permission in the next screen.")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivityForResult(intent, USAGE_STATS_PERMISSION_REQUEST)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == USAGE_STATS_PERMISSION_REQUEST) {
            if (hasUsageStatsPermission()) {
                setupUI()
            } else {
                Snackbar.make(binding.root, "Permission not granted. App cannot function properly.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant Permission") {
                        requestUsageStatsPermission()
                    }
                    .show()
            }
        }
    }

    private fun setupUI() {
        setupRecyclerView()
        observeViewModelData()
        setupClickListeners()

        viewModel.startMemoryMonitoring()
    }

    private fun setupRecyclerView() {
        appListAdapter = AppListAdapter(
            onItemClick = { packageName ->
                viewModel.prioritizeForegroundApp(packageName)
                Snackbar.make(binding.root, "Prioritized $packageName", Snackbar.LENGTH_SHORT).show()
            },
            onMoreInfoClick = { appInfo ->
                showAppDetailFragment(appInfo)
            }
        )
        binding.appListRecyclerView.apply {
            adapter = appListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun showAppDetailFragment(appInfo: AppInfo) {
        val fragment = AppDetailFragment.newInstance(appInfo)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeViewModelData() {
        viewModel.detailedMemoryInfo.observe(this) { detailedMemoryInfo ->
            updateMemoryUI(detailedMemoryInfo)
        }

        viewModel.recentlyUsedApps.observe(this) { recentlyUsedApps ->
            appListAdapter.submitList(recentlyUsedApps)
        }

        viewModel.memoryLeaks.observe(this) { leaks ->
            updateMemoryLeaksUI(leaks)
        }

        viewModel.predictedMemoryAllocations.observe(this) { allocations ->
            updatePredictedAllocationsUI(allocations)
        }
    }

    private fun setupClickListeners() {
        binding.optimizeButton.setOnClickListener {
            binding.optimizeButton.isEnabled = false
            binding.optimizeProgressBar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.optimizeMemory()
                binding.optimizeProgressBar.visibility = View.GONE
                binding.optimizeButton.isEnabled = true
                showOptimizationAnimation()
            }
        }
    }

    private fun updateMemoryUI(detailedMemoryInfo: DetailedMemoryInfo) {
        val usedMemory = detailedMemoryInfo.totalMem - detailedMemoryInfo.availMem
        val usedPercentage = (usedMemory.toFloat() / detailedMemoryInfo.totalMem * 100).toInt()

        binding.memoryUsageBar.progress = usedPercentage
        binding.memoryUsageText.text = getString(R.string.memory_usage_format, usedPercentage)

        val color = when {
            usedPercentage > 90 -> Color.RED
            usedPercentage > 70 -> Color.parseColor("#FFA500")
            else -> Color.GREEN
        }
        binding.memoryUsageText.setTextColor(color)
    }

    private fun updateMemoryLeaksUI(leaks: List<AppMemoryLeak>) {
        if (leaks.isEmpty()) {
            binding.memoryLeaksCard.visibility = View.GONE
        } else {
            binding.memoryLeaksCard.visibility = View.VISIBLE
            binding.memoryLeaksText.text = leaks.joinToString("\n") {
                "${it.packageName}: ${String.format("%.2f", it.leakTrend * 100)}% increase"
            }
            animateCardEntrance(binding.memoryLeaksCard)
        }
    }

    private fun updatePredictedAllocationsUI(allocations: Map<String, Long>) {
        if (allocations.isEmpty()) {
            binding.predictedAllocationsCard.visibility = View.GONE
        } else {
            binding.predictedAllocationsCard.visibility = View.VISIBLE
            binding.predictedAllocationsText.text = allocations.entries.joinToString("\n") {
                "${it.key}: ${it.value / 1024 / 1024}MB"
            }
            animateCardEntrance(binding.predictedAllocationsCard)
        }
    }

    private fun animateCardEntrance(card: MaterialCardView) {
        card.alpha = 0f
        card.translationY = 100f
        card.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showOptimizationAnimation() {
        binding.konfettiView.start(
            Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.5, 0.3)
            )
        )
        Snackbar.make(binding.root, "Memory optimized successfully!", Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val USAGE_STATS_PERMISSION_REQUEST = 1001
    }
}