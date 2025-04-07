package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
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
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Composable
fun ProfileScreen(user: User) {
    val padding = 8.dp
    val columnFillHeight = 70.dp
    ADBPurrytifyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,

            ) {
                Spacer(modifier = Modifier.padding(padding * 2))
                Row(
                    modifier = Modifier.padding(all = padding)
                ) {
                    Image(
                        painter = painterResource(R.drawable.remembering_sunday),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.padding(padding * 1/2f))
                Text(text = user.userName)
                Spacer(modifier = Modifier.padding(padding * 1/8f))
                Text(text = user.location)
                Spacer(modifier = Modifier.padding(padding * 1/2f))
                FilledTonalButton(
                    onClick = {},
                    enabled = true
                ) {
                    Text("Edit Profile")
                }
                Spacer(modifier = Modifier.padding(padding * 1/2))
                Row {
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1/3f)
                            .requiredHeight(columnFillHeight),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("135")
                        Text("Songs")
                    }
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1/2f)
                            .requiredHeight(columnFillHeight),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("32")
                        Text("Liked")
                    }
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1f)
                            .requiredHeight(columnFillHeight),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("50")
                        Text("Listened")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    val user = User(
        id = 0,
        userName = "My Username",
        email = "test@email.com",
        image = "pathToImage",
        location = "Indonesia",
        createdAt = "10/10/2010",
        updatedAt = "10/10/2010"
    )

    ADBPurrytifyTheme {
        Surface {
            ProfileScreen(user)
        }
    }
}