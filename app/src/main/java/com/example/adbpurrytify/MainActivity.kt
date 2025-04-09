package com.example.adbpurrytify

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.ui.navigation.AppNavigation
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController


class MainActivity : ComponentActivity() {

    private val readExternal = READ_EXTERNAL_STORAGE
    private val readImages = READ_MEDIA_IMAGES
    private val readAudio = READ_MEDIA_AUDIO
    private val permissions = arrayOf(
        readAudio, readImages
    )
    private lateinit var songViewModel: SongViewModel
    private lateinit var appDatabase: AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Create the AppDatabase instance
        appDatabase = AppDatabase.getDatabase(applicationContext)
        songViewModel = SongViewModel(appDatabase.songDao())

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





