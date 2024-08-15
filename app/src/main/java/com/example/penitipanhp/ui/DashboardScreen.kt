package com.example.penitipanhp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DashboardScreen(
    onScanQR: (String) -> Unit,
    status: String,
    onViewData: () -> Unit,
    onLogout: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { onScanQR("store") }) {
            Text("Scan QR for Storage")
        }

        Button(
            onClick = { onScanQR("retrieve") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Retrieve Phone")
        }

        Button(
            onClick = { onViewData() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("View Stored Data")
        }

        Button(
            onClick = { onLogout() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Logout")
        }

        Text(
            text = "Status: $status",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
