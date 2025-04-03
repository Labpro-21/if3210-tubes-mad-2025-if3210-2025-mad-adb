package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Composable
fun ProfileScreen(userName: String, location: String) {
    ADBPurrytifyTheme {
        Surface {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(all = 8.dp)) {
                    Image(
                        painter = painterResource(R.drawable.remembering_sunday),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = userName)
                Text(text = location)
                Button(onClick = {}) {
                    Text("Edit Profile")
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    ADBPurrytifyTheme {
        Surface {
            ProfileScreen(userName = "13522XX", location = "ID")
        }
    }
}