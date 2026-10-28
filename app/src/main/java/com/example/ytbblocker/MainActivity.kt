package com.example.ytbblocker

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("YTB_PREFS", Context.MODE_PRIVATE)
        val reason = intent.getStringExtra("REASON") ?: "SETUP"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 40, 50, 40)
            setBackgroundColor(Color.parseColor("#121212"))
        }

        val title = TextView(this).apply {
            text = if (reason == "UNINSTALL") "CHẶN GỠ CÀI ĐẶT ỨNG DỤNG!" else "QUẢN LÝ THỜI GIAN XEM YOUTUBE"
            textSize = 24f
            setTextColor(Color.parseColor("#FF5252"))
            gravity = Gravity.CENTER
        }

        val remainMin = prefs.getInt("REMAINING_SEC", 3600) / 60
        val info = TextView(this).apply {
            text = "Thời gian YouTube còn lại hôm nay: $remainMin phút\nNhập mật khẩu (HHmmddMM) để cài đặt giờ hoặc mở khóa:"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 15, 0, 25)
        }

        val pinInput = EditText(this).apply {
            hint = "Nhập mật khẩu 8 số (HHmmddMM)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            textSize = 20f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            width = 550
        }

        val timeInput = EditText(this).apply {
            hint = "Số phút cho xem mỗi ngày (VD: 60)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("DAILY_LIMIT_MIN", 60).toString())
            textSize = 18f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
            width = 550
        }

        val btnSave = Button(this).apply {
            text = "MỞ KHÓA & CẬP NHẬT SỐ PHÚT XEM"
            setOnClickListener {
                val pass = SimpleDateFormat("HHmmddMM", Locale.getDefault()).format(Date())
                if (pinInput.text.toString() == pass) {
                    val mins = timeInput.text.toString().toIntOrNull() ?: 60
                    prefs.edit()
                        .putInt("DAILY_LIMIT_MIN", mins)
                        .putInt("REMAINING_SEC", mins * 60)
                        .putLong("ALLOW_UNINSTALL_UNTIL", System.currentTimeMillis() + 120000) // Cho phép gỡ/cài đặt trong 2 phút nếu cần
                        .apply()
                    Toast.makeText(this@MainActivity, "Thành công! Đã cấp $mins phút xem YouTube.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Sai mật khẩu! (Định dạng: GiờPhútNgàyTháng)", Toast.LENGTH_SHORT).show()
                    pinInput.text.clear()
                }
            }
        }

        val btnAdmin = Button(this).apply {
            text = "Bước 1: Bật Chống Xóa App (Device Admin)"
            setOnClickListener {
                val comp = ComponentName(this@MainActivity, AdminReceiver::class.java)
                val i = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Bảo vệ ứng dụng không bị gỡ cài đặt.")
                }
                startActivity(i)
            }
        }

        val btnAccess = Button(this).apply {
            text = "Bước 2: Bật Quyền Canh Giờ (Accessibility)"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(pinInput)
        layout.addView(timeInput)
        layout.addView(btnSave)
        layout.addView(btnAdmin)
        layout.addView(btnAccess)

        setContentView(layout)
        pinInput.requestFocus()
    }

    override fun onBackPressed() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(home)
    }
}
