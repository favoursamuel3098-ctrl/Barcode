package com.offlineqr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.offlineqr.app.ui.generate.GenerateScreen
import com.offlineqr.app.ui.map.MapScreen
import com.offlineqr.app.ui.scan.ScanScreen
import com.offlineqr.app.ui.theme.OfflineQRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstancee: Bundle?) {
        super.onCreate(savedInstancee)
        enableEdgeToEdge()
        setContent {
            OfflineQRTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                var mapLat by remember { mutableStateOf<Double?>(null) }
                var mapLng by remember { mutableStateOf<Double?>(null) }

                if (mapLat != null && mapLng != null) {
                    MapScreen(
                        latitude = mapLat!!,
                        longitude = mapLng!!,
                        onBack = {
                            mapLat = null
                            mapLng = null
                        }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                                    label = { Text("Generate") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                                    label = { Text("Scan") }
                                )
                            }
                        }
                    ) {
                        when (selectedTab) {
                            0 -> GenerateScreen(
                                onShowMap = { lat, lng ->
                                    mapLat = lat
                                    mapLng = lng
                                }
                            )
                            1 -> ScanScreen(
                                onShowMap = { lat, lng ->
                                    mapLat = lat
                                    mapLng = lng
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
