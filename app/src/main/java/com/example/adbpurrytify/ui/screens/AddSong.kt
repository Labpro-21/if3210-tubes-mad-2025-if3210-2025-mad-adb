package com.example.adbpurrytify.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSong() {
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

        // Uncomment for DEBUGGING only, after enabling the below code, kindly check logcat
        // Log.d("PhotoUri", "Photo URI: $photoUri")
    }

    ADBPurrytifyTheme {
        Surface {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
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
                                .fillMaxWidth(1/2f)
                                .padding(start = padding, top = padding, end = padding)
                        ) {
                            Image(
                                painter = if(photoUri == null) painterResource(R.drawable.upload_file) else rememberAsyncImagePainter(photoUri),
                                contentDescription = "Upload Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(true,
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
                                painter = painterResource(R.drawable.upload_file),
                                contentDescription = "Upload File",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(true, onClick = {})
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1/2f, vertical = padding * 1/4f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("Title")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1/2f),
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
                            .padding(horizontal = padding * 1/2f, vertical = padding * 1/4f),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text("Artist")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1/2f),
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
                            .padding(horizontal = padding * 1/2f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1/2f)
                                .padding(horizontal = padding * 1/4f),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(1f)
                                .padding(horizontal = padding * 1/4f),
                            enabled = titleText.isNotEmpty() && artistText.isNotEmpty() && (photoUri != null)
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
    AddSong()
}