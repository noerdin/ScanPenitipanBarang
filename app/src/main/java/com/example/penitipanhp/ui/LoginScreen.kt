package com.example.penitipanhp.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) } // Menambahkan state untuk registrasi
    val snackbarHostState = remember { SnackbarHostState() } // State untuk Snackbar
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    val auth = FirebaseAuth.getInstance()  // Inisialisasi auth di sini
                    if (isRegistering) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess()
                                } else {
                                    val errorMessage = task.exception?.message
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            errorMessage ?: "Registration failed"
                                        )
                                    }
                                    Log.e(
                                        "Register",
                                        "Registration failed: ${task.exception?.message}"
                                    )
                                }
                            }
                    } else {
                        // Logika untuk login
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess()
                                } else {
                                    val errorMessage = task.exception?.message
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            errorMessage ?: "Login failed"
                                        )
                                    }
                                        Log.e("Login", "Login failed: ${task.exception?.message}")
                                    }
                                }
                            }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please enter email and password")
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
                ) {
                Text(if (isRegistering) "Register" else "Login")
            }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isRegistering) "Already have an account? Login here" else "Don't have an account? Register here",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            isRegistering = !isRegistering
                        }
                )

                // Menampilkan Snackbar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

            }
    }


