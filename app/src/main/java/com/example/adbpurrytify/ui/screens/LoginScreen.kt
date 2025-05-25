package com.example.adbpurrytify.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import com.example.adbpurrytify.ui.viewmodels.AuthViewModel
import com.example.adbpurrytify.worker.JwtExpiryWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@Composable
fun PurritifyLogo(scaleTo: Float) {
    Image(
        painter = painterResource(id = R.drawable.logo_purrytify),
        contentDescription = "Purritify Logo",
        modifier = Modifier.scale(scaleTo)
    )
}

@Composable
fun PurritifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    imeAction: ImeAction
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = SpotifyLightBlack,
                focusedContainerColor = SpotifyLightBlack,
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = SpotifyLightBlack,
                focusedBorderColor = SpotifyGreen
            ),
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
            ),
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description, tint = Color.Gray)
                    }
                }
            }
        )
    }
}

@Composable
fun PurritifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(50), // Keep the pill shape
        colors = ButtonDefaults.buttonColors(
            containerColor = SpotifyGreen,
            disabledContainerColor = SpotifyGreen.copy(alpha = 0.6f)
        ),
        enabled = enabled
    ) {
        Text(
            text = text.uppercase(), // Spotify often uses uppercase
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK_BACKGROUND)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = "Login Background",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp)
                .verticalScroll(rememberScrollState()), // How nice, to have this thing existed
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PurritifyLogo(
                scaleTo = 0.8f
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Millions of Songs.\nOnly on Purritify.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            PurritifyTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Email",
                modifier = Modifier.padding(bottom = 16.dp),
                imeAction = ImeAction.Next
            )

            PurritifyTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Password",
                isPassword = true,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            PurritifyButton(
                text = if (isLoading) "Logging In..." else "Log In",
                onClick = {
                    if (isLoading) return@PurritifyButton

                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                        return@PurritifyButton
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val result = authViewModel.login(email, password)
                            if (result.isSuccess) {
                                // Schedule Background JWT Check
                                Log.i("LoginScreen", "Login successful. Scheduling background worker.")
                                val workManager = WorkManager.getInstance(context.applicationContext)
                                val firstWork = OneTimeWorkRequestBuilder<JwtExpiryWorker>()
                                    .setInitialDelay(0, TimeUnit.MINUTES)
                                    .setConstraints(
                                        Constraints.Builder()
                                            .setRequiredNetworkType(NetworkType.CONNECTED)
                                            .build()
                                    )
                                    .build()

                                workManager.enqueue(firstWork)

                                // Navigate to Home
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, result.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Error during login", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                // Disable button while loading
                modifier = Modifier.then(if (isLoading) Modifier.alpha(0.6f) else Modifier),
                enabled = !isLoading
            )
        }
    }
}