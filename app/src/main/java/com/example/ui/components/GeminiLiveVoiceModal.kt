package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.service.GeminiLiveAudioEngine
import com.example.viewmodel.LighthouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiLiveVoiceModal(
    viewModel: LighthouseViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun close() {
        viewModel.stopLiveVoiceSession()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = ::close,
        sheetState = sheetState,
        modifier = modifier.testTag("gemini_live_voice_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Voice companion preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = GeminiLiveAudioEngine.UNAVAILABLE_MESSAGE,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "This build does not request microphone access or send audio or transcripts.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = ::close) {
                Text("Close")
            }
        }
    }
}
