package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
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
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Composable
fun ProfileScreen(user: User) {
    val padding = 8.dp
    ADBPurrytifyTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.padding(all = 8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.remembering_sunday),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
                Text(text = user.userName)
                Text(text = user.location)
                Button(
                    onClick = {},
                    enabled = true
                ) {
                    Text("Edit Profile")
                }
                Row(
                    modifier = Modifier
                        .fillMaxHeight(1/8f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1/3f)
                            .requiredHeight(50.dp)
                    ) {
                        Text("135")
                        Text("Songs")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1/2f)
                            .requiredHeight(50.dp)
                    ) {
                        Text("32")
                        Text("Liked")
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxHeight()
                            .fillMaxWidth(1f)
                            .requiredHeight(50.dp)
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
    val user: User = User(
        id = 0,
        userName = "13522XX",
        email = "test@email.com",
        image = "pathToImage",
        location = "ID",
        createdAt = "10/10/2010",
        updatedAt = "10/10/2010"
    )

    ADBPurrytifyTheme {
        Surface {
            ProfileScreen(user)
        }
    }
}