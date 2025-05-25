package com.example.adbpurrytify.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.SpotifyGray
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import com.example.adbpurrytify.ui.viewmodels.EditProfileViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State variables
    var location by remember { mutableStateOf("") }
    var isLocationLoading by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Camera-related state
    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Location client
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // UI state from ViewModel
    val uiState by viewModel.uiState
    val currentUser = (uiState as? EditProfileViewModel.EditProfileUiState.Success)?.user

    // Initialize location field with current user location
    LaunchedEffect(currentUser) {
        currentUser?.let {
            location = it.location
        }
    }

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation(fusedLocationClient, context) { newLocation ->
                location = newLocation
                isLocationLoading = false
            }
        } else {
            isLocationLoading = false
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Create local variable to avoid smart cast issue
            val cameraUri = tempCameraImageUri
            cameraUri?.let { uri ->
                profileImageUri = uri
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Create temp file and launch camera
            val tempFile = createTempImageFile(context)
            val cameraUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            tempCameraImageUri = cameraUri
            cameraLauncher.launch(cameraUri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Image pickers
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { profileImageUri = it }
    }



    // Custom location picker launcher
    val customLocationPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val selectedLocation = data?.getStringExtra("selected_location")
            selectedLocation?.let {
                location = it
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BLACK_BACKGROUND
                )
            )
        },
        containerColor = BLACK_BACKGROUND
    ) { paddingValues ->

        when (uiState) {
            is EditProfileViewModel.EditProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            }

            is EditProfileViewModel.EditProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as EditProfileViewModel.EditProfileUiState.Error).message,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                        ) {
                            Text("Retry", color = Color.Black)
                        }
                    }
                }
            }

            is EditProfileViewModel.EditProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Profile Picture Section
                    Card(
                        modifier = Modifier
                            .size(150.dp)
                            .clickable { showImageDialog = true },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = SpotifyLightBlack),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                SubcomposeAsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Selected Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else if ((uiState as EditProfileViewModel.EditProfileUiState.Success).user.image.isNotBlank()) {
                                val imageUrl = "http://34.101.226.132:3000/uploads/profile-picture/${(uiState as EditProfileViewModel.EditProfileUiState.Success).user.image}"
                                SubcomposeAsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Current Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = SpotifyGreen
                                        )
                                    },
                                    error = {
                                        Image(
                                            painter = painterResource(R.drawable.navbar_profile),
                                            contentDescription = "Default Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                )
                            } else {
                                Image(
                                    painter = painterResource(R.drawable.navbar_profile),
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Camera overlay icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(SpotifyGreen, CircleShape)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Photo",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username (Read-only)
                    Text(
                        text = (uiState as EditProfileViewModel.EditProfileUiState.Success).user.userName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = (uiState as EditProfileViewModel.EditProfileUiState.Success).user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SpotifyGray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Location Section
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location", color = SpotifyGray) },
                        placeholder = { Text("Enter your location", color = SpotifyGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SpotifyLightBlack,
                            focusedContainerColor = SpotifyLightBlack,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedBorderColor = SpotifyGray,
                            focusedBorderColor = SpotifyGreen,
                            unfocusedLabelColor = SpotifyGray,
                            focusedLabelColor = SpotifyGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = {
                                        isLocationLoading = true
                                        requestLocationPermission(
                                            context,
                                            locationPermissionLauncher,
                                            fusedLocationClient
                                        ) { newLocation ->
                                            location = newLocation
                                            isLocationLoading = false
                                        }
                                    }
                                ) {
                                    if (isLocationLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = SpotifyGreen,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "Get Current Location",
                                            tint = SpotifyGreen
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        // Launch map-based location picker
                                        val intent = Intent(context, MapLocationPickerActivity::class.java)
                                        customLocationPickerLauncher.launch(intent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pick Location on Map",
                                        tint = SpotifyGreen
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Save Button
                    Button(
                        onClick = {
                            scope.launch {
                                isUploading = true
                                val result = viewModel.updateProfile(
                                    location = location.takeIf { it.isNotBlank() },
                                    profileImageUri = profileImageUri,
                                    context = context
                                )
                                isUploading = false

                                when (result) {
                                    is EditProfileViewModel.ProfileUpdateResult.Success -> {
                                        Toast.makeText(
                                            context,
                                            "Profile updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    }
                                    is EditProfileViewModel.ProfileUpdateResult.Error -> {
                                        Toast.makeText(
                                            context,
                                            result.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // Stay on the edit profile screen so user can try again
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            disabledContainerColor = SpotifyGreen.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Updating...",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Save Changes",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Image Selection Dialog
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Change Profile Picture", color = Color.White) },
            text = { Text("Choose how you'd like to update your profile picture", color = SpotifyGray) },
            containerColor = SpotifyLightBlack,
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showImageDialog = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery", color = SpotifyGreen)
                    }

                    TextButton(
                        onClick = {
                            showImageDialog = false
                            // Check camera permission first
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val tempFile = createTempImageFile(context)
                                val cameraUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    tempFile
                                )
                                tempCameraImageUri = cameraUri
                                cameraLauncher.launch(cameraUri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Camera", color = SpotifyGreen)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImageDialog = false }
                ) {
                    Text("Cancel", color = SpotifyGray)
                }
            }
        )
    }

    // Location Selection Dialog
    if (showLocationDialog) {
        LocationSelectionDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { selectedLocation ->
                location = selectedLocation
                showLocationDialog = false
            },
            context = context,
            customLocationPickerLauncher = customLocationPickerLauncher
        )
    }
}

@Composable
fun LocationSelectionDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit,
    context: Context,
    customLocationPickerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location Method", color = Color.White) },
        text = { Text("Choose how you'd like to select your location", color = SpotifyGray) },
        containerColor = SpotifyLightBlack,
        confirmButton = {
            Column {
                // Manual Entry Button
                TextButton(
                    onClick = {
                        onDismiss()
                        // Focus will return to the text field for manual entry
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Manual Entry",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Manually", color = SpotifyGreen)
                }

                // Popular Locations Button
                TextButton(
                    onClick = {
                        onDismiss()
                        showCountrySelectionDialog(context, onLocationSelected)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Select Country",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Country", color = SpotifyGreen)
                }

                // Future: Custom Map Picker Button (you can implement later)
                // This is where you'd integrate a proper map picker library
                TextButton(
                    onClick = {
                        onDismiss()
                        Toast.makeText(context, "Map picker coming soon! Use manual entry for now.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Map Picker",
                        tint = SpotifyGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Map Picker (Coming Soon)", color = SpotifyGray)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SpotifyGray)
            }
        }
    )
}

fun showCountrySelectionDialog(context: Context, onLocationSelected: (String) -> Unit) {
    val countries = listOf(
        "ID" to "Indonesia",
        "US" to "United States",
        "GB" to "United Kingdom",
        "CA" to "Canada",
        "AU" to "Australia",
        "DE" to "Germany",
        "FR" to "France",
        "JP" to "Japan",
        "KR" to "South Korea",
        "MY" to "Malaysia",
        "SG" to "Singapore",
        "TH" to "Thailand",
        "VN" to "Vietnam",
        "PH" to "Philippines",
        "IN" to "India",
        "BR" to "Brazil",
        "MX" to "Mexico",
        "ES" to "Spain",
        "IT" to "Italy",
        "NL" to "Netherlands"
    )

    val items = countries.map { "${it.second} (${it.first})" }.toTypedArray()

    val builder = android.app.AlertDialog.Builder(context)
    builder.setTitle("Select Country")
        .setItems(items) { _, which ->
            onLocationSelected(countries[which].first)
        }
        .setNegativeButton("Cancel", null)
    builder.create().show()
}

// Helper Functions
private fun createTempImageFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = File(context.cacheDir, "images")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File(storageDir, "JPEG_${timestamp}_.jpg")
}

private fun requestLocationPermission(
    context: Context,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (String) -> Unit
) {
    val fineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarseLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

    if (fineLocationPermission == PackageManager.PERMISSION_GRANTED || coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
        getCurrentLocation(fusedLocationClient, context, onLocationReceived)
    } else {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationReceived: (String) -> Unit
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    getCountryFromLocation(context, it.latitude, it.longitude, onLocationReceived)
                } ?: run {
                    onLocationReceived("Location not available")
                }
            }
            .addOnFailureListener {
                onLocationReceived("Failed to get location")
            }
    } catch (e: SecurityException) {
        onLocationReceived("Location permission denied")
    }
}

private fun getCountryFromLocation(
    context: Context,
    latitude: Double,
    longitude: Double,
    onLocationReceived: (String) -> Unit
) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val countryCode = address.countryCode ?: ""
            Log.d("EditProfile", "Country code: $countryCode")
            onLocationReceived(countryCode)
        } else {
            onLocationReceived("Unknown location")
        }
    } catch (e: Exception) {
        Log.e("EditProfile", "Geocoding failed", e)
        onLocationReceived("Geocoding failed")
    }
}