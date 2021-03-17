package com.tck.av.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 12:45</p>
 * @author tck
 * @version v1.0
 *
 */
object PermissionsUtils {
    val REQUEST_PERMISSIONS_CODE = 1000

    val RECORD_AUDIO_PERMISSIONS = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO,
    )

    fun checkAudioPermissions(context: Context): Boolean {
        return RECORD_AUDIO_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestAudioPermissions(activity: Activity) {
        requestPermissions(activity, RECORD_AUDIO_PERMISSIONS)
    }

    fun onRequestPermissionsResult(
        context: Context,
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {
        var result = false
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            result = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        }
        if (!result) {
            Toast.makeText(context, "请打开麦克风权限", Toast.LENGTH_SHORT).show()
        }
        return result
    }

    private fun requestPermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity,permissions, REQUEST_PERMISSIONS_CODE)
    }


}