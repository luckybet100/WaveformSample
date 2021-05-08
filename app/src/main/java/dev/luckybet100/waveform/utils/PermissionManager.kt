package dev.luckybet100.waveform.utils

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

class PermissionManager(private val activity: WeakReference<Activity>) {

    companion object {
        private const val CODE = 179
        private const val KEY = "request_permission"
    }

    private val prefs = activity.get()!!.getSharedPreferences("PermissionManager", MODE_PRIVATE)

    fun requestPermissions(permissions: List<String>) = activity.get()?.let {
        if (prefs.getBoolean(KEY, true)) {
            ActivityCompat.requestPermissions(it, permissions.toTypedArray(), CODE)
        } else {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", it.packageName, null)
            intent.data = uri
            it.startActivity(intent)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (requestCode != CODE)
            return false
        var allPermissionGranted = true
        for (index in grantResults.indices) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                allPermissionGranted = false
            }
        }
        if (!allPermissionGranted) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity.get()!!,
                        permission
                    ) && ActivityCompat.checkSelfPermission(
                        activity.get()!!,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    prefs.edit().apply {
                        putBoolean(KEY, false)
                        commit()
                    }
                    break
                }
            }
        }
        return true
    }

}