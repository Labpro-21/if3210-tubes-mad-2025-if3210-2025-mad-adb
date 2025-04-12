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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.adbpurrytify.R
import com.example.adbpurrytify.api.LoginRequest
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.theme.TEXT_FIELD_BACKGROUND
import com.example.adbpurrytify.ui.theme.TEXT_FIELD_TEXT
import com.example.adbpurrytify.worker.JwtExpiryWorker
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
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
    isPassword: Boolean = false
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
                unfocusedContainerColor = TEXT_FIELD_BACKGROUND,
                focusedContainerColor = TEXT_FIELD_BACKGROUND,
                unfocusedTextColor = TEXT_FIELD_TEXT,
                focusedTextColor = TEXT_FIELD_TEXT,
                unfocusedBorderColor = TEXT_FIELD_BACKGROUND,
                focusedBorderColor = TEXT_FIELD_BACKGROUND
            ),
            singleLine = true,
            shape = RoundedCornerShape(4.dp),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green
        )
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Add loading state
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Get context

    LaunchedEffect(Unit) {
        TokenManager.initialize(context.applicationContext)
    }

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
                .padding(bottom = 48.dp),
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PurritifyTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Password",
                isPassword = true
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
                            val response = RetrofitClient.instance.login(
                                LoginRequest(email = email, password = password)
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val loginResponse = response.body()!!
                                TokenManager.saveAuthToken(loginResponse.accessToken)
                                TokenManager.saveRefreshToken(loginResponse.refreshToken)


                                // --- Schedule Background JWT Check ---
                                Log.i("LoginScreen", "Tokens saved successfully. Scheduling background worker.")
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

                                // --- Navigate to Home ---
                                // Only navigate if tokens were successfully saved (or if refresh token is optional)
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Handle unsuccessful login (e.g., wrong credentials)
                                val errorMsg = "Wrong Credentials"
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                Log.i("401", "Invalid credentials")
                            }


                        } catch (e: IOException) {
                            // Handle network errors
                            Log.e("LoginScreen", "Network error during login", e) // Log network error
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        } catch (e: HttpException) {
                            // Handle HTTP errors (non-2xx responses)
                            Log.e("LoginScreen", "HTTP error during login: ${e.code()}", e) // Log HTTP error
                            Toast.makeText(context, "HTTP error: ${e.message}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            // Handle other unexpected errors
                            Log.e("LoginScreen", "Unexpected error during login", e) // Log general error
                            Toast.makeText(context, "An unexpected error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            // This block always executes, regardless of success or failure
                            isLoading = false // Reset loading state
                        }
                    }
                },
                // Disable button while loading
                modifier = Modifier.then(if (isLoading) Modifier.alpha(0.6f) else Modifier)
            )
        }
    }
}

