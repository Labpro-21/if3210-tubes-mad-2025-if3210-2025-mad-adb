package com.example.adbpurrytify.ui.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyGray
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack

class SimpleLocationPickerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ADBPurrytifyTheme {
                var searchQuery by remember { mutableStateOf("") }
                var selectedLocation by remember { mutableStateOf("") }

                // Predefined list of countries with ISO codes
                val countries = listOf(
                    "AD" to "Andorra", "AE" to "United Arab Emirates", "AF" to "Afghanistan",
                    "AG" to "Antigua and Barbuda", "AI" to "Anguilla", "AL" to "Albania",
                    "AM" to "Armenia", "AO" to "Angola", "AQ" to "Antarctica", "AR" to "Argentina",
                    "AS" to "American Samoa", "AT" to "Austria", "AU" to "Australia", "AW" to "Aruba",
                    "AX" to "Åland Islands", "AZ" to "Azerbaijan", "BA" to "Bosnia and Herzegovina",
                    "BB" to "Barbados", "BD" to "Bangladesh", "BE" to "Belgium", "BF" to "Burkina Faso",
                    "BG" to "Bulgaria", "BH" to "Bahrain", "BI" to "Burundi", "BJ" to "Benin",
                    "BL" to "Saint Barthélemy", "BM" to "Bermuda", "BN" to "Brunei", "BO" to "Bolivia",
                    "BQ" to "Caribbean Netherlands", "BR" to "Brazil", "BS" to "Bahamas", "BT" to "Bhutan",
                    "BV" to "Bouvet Island", "BW" to "Botswana", "BY" to "Belarus", "BZ" to "Belize",
                    "CA" to "Canada", "CC" to "Cocos Islands", "CD" to "DR Congo", "CF" to "Central African Republic",
                    "CG" to "Republic of the Congo", "CH" to "Switzerland", "CI" to "Côte d'Ivoire",
                    "CK" to "Cook Islands", "CL" to "Chile", "CM" to "Cameroon", "CN" to "China",
                    "CO" to "Colombia", "CR" to "Costa Rica", "CU" to "Cuba", "CV" to "Cape Verde",
                    "CW" to "Curaçao", "CX" to "Christmas Island", "CY" to "Cyprus", "CZ" to "Czech Republic",
                    "DE" to "Germany", "DJ" to "Djibouti", "DK" to "Denmark", "DM" to "Dominica",
                    "DO" to "Dominican Republic", "DZ" to "Algeria", "EC" to "Ecuador", "EE" to "Estonia",
                    "EG" to "Egypt", "EH" to "Western Sahara", "ER" to "Eritrea", "ES" to "Spain",
                    "ET" to "Ethiopia", "FI" to "Finland", "FJ" to "Fiji", "FK" to "Falkland Islands",
                    "FM" to "Micronesia", "FO" to "Faroe Islands", "FR" to "France", "GA" to "Gabon",
                    "GB" to "United Kingdom", "GD" to "Grenada", "GE" to "Georgia", "GF" to "French Guiana",
                    "GG" to "Guernsey", "GH" to "Ghana", "GI" to "Gibraltar", "GL" to "Greenland",
                    "GM" to "Gambia", "GN" to "Guinea", "GP" to "Guadeloupe", "GQ" to "Equatorial Guinea",
                    "GR" to "Greece", "GS" to "South Georgia", "GT" to "Guatemala", "GU" to "Guam",
                    "GW" to "Guinea-Bissau", "GY" to "Guyana", "HK" to "Hong Kong", "HM" to "Heard Island",
                    "HN" to "Honduras", "HR" to "Croatia", "HT" to "Haiti", "HU" to "Hungary",
                    "ID" to "Indonesia", "IE" to "Ireland", "IL" to "Israel", "IM" to "Isle of Man",
                    "IN" to "India", "IO" to "British Indian Ocean Territory", "IQ" to "Iraq", "IR" to "Iran",
                    "IS" to "Iceland", "IT" to "Italy", "JE" to "Jersey", "JM" to "Jamaica",
                    "JO" to "Jordan", "JP" to "Japan", "KE" to "Kenya", "KG" to "Kyrgyzstan",
                    "KH" to "Cambodia", "KI" to "Kiribati", "KM" to "Comoros", "KN" to "Saint Kitts and Nevis",
                    "KP" to "North Korea", "KR" to "South Korea", "KW" to "Kuwait", "KY" to "Cayman Islands",
                    "KZ" to "Kazakhstan", "LA" to "Laos", "LB" to "Lebanon", "LC" to "Saint Lucia",
                    "LI" to "Liechtenstein", "LK" to "Sri Lanka", "LR" to "Liberia", "LS" to "Lesotho",
                    "LT" to "Lithuania", "LU" to "Luxembourg", "LV" to "Latvia", "LY" to "Libya",
                    "MA" to "Morocco", "MC" to "Monaco", "MD" to "Moldova", "ME" to "Montenegro",
                    "MF" to "Saint Martin", "MG" to "Madagascar", "MH" to "Marshall Islands", "MK" to "North Macedonia",
                    "ML" to "Mali", "MM" to "Myanmar", "MN" to "Mongolia", "MO" to "Macau",
                    "MP" to "Northern Mariana Islands", "MQ" to "Martinique", "MR" to "Mauritania", "MS" to "Montserrat",
                    "MT" to "Malta", "MU" to "Mauritius", "MV" to "Maldives", "MW" to "Malawi",
                    "MX" to "Mexico", "MY" to "Malaysia", "MZ" to "Mozambique", "NA" to "Namibia",
                    "NC" to "New Caledonia", "NE" to "Niger", "NF" to "Norfolk Island", "NG" to "Nigeria",
                    "NI" to "Nicaragua", "NL" to "Netherlands", "NO" to "Norway", "NP" to "Nepal",
                    "NR" to "Nauru", "NU" to "Niue", "NZ" to "New Zealand", "OM" to "Oman",
                    "PA" to "Panama", "PE" to "Peru", "PF" to "French Polynesia", "PG" to "Papua New Guinea",
                    "PH" to "Philippines", "PK" to "Pakistan", "PL" to "Poland", "PM" to "Saint Pierre and Miquelon",
                    "PN" to "Pitcairn Islands", "PR" to "Puerto Rico", "PS" to "Palestine", "PT" to "Portugal",
                    "PW" to "Palau", "PY" to "Paraguay", "QA" to "Qatar", "RE" to "Réunion",
                    "RO" to "Romania", "RS" to "Serbia", "RU" to "Russia", "RW" to "Rwanda",
                    "SA" to "Saudi Arabia", "SB" to "Solomon Islands", "SC" to "Seychelles", "SD" to "Sudan",
                    "SE" to "Sweden", "SG" to "Singapore", "SH" to "Saint Helena", "SI" to "Slovenia",
                    "SJ" to "Svalbard and Jan Mayen", "SK" to "Slovakia", "SL" to "Sierra Leone", "SM" to "San Marino",
                    "SN" to "Senegal", "SO" to "Somalia", "SR" to "Suriname", "SS" to "South Sudan",
                    "ST" to "São Tomé and Príncipe", "SV" to "El Salvador", "SX" to "Sint Maarten",
                    "SY" to "Syria", "SZ" to "Eswatini", "TC" to "Turks and Caicos Islands", "TD" to "Chad",
                    "TF" to "French Southern Territories", "TG" to "Togo", "TH" to "Thailand", "TJ" to "Tajikistan",
                    "TK" to "Tokelau", "TL" to "East Timor", "TM" to "Turkmenistan", "TN" to "Tunisia",
                    "TO" to "Tonga", "TR" to "Turkey", "TT" to "Trinidad and Tobago", "TV" to "Tuvalu",
                    "TW" to "Taiwan", "TZ" to "Tanzania", "UA" to "Ukraine", "UG" to "Uganda",
                    "UM" to "U.S. Minor Outlying Islands", "US" to "United States", "UY" to "Uruguay",
                    "UZ" to "Uzbekistan", "VA" to "Vatican City", "VC" to "Saint Vincent and the Grenadines",
                    "VE" to "Venezuela", "VG" to "British Virgin Islands", "VI" to "U.S. Virgin Islands",
                    "VN" to "Vietnam", "VU" to "Vanuatu", "WF" to "Wallis and Futuna", "WS" to "Samoa",
                    "YE" to "Yemen", "YT" to "Mayotte", "ZA" to "South Africa", "ZM" to "Zambia", "ZW" to "Zimbabwe"
                )

