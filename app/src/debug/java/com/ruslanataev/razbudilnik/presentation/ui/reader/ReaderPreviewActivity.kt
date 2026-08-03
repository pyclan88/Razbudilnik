package com.ruslanataev.razbudilnik.presentation.ui.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ruslanataev.razbudilnik.presentation.ui.theme.RazbudilnikTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReaderPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RazbudilnikTheme {
                ReaderRoute(onChallengeFinished = ::finish)
            }
        }
    }
}
