package com.example.adbpurrytify.ui.screens

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class MapLocationPickerActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContent {
            ADBPurrytifyTheme {
                MapLocationPickerScreen(
                    onBackPressed = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onLocationSelected = { countryCode ->
                        val resultIntent = Intent().apply {
                            putExtra("selected_location", countryCode)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MapLocationPickerScreen(
        onBackPressed: () -> Unit,
        onLocationSelected: (String) -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
        var countryInfo by remember { mutableStateOf<CountryInfo?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Pick Location on Map", color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (countryInfo != null && countryInfo!!.isSupportedByApp) {
                            IconButton(onClick = {
                                countryInfo?.let { info ->
                                    onLocationSelected(info.mappedCountryCode)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirm",
                                    tint = SpotifyGreen
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BLACK_BACKGROUND
                    )
                )
            },
            containerColor = BLACK_BACKGROUND,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Center on world view
                        mapView.controller.setZoom(3.0)
                        mapView.controller.setCenter(GeoPoint(0.0, 0.0))
                    },
                    containerColor = SpotifyGreen
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "World View",
                        tint = Color.Black
                    )
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                // Map View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { context ->
                            MapView(context).apply {
                                mapView = this
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(3.0)
                                controller.setCenter(GeoPoint(0.0, 0.0))

                                // Set map click listener
                                setOnTouchListener { _, event ->
                                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                                        val projection = projection
                                        val geoPoint = projection.fromPixels(
                                            event.x.toInt(),
                                            event.y.toInt()
                                        ) as GeoPoint

                                        selectedLocation = geoPoint
                                        isLoading = true
                                        errorMessage = null

                                        // Clear previous markers
                                        overlays.clear()

                                        // Add marker at selected location
                                        val marker = Marker(this).apply {
                                            position = geoPoint
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            title = "Selected Location"
                                        }
                                        overlays.add(marker)
                                        invalidate()

                                        // Get country information
                                        scope.launch {
                                            val result = getCountryFromCoordinates(
                                                context,
                                                geoPoint.latitude,
                                                geoPoint.longitude
                                            )

                                            isLoading = false

                                            if (result.isSuccess) {
                                                countryInfo = result.getOrNull()
                                            } else {
                                                errorMessage = result.exceptionOrNull()?.message
                                                countryInfo = null
                                            }
                                        }
                                    }
                                    false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom info panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SpotifyLightBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tap on the map to select a location",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when {
                            isLoading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = SpotifyGreen,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Getting location info...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            errorMessage != null -> {
                                Text(
                                    text = "Error: $errorMessage",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }

                            countryInfo != null -> {
                                Column {
                                    Text(
                                        text = "Country: ${countryInfo!!.countryName}",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Original Code: ${countryInfo!!.countryCode}",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (countryInfo!!.isSupportedByApp) {
                                        Text(
                                            text = "✅ This country is supported by the app",
                                            color = SpotifyGreen,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "App Code: ${countryInfo!!.mappedCountryCode}",
                                            color = SpotifyGreen,
                                            fontSize = 12.sp
                                        )
                                    } else {
                                        Column {
                                            Text(
                                                text = "⚠️ This country is not supported by the server",
                                                color = Color.Yellow,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "Will use data from: ${countryInfo!!.mappedCountryCode} (${countryInfo!!.mappedCountryName})",
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "Note: Server only supports ID, MY, US, GB, CH, DE, BR",
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }

                                }
                            }

                            selectedLocation == null -> {
                                Text(
                                    text = "No location selected",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                if (::mapView.isInitialized) {
                    mapView.onDetach()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mapView.isInitialized) {
            mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::mapView.isInitialized) {
            mapView.onPause()
        }
    }
}

data class CountryInfo(
    val countryCode: String,
    val countryName: String,
    val isSupportedByApp: Boolean,
    val mappedCountryCode: String,
    val mappedCountryName: String
)

suspend fun getCountryFromCoordinates(
    context: android.content.Context,
    latitude: Double,
    longitude: Double
): Result<CountryInfo> = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val countryCode = address.countryCode ?: return@withContext Result.failure(
                Exception("Could not determine country code")
            )
            val countryName = address.countryName ?: "Unknown Country"

            Log.d("MapLocationPicker", "Found country: $countryName ($countryCode)")

            // Check if country is supported by app and map it
            val (mappedCode, mappedName, isSupported) = mapCountryToSupported(countryCode, countryName)

            val countryInfo = CountryInfo(
                countryCode = countryCode,
                countryName = countryName,
                isSupportedByApp = isSupported,
                mappedCountryCode = mappedCode,
                mappedCountryName = mappedName
            )

            Result.success(countryInfo)
        } else {
            Result.failure(Exception("No address found for these coordinates"))
        }
    } catch (e: Exception) {
        Log.e("MapLocationPicker", "Geocoding failed", e)
        Result.failure(e)
    }
}

/**
 * Maps country codes to supported ones and handles unsupported countries
 * Server only supports: ID, MY, US, GB, CH, DE, BR
 * Returns Triple<MappedCode, MappedName, IsOriginalSupported>
 */
fun mapCountryToSupported(countryCode: String, countryName: String): Triple<String, String, Boolean> {
    // List of countries actually supported by your server
    val supportedCountries = setOf("ID", "MY", "US", "GB", "CH", "DE", "BR")

    if (supportedCountries.contains(countryCode)) {
        return Triple(countryCode, countryName, true)
    }

    // Map unsupported countries to the closest supported ones
    val countryMappings = mapOf(
        // Southeast Asia -> Indonesia or Malaysia
        "SG" to "MY", "TH" to "MY", "VN" to "MY", "PH" to "MY", "BN" to "MY",
        "LA" to "MY", "KH" to "MY", "MM" to "MY", "TL" to "ID",

        // Asia -> closest regional power
        "IN" to "MY", "CN" to "MY", "JP" to "MY", "KR" to "MY", "HK" to "MY",
        "TW" to "MY", "MO" to "MY", "BD" to "MY", "LK" to "MY", "NP" to "MY",
        "BT" to "MY", "MV" to "MY", "AF" to "MY", "PK" to "MY", "MN" to "MY",
        "KZ" to "DE", "UZ" to "DE", "TJ" to "DE", "KG" to "DE", "TM" to "DE",

        // Europe -> Germany, Switzerland, or UK
        "FR" to "DE", "IT" to "DE", "ES" to "DE", "PT" to "DE", "NL" to "DE",
        "BE" to "DE", "AT" to "CH", "LU" to "DE", "LI" to "CH", "MC" to "DE",
        "SM" to "DE", "VA" to "DE", "AD" to "DE", "MT" to "DE", "CY" to "DE",
        "SE" to "DE", "NO" to "DE", "DK" to "DE", "FI" to "DE", "IS" to "GB",
        "IE" to "GB", "PL" to "DE", "CZ" to "DE", "SK" to "DE", "HU" to "DE",
        "RO" to "DE", "BG" to "DE", "HR" to "DE", "SI" to "DE", "EE" to "DE",
        "LV" to "DE", "LT" to "DE", "RS" to "DE", "BA" to "DE", "ME" to "DE",
        "MK" to "DE", "AL" to "DE", "MD" to "DE", "UA" to "DE", "BY" to "DE",
        "RU" to "DE",

        // Americas -> USA or Brazil
        "CA" to "US", "MX" to "US", "GT" to "US", "BZ" to "US", "SV" to "US",
        "HN" to "US", "NI" to "US", "CR" to "US", "PA" to "US", "CU" to "US",
        "JM" to "US", "HT" to "US", "DO" to "US", "PR" to "US", "TT" to "US",
        "BB" to "US", "LC" to "US", "GD" to "US", "VC" to "US", "AG" to "US",
        "KN" to "US", "DM" to "US", "BS" to "US", "VI" to "US", "GU" to "US",
        "AS" to "US", "MP" to "US", "UM" to "US",

        // South America -> Brazil
        "AR" to "BR", "CL" to "BR", "CO" to "BR", "PE" to "BR", "UY" to "BR",
        "PY" to "BR", "BO" to "BR", "EC" to "BR", "VE" to "BR", "GY" to "BR",
        "SR" to "BR", "GF" to "BR",

        // Oceania -> USA (as closest major English-speaking country)
        "AU" to "US", "NZ" to "US", "FJ" to "US", "PG" to "US", "NC" to "US",
        "VU" to "US", "SB" to "US", "TO" to "US", "WS" to "US", "KI" to "US",
        "TV" to "US", "NR" to "US", "PW" to "US", "FM" to "US", "MH" to "US",

        // Africa -> Germany (as major European hub) or USA
        "ZA" to "DE", "EG" to "DE", "NG" to "GB", "KE" to "GB", "GH" to "GB",
        "TZ" to "GB", "UG" to "GB", "ZM" to "GB", "ZW" to "GB", "MW" to "GB",
        "BW" to "GB", "LS" to "GB", "SZ" to "GB", "NA" to "GB", "MZ" to "GB",
        "MG" to "GB", "MU" to "GB", "SC" to "GB", "KM" to "GB", "DJ" to "GB",
        "ER" to "GB", "ET" to "GB", "SO" to "GB", "RW" to "GB", "BI" to "GB",
        "CD" to "GB", "CF" to "GB", "TD" to "GB", "CM" to "GB", "GQ" to "GB",
        "GA" to "GB", "ST" to "GB", "AO" to "GB", "CV" to "GB", "LY" to "DE",
        "TN" to "DE", "DZ" to "DE", "MA" to "DE", "SD" to "DE", "SS" to "DE",
        "NE" to "DE", "ML" to "DE", "BF" to "DE", "CI" to "DE", "TG" to "DE",
        "BJ" to "DE", "SN" to "DE", "GM" to "DE", "GW" to "DE", "GN" to "DE",
        "SL" to "GB", "LR" to "US",

        // Middle East -> Germany or UK
        "AE" to "DE", "SA" to "DE", "QA" to "DE", "KW" to "DE", "BH" to "DE",
        "OM" to "DE", "YE" to "DE", "JO" to "DE", "LB" to "DE", "SY" to "DE",
        "IQ" to "DE", "IR" to "DE", "IL" to "DE", "PS" to "DE", "TR" to "DE",
        "AM" to "DE", "AZ" to "DE", "GE" to "DE"
    )

    val mappedCode = countryMappings[countryCode] ?: "US" // Default fallback to US
    val mappedName = getCountryName(mappedCode)

    return Triple(mappedCode, mappedName, false)
}

fun getCountryName(countryCode: String): String {
    val countryNames = mapOf(
        "ID" to "Indonesia",
        "MY" to "Malaysia",
        "US" to "United States",
        "GB" to "United Kingdom",
        "CH" to "Switzerland",
        "DE" to "Germany",
        "BR" to "Brazil"
    )

    return countryNames[countryCode] ?: "Unknown Country"
}

