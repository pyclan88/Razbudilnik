package com.ruslanataev.razbudilnik.presentation.ui.setup

import android.content.Intent
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ruslanataev.razbudilnik.presentation.ui.reader.ReaderPreviewActivity

@Composable
internal fun BuildVariantSetupControls() {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            val intent = Intent(context, ReaderPreviewActivity::class.java)
            context.startActivity(intent)
        },
    ) {
        Text("DEBUG: Open reader")
    }
}
