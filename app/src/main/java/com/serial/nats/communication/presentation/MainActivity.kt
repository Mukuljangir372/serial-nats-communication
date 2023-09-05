package com.serial.nats.communication.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.serial.nats.communication.core.theme.SerialNatsCommunicationTheme
import com.serial.nats.communication.presentation.device.DeviceConnectionScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SerialNatsCommunicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    val controller = rememberNavController()
    NavHost(navController = controller, startDestination = DEVICE_SCREEN) {
        composable(DEVICE_SCREEN) {
            DeviceConnectionScreen()
        }
    }
}

private const val DEVICE_SCREEN = "DEVICE_SCREEN"