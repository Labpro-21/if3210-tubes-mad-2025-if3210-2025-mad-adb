package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.Green


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSong() {
    val padding = 32.dp
    var titleText by remember { mutableStateOf("") }
    var artistText by remember { mutableStateOf("") }
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
                                .padding(all = padding)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.remembering_sunday),
                                contentDescription = "Upload Photo"
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(all = padding)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.remembering_sunday),
                                contentDescription = "Upload File"
                            )
                        }
                    }
                    Spacer(Modifier.padding(padding * 1/4f))
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
                            placeholder = { Text("Title") }
                        )
                    }
                    Spacer(Modifier.padding(padding * 1/2f))
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
                            placeholder = { Text("Artist") }
                        )
                    }
                    Spacer(Modifier.padding(padding))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = padding * 1/2f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalButton(
                            onClick = {}
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.padding(horizontal = padding))
                        Button(
                            onClick = {},
                            enabled = titleText.isNotEmpty() && artistText.isNotEmpty()
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