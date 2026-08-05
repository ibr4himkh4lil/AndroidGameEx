package com.aether.memoryeditor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.aether.memoryeditor.R
import com.aether.memoryeditor.ui.MainActivity
import timber.log.Timber

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayShowing = false

    companion object {
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_PID = "extra_pid"
        const val EXTRA_PROCESS_NAME = "extra_process_name"

        fun start(context: Context, pid: Int, processName: String) {
            val intent = Intent(context, OverlayService::class.java).apply {
                putExtra(EXTRA_PID, pid)
                putExtra(EXTRA_PROCESS_NAME, processName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Timber.d("OverlayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pid = intent?.getIntExtra(EXTRA_PID, -1) ?: -1
        val processName = intent?.getStringExtra(EXTRA_PROCESS_NAME) ?: "Unknown"

        val notification = createNotification(processName, pid)
        startForeground(NOTIFICATION_ID, notification)

        if (!isOverlayShowing) {
            showOverlay(pid, processName)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        Timber.d("OverlayService destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Memory Editor Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating overlay for memory editing"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(processName: String, pid: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Memory Editor Active")
            .setContentText("Attached to: $processName (PID: $pid)")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay(pid: Int, processName: String) {
        try {
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 100
            }

            overlayView = LayoutInflater.from(this).inflate(
                R.layout.overlay_floating_icon,
                null
            )

            overlayView?.setOnClickListener {
                // Open full dashboard or expand overlay
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                startActivity(intent)
            }

            windowManager?.addView(overlayView, layoutParams)
            isOverlayShowing = true
            Timber.d("Overlay shown for process: $processName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show overlay")
        }
    }

    private fun removeOverlay() {
        try {
            if (overlayView != null && isOverlayShowing) {
                windowManager?.removeView(overlayView)
                overlayView = null
                isOverlayShowing = false
                Timber.d("Overlay removed")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove overlay")
        }
    }
}
