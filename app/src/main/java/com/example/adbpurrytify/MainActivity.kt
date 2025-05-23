package com.example.adbpurrytify

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.adbpurrytify.api.MusicService
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.ui.navigation.AppNavigation
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.viewmodels.PlayerViewModel
import com.example.adbpurrytify.ui.viewmodels.SongViewModel


class MainActivity : ComponentActivity() {

    private val readExternal = READ_EXTERNAL_STORAGE
    private val readImages = READ_MEDIA_IMAGES
    private val readAudio = READ_MEDIA_AUDIO
    private val permissions = arrayOf(
        readAudio, readImages
    )
    private lateinit var songViewModel: SongViewModel
    private lateinit var appDatabase: AppDatabase
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
//        // Create the AppDatabase instance
        TokenManager.initialize(this)
        appDatabase = AppDatabase.getDatabase(applicationContext)
        songViewModel = SongViewModel(appDatabase.songDao())


        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                var controller = controllerFuture.get()
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d("Controller", "Playing: $isPlaying")
                    }
                })
                mediaController = controller
                var vm = ViewModelProvider(this)[PlayerViewModel::class.java]
                vm.connect(controller)

            } catch (e: Exception) {
                Log.e("Controller", "Failed to connect", e)
            }
        }, ContextCompat.getMainExecutor(this))

        enableEdgeToEdge()
        setContent {
            ADBPurrytifyTheme {
//                TestPlayer("https://storage.googleapis.com/mad-public-bucket/mp3/Alex%20Warren%20-%20Ordinary%20(Official%20Lyric%20Video).mp3")
                AppNavigation()
                requestPermissions() // ini maybe just leave it here,
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaController?.release()
        mediaController = null
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





