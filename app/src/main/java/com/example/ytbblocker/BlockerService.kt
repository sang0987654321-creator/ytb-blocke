package com.example.ytbblocker

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import java.text.SimpleDateFormat
import java.util.*

class BlockerService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var isYoutubeOpen = false

    private val youtubePackages = setOf(
        "com.google.android.youtube.tv",
        "com.google.android.youtube.tvkids",
        "com.google.android.youtube",
        "com.liskovsoft.smarttubetv.beta",
        "com.liskovsoft.smarttubetv",
        "com.smarttube.stable"
    )

    // Chặn trình gỡ cài đặt gói (không cho xóa app nếu chưa mở khóa tạm thời)
    private val installerPackages = setOf(
        "com.android.packageinstaller",
        "com.google.android.packageinstaller"
    )

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isYoutubeOpen) {
                val prefs = getSharedPreferences("YTB_PREFS", Context.MODE_PRIVATE)
                val today = SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(Date())
                val savedDate = prefs.getString("LAST_DATE", "")
                
                val defaultLimitSeconds = prefs.getInt("DAILY_LIMIT_MIN", 60) * 60
                var remainingSeconds = if (today != savedDate) {
                    prefs.edit().putString("LAST_DATE", today).putInt("REMAINING_SEC", defaultLimitSeconds).apply()
                    defaultLimitSeconds
                } else {
                    prefs.getInt("REMAINING_SEC", defaultLimitSeconds)
                }

                if (remainingSeconds <= 0) {
                    showLockScreen("YTB")
                } else {
                    remainingSeconds -= 5
                    prefs.edit().putInt("REMAINING_SEC", remainingSeconds).apply()
                }
            }
            handler.postDelayed(this, 5000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(timerRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (pkg == this.packageName || pkg.contains("inputmethod")) return

            val prefs = getSharedPreferences("YTB_PREFS", Context.MODE_PRIVATE)

            // 1. Nếu ai đó bấm Gỡ cài đặt (Uninstall) -> Khóa ngay bằng mật khẩu!
            val allowUninstallUntil = prefs.getLong("ALLOW_UNINSTALL_UNTIL", 0L)
            if (installerPackages.contains(pkg) && System.currentTimeMillis() > allowUninstallUntil) {
                showLockScreen("UNINSTALL")
                return
            }

            // 2. Kiểm tra YouTube
            isYoutubeOpen = youtubePackages.contains(pkg)
            if (isYoutubeOpen && prefs.getInt("REMAINING_SEC", 3600) <= 0) {
                showLockScreen("YTB")
            }
        }
    }

    private fun showLockScreen(reason: String) {
        val lockIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("REASON", reason)
        }
        startActivity(lockIntent)
    }

    override fun onInterrupt() {
        handler.removeCallbacks(timerRunnable)
    }
}