                // Filter countries based on search query
                val filteredCountries = if (searchQuery.isEmpty()) {
                    countries
                } else {
                    countries.filter {
                        it.second.contains(searchQuery, ignoreCase = true) ||
                                it.first.contains(searchQuery, ignoreCase = true)
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Select Location", color = Color.White) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    setResult(Activity.RESULT_CANCELED)
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                if (selectedLocation.isNotEmpty()) {
                                    IconButton(onClick = {
                                        val resultIntent = Intent().apply {
                                            putExtra("selected_location", selectedLocation)
                                        }
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        finish()
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
                    containerColor = BLACK_BACKGROUND
                ) { paddingValues ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    ) {
                        // Search field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search countries", color = SpotifyGray) },
                            placeholder = { Text("Type country name or code", color = SpotifyGray) },
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = SpotifyGray
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedLocation.isNotEmpty()) {
                            Text(
                                text = "Selected: $selectedLocation",
                                color = SpotifyGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Countries list
                        LazyColumn {
                            items(filteredCountries) { (code, name) ->
                                CountryItem(
                                    code = code,
                                    name = name,
                                    isSelected = selectedLocation == code,
                                    onSelect = { selectedLocation = code }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountryItem(
    code: String,
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SpotifyGreen.copy(alpha = 0.2f) else SpotifyLightBlack
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = code,
                    color = SpotifyGray,
                    fontSize = 14.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = SpotifyGreen
                )
            }
        }
    }
}