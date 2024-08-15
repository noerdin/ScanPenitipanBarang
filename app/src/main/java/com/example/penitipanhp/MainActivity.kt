package com.example.penitipanhp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.penitipanhp.ui.DashboardScreen
import com.example.penitipanhp.ui.LoginScreen
import com.example.penitipanhp.ui.StoredDataScreen
import com.example.penitipanhp.ui.theme.PenitipanHpTheme
import com.google.zxing.integration.android.IntentIntegrator
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// Enum class to define different screens
enum class Screen {
    Dashboard,
    StoredData
}

class MainActivity : ComponentActivity() {
    private var currentMode: String = "store"
    private val status = mutableStateOf("Ready")  // Simpan status di sini
    private var currentScreen by mutableStateOf(Screen.Dashboard)  // Deklarasikan currentScreen di sini


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PenitipanHpTheme {
                MainScreen(
                    status = status,
                    currentScreen = currentScreen,
                    onScreenChange = { screen -> currentScreen = screen },
                    onScanQrCode = { mode ->
                        currentMode = mode
                        startQrCodeScanner(this@MainActivity)
                    },
                    onLogout = {
                        // Proses logout jika diperlukan
                    }
                )
            }
        }
    }

    @Composable
    fun MainScreen(
        status: MutableState<String>,
        currentScreen: Screen,
        onScreenChange: (Screen) -> Unit,
        onScanQrCode: (String) -> Unit, // Callback untuk memulai QR code scanner
        onLogout: () -> Unit // Callback untuk logout
    ) {
        var loggedIn by remember { mutableStateOf(false) }

        if (loggedIn) {
            when (currentScreen) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        onScanQR = onScanQrCode,
                        status = status.value,
                        onViewData = { onScreenChange(Screen.StoredData) },
                        onLogout = {
                            loggedIn = false
                            onLogout()
                        }
                    )
                }
                Screen.StoredData -> {
                    StoredDataScreen(onBack = { onScreenChange(Screen.Dashboard) })
                }
            }
        } else {
            LoginScreen(onLoginSuccess = { loggedIn = true })
        }
    }

    private fun startQrCodeScanner(context: Context) {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR code")
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                val qrCode = result.contents
                handleQrCodeResult(qrCode)
            } else {
                Log.e("QR Code", "Failed to scan QR code")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQrCodeResult(qrCode: String) {
        // Format tanggal untuk scan in dan scan out
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // Data yang akan dikirim ke Google Spreadsheet
        val phoneData = mapOf(
            "name" to qrCode, // Anggap QR code berisi nama orang
            "scanInDate" to if (currentMode == "store") currentDate else null,
            "scanOutDate" to if (currentMode == "retrieve") currentDate else null
        )

        // URL Web App dari Google Apps Script
        val webAppUrl = "https://script.google.com/macros/s/AKfycbzmf6sUOuPETLqvi2uztOsKj_T2gDs1CCyjUizVaOFs0N9G5AawV4Gkhc9mwXmKJCykwA/exec" // Ganti dengan URL dari Google Apps Script

        // Kirim data ke Google Apps Script
        sendDataToWebApp(webAppUrl, phoneData)
    }

    private fun sendDataToWebApp(url: String, data: Map<String, String?>) {
        Thread {
            try {
                val jsonInputString = data.map { "\"${it.key}\": \"${it.value ?: ""}\"" }
                    .joinToString(prefix = "{", postfix = "}")

                val urlObj = URL(url)
                val conn = urlObj.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true

                OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(jsonInputString)
                    writer.flush()
                }

                conn.inputStream.bufferedReader().use {
                    it.lines().forEach { line -> Log.d("Response", line) }
                }

                status.value = "Data sent successfully!"
            } catch (e: Exception) {
                Log.e("Error", "Failed to send data: ${e.message}")
                status.value = "Failed to send data"
            }
        }.start()
    }
}
