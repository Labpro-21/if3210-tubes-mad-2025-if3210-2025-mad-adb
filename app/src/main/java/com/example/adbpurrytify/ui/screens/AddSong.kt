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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSong() {
    val padding = 32.dp
    var uploadPhotoPainter = painterResource(R.drawable.upload_file)
    var titleText by remember { mutableStateOf("") }
    var artistText by remember { mutableStateOf("") }
    var photoSelected = false

    ADBPurrytifyTheme {
        Surface {
            ModalBottomSheet(onDismissRequest = {}) {
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
                                painter = uploadPhotoPainter,
                                contentDescription = "Upload Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(true, onClick = {})
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
                            onClick = {},
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
                            enabled = titleText.isNotEmpty() && artistText.isNotEmpty() && photoSelected
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