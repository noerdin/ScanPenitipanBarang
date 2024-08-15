package com.example.penitipanhp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class StoredItem(
    val name: String,
    val scanInDate: String,
    val scanOutDate: String? = null
)

@Composable
fun StoredDataScreen(onBack: () -> Unit) {
    val phoneDataList = remember { mutableStateListOf<StoredItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Mengambil data dari Google Apps Script
    LaunchedEffect(Unit) {
        val fetchedData = fetchStoredData()
        if (fetchedData != null) {
            phoneDataList.clear()
            phoneDataList.addAll(fetchedData)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back to Dashboard")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp)
        ) {
            Text(text = "No.", modifier = Modifier.weight(1f), fontSize = 16.sp)
            Text(text = "Nama", modifier = Modifier.weight(3f), fontSize = 16.sp)
            Text(text = "Scan In Date", modifier = Modifier.weight(3f), fontSize = 16.sp)
            Text(text = "Scan Out Date", modifier = Modifier.weight(3f), fontSize = 16.sp)
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            if (isLoading) {
                item { Text("Loading data...") }
            } else {
                val filteredList = if (searchQuery.isEmpty()) {
                    phoneDataList
                } else {
                    phoneDataList.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.scanInDate.contains(searchQuery, ignoreCase = true) ||
                                (it.scanOutDate?.contains(searchQuery, ignoreCase = true) ?: false)
                    }
                }

                if (filteredList.isEmpty()) {
                    item {
                        Text("No data available")
                    }
                } else {
                    itemsIndexed(filteredList) { index, phoneData ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = "${index + 1}", modifier = Modifier.weight(1f))
                            Text(text = phoneData.name, modifier = Modifier.weight(3f))
                            Text(text = phoneData.scanInDate, modifier = Modifier.weight(3f))
                            Text(
                                text = phoneData.scanOutDate ?: "Not Retrieved",
                                modifier = Modifier.weight(3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fungsi untuk mengambil data dari Google Apps Script
private suspend fun fetchStoredData(): List<StoredItem>? = withContext(Dispatchers.IO) {
    try {
        val url =
            URL("https://script.google.com/macros/s/AKfycbzmf6sUOuPETLqvi2uztOsKj_T2gDs1CCyjUizVaOFs0N9G5AawV4Gkhc9mwXmKJCykwA/exec")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val jsonResponse = connection.inputStream.bufferedReader().use { it.readText() }
            parseStoredData(jsonResponse)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Fungsi untuk parsing data JSON ke dalam bentuk list StoredItem
private fun parseStoredData(jsonResponse: String): List<StoredItem> {
    val storedItems = mutableListOf<StoredItem>()

    // Parsing sederhana (sesuaikan dengan format JSON sebenarnya)
    val jsonArray = JSONArray(jsonResponse)
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("name")
        val scanInDate = jsonObject.getString("scanInDate")
        val scanOutDate =
            if (jsonObject.has("scanOutDate") && jsonObject.getString("scanOutDate").isNotEmpty()) {
                jsonObject.getString("scanOutDate")
            } else {
                null
            }
        storedItems.add(StoredItem(name, scanInDate, scanOutDate))
    }

    return storedItems
}