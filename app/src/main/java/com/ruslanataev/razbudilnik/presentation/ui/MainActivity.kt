package com.ruslanataev.razbudilnik.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ruslanataev.razbudilnik.presentation.ui.reader.ReaderRoute
import com.ruslanataev.razbudilnik.presentation.ui.setup.SetupRoute
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RazbudilnikTheme {
                var isReaderVisible by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isReaderVisible) {
                        ReaderRoute(
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        SetupRoute(
                            onOpenReaderClick = {
                                isReaderVisible = true
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
