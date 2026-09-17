package com.libremobileos.sidebar.ui.all_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.bean.AppInfo
import com.libremobileos.sidebar.utils.Logger
import kotlinx.coroutines.launch

/**
 * @author KindBrave
 * @since 2023/10/25
 */
class AllAppActivity : ComponentActivity() {
    private val logger = Logger(TAG)
    private val viewModel: AllAppViewModel by viewModels { AllAppViewModel.Factory }

    companion object {
        private const val PACKAGE = "com.libremobileos.freeform"
        private const val ACTION = "com.libremobileos.freeform.START_FREEFORM"
        private const val TAG = "AllAppActivity"
        private const val COLUMNS = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d("onCreate")

        setContentView(R.layout.activity_all_app)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, COLUMNS)
        recyclerView.adapter = AllAppAdapter(::onClick)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appListFlow.collect { appList ->
                    (recyclerView.adapter as? AllAppAdapter)?.submitList(appList)
                }
            }
        }
    }

    override fun onDestroy() {
        logger.d("onDestroy")
        super.onDestroy()
    }

    private fun onClick(appInfo: AppInfo) {
        val intent = Intent(ACTION).apply {
            setPackage(PACKAGE)
            putExtra("packageName", appInfo.packageName)
            putExtra("activityName", appInfo.activityName)
            putExtra("userId", appInfo.userId)
        }
        sendBroadcast(intent)
        finish()
    }

    private class AllAppAdapter(
        private val onClick: (AppInfo) -> Unit
    ) : ListAdapter<AppInfo, AllAppAdapter.AppViewHolder>(DIFF) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.all_app_grid_item, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val iconView = itemView.findViewById<ImageView>(R.id.icon)
            private val labelView = itemView.findViewById<TextView>(R.id.label)

            fun bind(appInfo: AppInfo) {
                iconView.setImageDrawable(appInfo.icon)
                labelView.text = appInfo.label
                itemView.setOnClickListener { onClick(appInfo) }
            }
        }

        companion object {
            private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
                override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
                    return oldItem.packageName == newItem.packageName &&
                        oldItem.activityName == newItem.activityName
                }

                override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }
}