package dev.luckybet100.waveform.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionChecker {

    fun checkPermissions(context: Context, permissions: List<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}