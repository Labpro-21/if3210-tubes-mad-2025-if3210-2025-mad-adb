package com.example.adbpurrytify

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.adbpurrytify.ui.navigation.AppNavigation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

class MainActivity : ComponentActivity() {

    private val readExternal = READ_EXTERNAL_STORAGE
    private val readImages = READ_MEDIA_IMAGES
    private val readAudio = READ_MEDIA_AUDIO
    private val permissions = arrayOf(
        readAudio, readImages
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBPurrytifyTheme {
                AppNavigation()
                requestPermissions() // ini maybe just leave it here,
            }
        }
    }

    /**
     * Kode fungsi ini diambil langsung dari sumber ini.
     * https://medium.com/@ominoblair/android-runtime-permissions-91b42d2fa0a3
     *
     * Cara make: panggil anytime sebelom load lagu ntar.
     * Ini permission KUDU biar bisa load dan up lagu dari fs kalo ga nanti crash.
     * Jadi, TODO: kalo permission denied, kick user dari app karena gbs ngapa2in jg :v
     *
     * */
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notGrantedPermissions = permissions.filterNot { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
            if (notGrantedPermissions.isNotEmpty()) {
                val showRationale = notGrantedPermissions.any { permission ->
                    shouldShowRequestPermissionRationale(permission)
                }
                if (showRationale) {
                    AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("Storage permission is needed in order to play and upload songs!")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            Toast.makeText(
                                this,
                                "Read media storage permission denied!",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK") { _, _ ->
                            videoImagesPermission.launch(notGrantedPermissions.toTypedArray())
                        }
                        .show()
                } else {
                    videoImagesPermission.launch(notGrantedPermissions.toTypedArray())
                }
            } else {
                Toast.makeText(this, "Read media storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    readExternal
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (shouldShowRequestPermissionRationale(readExternal)) {
                    AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("Storage permission is needed in order to play and upload songs!")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            Toast.makeText(
                                this,
                                "Read external storage permission denied!",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK") { _, _ ->
                            readExternalPermission.launch(readExternal)
                        }
                        .show()
                } else {
                    readExternalPermission.launch(readExternal)
                }
            }
        }
    }
    private val videoImagesPermission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissionMap->
        if (permissionMap.all { it.value }){
            Toast.makeText(this, "Media permissions granted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Media permissions not granted!", Toast.LENGTH_SHORT).show()
        }
    }
    private val readExternalPermission=registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
        if (isGranted){
            Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Read external storage permission denied!", Toast.LENGTH_SHORT).show()
        }
    }
}





