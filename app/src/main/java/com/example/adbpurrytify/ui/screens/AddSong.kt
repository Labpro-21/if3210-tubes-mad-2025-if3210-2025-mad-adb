package com.example.adbpurrytify.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.adbpurrytify.R
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    val returnCursor = contentResolver.query(uri, null, null, null, null) ?: return null

    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    returnCursor.close()

    val inputStream = contentResolver.openInputStream(uri) ?: return null
    val file = File(context.filesDir, name)
    val outputStream = FileOutputStream(file)

    try {
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    } catch (e: Exception) {
        Log.e("copyUri", "Error copying file: ${e.message}")
        return null
    } finally {
        inputStream.close()
        outputStream.close()
    }

    return file.absolutePath
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSong(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return
    val padding = 32.dp

    // ModalBottomSheet params
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Remember user input, even when the screen is rotated :(
    var titleText by rememberSaveable { mutableStateOf("") }
    var artistText by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }
    var fileUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        photoUri = uri
    }

    val context = LocalContext.current
    val pickAudio = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fileUri = uri
        uri?.let {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, it)

            val retrievedTitle = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
            val retrievedArtist = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)

            if (titleText.isEmpty() && !retrievedTitle.isNullOrEmpty()) {
                titleText = retrievedTitle
            }
            if (artistText.isEmpty() && !retrievedArtist.isNullOrEmpty()) {
                artistText = retrievedArtist
            }

            retriever.release()
        }
    }
    val authRepository = AuthRepository(RetrofitClient.instance)

// State to hold current user ID
    var currentUserId by rememberSaveable { mutableStateOf(-1L) }

// Load current user when composable launches
    LaunchedEffect(Unit) {
        val userProfile = authRepository.currentUser()

        // Set user ID or default to -1
        currentUserId = userProfile?.id ?: -1L
    }

    ADBPurrytifyTheme {
        Surface {
            ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Upload Song")
                    }
                    Row {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1 / 2f)
                                .padding(start = padding, top = padding, end = padding)
                        ) {
                            Image(
                                painter = if(photoUri == null) painterResource(R.drawable.upload_file) else rememberAsyncImagePainter(photoUri),
                                contentDescription = "Upload Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        true,
                                        onClick = {
                                            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                        }
                                    )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(start = padding, top = padding, end = padding)
                        ) {
                            Image(
                                painter = if (fileUri != null) painterResource(R.drawable.song_art_placeholder) else painterResource(R.drawable.upload_file),
                                contentDescription = "Upload File",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(true, onClick = {
                                        pickAudio.launch("audio/*")
                                    })
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1 / 2f, vertical = padding * 1 / 4f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("Title")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1 / 2f),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            placeholder = { Text("Title") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.padding(padding * 1/4f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1 / 2f, vertical = padding * 1 / 4f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("Artist")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1 / 2f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        OutlinedTextField(
                            value = artistText,
                            onValueChange = { artistText = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            placeholder = { Text("Artist") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.padding(padding * 1/2f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(padding * 1.3f)
                            .padding(horizontal = padding * 1 / 2f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1 / 2f)
                                .padding(horizontal = padding * 1 / 4f),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val title = titleText
                                val author = artistText
                                var audioUri = fileUri.toString()
                                var artUri = photoUri.toString()

                                // Get current timestamp for lastPlayedTimestamp
                                val currentTimestamp = java.time.Instant.now().toString()

                                try {
                                    val copiedPath = copyUriToInternalStorage(context, Uri.parse(artUri))
                                    if (copiedPath != null) artUri = copiedPath
                                    else artUri = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to read/copy the image file", Toast.LENGTH_SHORT).show()
                                    Log.d("File Copy Err", e.message!!)
                                    artUri = ""
                                    return@Button
                                }

                                try {
                                    val copiedPath = copyUriToInternalStorage(context, Uri.parse(audioUri))
                                    if (copiedPath != null) audioUri = copiedPath
                                    else audioUri = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to read/copy the audio file", Toast.LENGTH_SHORT).show()
                                    Log.d("File Copy Err", e.message!!)
                                    artUri = ""
                                    return@Button
                                }

                                val song = SongEntity(
                                    title = title,
                                    author = author,
                                    artUri = artUri,
                                    audioUri = audioUri,
                                    userId = currentUserId,
                                    isLiked = false,
                                    lastPlayedTimestamp = currentTimestamp,
                                    lastPlayedPositionMs = 0
                                )

                                scope.launch {
                                    val db = AppDatabase.getDatabase(context)
                                    val songDao = db.songDao()
                                    val songViewModel = SongViewModel(songDao)
                                    songViewModel.insert(song)
                                    sheetState.hide()
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1f)
                                .padding(horizontal = padding * 1 / 4f),
                            enabled = titleText.isNotEmpty() &&
                                    artistText.isNotEmpty() &&
                                    photoUri != null &&
                                    fileUri != null &&
                                    currentUserId != -1L
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddSong() {
    var showAddSongSheet by remember { mutableStateOf(false) }
    AddSong(
        show = showAddSongSheet,
        onDismiss = { showAddSongSheet = false }
    )
}