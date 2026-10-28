package com.example.ytbblocker
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Đã bật tính năng Chống Xóa App!", Toast.LENGTH_SHORT).show()
    }
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "CẢNH BÁO: Không được phép tắt quyền quản trị của ứng dụng này!"
    }
}
