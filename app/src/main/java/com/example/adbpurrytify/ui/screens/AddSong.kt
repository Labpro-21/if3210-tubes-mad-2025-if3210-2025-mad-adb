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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.SpotifyBlack
import com.example.adbpurrytify.ui.theme.SpotifyButtonShape
import com.example.adbpurrytify.ui.theme.SpotifyGray
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
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
    onDismiss: () -> Unit,
    viewModel: SongViewModel = hiltViewModel()
) {
    if (!show) return
    val padding = 24.dp

    // ModalBottomSheet params
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Remember user input, even when the screen is rotated
    var titleText by rememberSaveable { mutableStateOf("") }
    var artistText by rememberSaveable { mutableStateOf("") }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var fileUri by rememberSaveable { mutableStateOf<Uri?>(null) }

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

    // State to hold current user ID
    var currentUserId by rememberSaveable { mutableStateOf(-1L) }

    // Load current user when composable launches
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
        val id = viewModel.getCurrentUserId()
        if (id != null) {
            currentUserId = id
        }
    }

    ADBPurrytifyTheme {
        Surface {
            ModalBottomSheet(
                onDismissRequest = { onDismiss() },
                sheetState = sheetState,
                containerColor = SpotifyBlack,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(
                                SpotifyGray,
                                RoundedCornerShape(2.dp)
                            )
                            .size(width = 32.dp, height = 4.dp)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Title
                    Text(
                        text = "Upload Song",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Image and Audio Upload Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Album Art Upload
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Album Art",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f) // Force square aspect ratio
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpotifyLightBlack)
                                    .clickable {
                                        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri == null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.upload_file),
                                            contentDescription = "Upload Photo",
                                            modifier = Modifier.size(48.dp),
                                            alpha = 0.7f
                                        )
                                        Text(
                                            text = "Tap to upload",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(photoUri),
                                        contentDescription = "Selected Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop // This ensures the image fills the square container
                                    )
                                }
                            }
                        }

                        // Audio File Upload
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Audio File",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f) // Force square aspect ratio
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpotifyLightBlack)
                                    .clickable {
                                        pickAudio.launch("audio/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        painter = if (fileUri != null)
                                            painterResource(R.drawable.song_art_placeholder)
                                        else
                                            painterResource(R.drawable.upload_file),
                                        contentDescription = "Upload Audio",
                                        modifier = Modifier.size(48.dp),
                                        alpha = if (fileUri != null) 1f else 0.7f
                                    )
                                    Text(
                                        text = if (fileUri != null) "Audio selected" else "Tap to upload",
                                        color = if (fileUri != null) SpotifyGreen else Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Title Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Title",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SpotifyLightBlack,
                                focusedContainerColor = SpotifyLightBlack,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedBorderColor = SpotifyGray,
                                focusedBorderColor = SpotifyGreen,
                                unfocusedPlaceholderColor = Color.Gray,
                                focusedPlaceholderColor = Color.Gray
                            ),
                            value = titleText,
                            onValueChange = { titleText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter song title") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            maxLines = 1,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Artist Input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Artist",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SpotifyLightBlack,
                                focusedContainerColor = SpotifyLightBlack,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedBorderColor = SpotifyGray,
                                focusedBorderColor = SpotifyGreen,
                                unfocusedPlaceholderColor = Color.Gray,
                                focusedPlaceholderColor = Color.Gray
                            ),
                            value = artistText,
                            onValueChange = { artistText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter artist name") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            maxLines = 1,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SpotifyLightBlack,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                val title = titleText
                                val author = artistText
                                var audioUri = fileUri.toString()
                                var artUri = photoUri.toString()

                                val currentTimestamp = java.time.Instant.now().toEpochMilli()

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
                                    viewModel.insert(song)
                                    sheetState.hide()
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotifyGreen,
                                contentColor = Color.Black,
                                disabledContainerColor = SpotifyGreen.copy(alpha = 0.5f),
                                disabledContentColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = titleText.isNotEmpty() &&
                                    artistText.isNotEmpty() &&
                                    photoUri != null &&
                                    fileUri != null &&
                                    currentUserId != -1L
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
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