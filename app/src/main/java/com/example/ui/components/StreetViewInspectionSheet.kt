package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RouteData
import com.example.model.SamplePoint
import com.example.ui.theme.BorderCanvas
import com.example.ui.theme.DeepSlateDark
import com.example.ui.theme.DeepSlateText
import com.example.ui.theme.EmergencyRose
import com.example.ui.theme.MistBlue
import com.example.ui.theme.MonospaceDataSm
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.PureWhiteCard
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftSage
import com.example.ui.theme.Typography
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.WarmCloudBackground
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreetViewInspectionSheet(
    sample: SamplePoint,
    route: RouteData,
    onSelectSample: (SamplePoint) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var imageBitmap by remember(sample.panoId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(sample.panoId) {
        withContext(Dispatchers.IO) {
            try {
                context.assets.open("lighthouse/${sample.panoId}.jpg").use { stream ->
                    imageBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                imageBitmap = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmCloudBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SlateLight)
            )
        },
        modifier = modifier.testTag("street_view_inspection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PrimaryActionBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PureWhiteCard,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "STREET VIEW AUDIT",
                            style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = PrimaryActionBlue
                        )
                        Text(
                            text = "${route.via} • Pano ${sample.panoId.take(8)}...",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepSlateText
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DeepSlateText
                    )
                }
            }

            // Street View Photo Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSlateDark)
                    .border(1.dp, BorderCanvas, RoundedCornerShape(16.dp))
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!.asImageBitmap(),
                        contentDescription = "Street View photo at ${sample.lat}, ${sample.lng}",
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Street View Photo (640x400)",
                            style = MonospaceDataSm,
                            color = SlateLight
                        )
                    }
                }

                // Metadata overlay pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Date: ${sample.imageDate ?: "2025-03"}",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = PureWhiteCard
                        )
                        Text(text = "•", color = SlateLight)
                        Text(
                            text = "Gemini Conf: ${(sample.score?.confidence?.times(100))?.toInt() ?: 90}%",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = VerifiedGreen
                        )
                    }
                }
            }

            // Gemini Factual Assessment Note
            sample.score?.let { score ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhiteCard)
                        .border(1.dp, BorderCanvas, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryActionBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "GEMINI STREET ASSESSMENT",
                                style = MonospaceDataSm.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = PrimaryActionBlue
                            )
                        }
                        Text(
                            text = "\"${score.note}\"",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = DeepSlateText
                        )
                    }
                }

                // 5 Feature Scores Grid
                Text(
                    text = "WALK COMFORT SCORES (0-2)",
                    style = MonospaceDataSm.copy(fontSize = 10.sp),
                    color = SlateMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ScorePill(
                        label = "Shade",
                        score = score.treeShade,
                        icon = Icons.Default.Park,
                        modifier = Modifier.weight(1f)
                    )
                    ScorePill(
                        label = "Lighting",
                        score = score.streetlights,
                        icon = Icons.Default.FlashOn,
                        modifier = Modifier.weight(1f)
                    )
                    ScorePill(
                        label = "Sidewalk",
                        score = score.sidewalk,
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    ScorePill(
                        label = "Shops",
                        score = score.activeFrontage,
                        icon = Icons.Default.Storefront,
                        modifier = Modifier.weight(1f)
                    )
                    ScorePill(
                        label = "Clear",
                        score = 2 - score.obstructions,
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Route Filmstrip (Browse other sample points along this route)
            Text(
                text = "ROUTE STROLL FILMSTRIP (${route.samples.size} SAMPLES)",
                style = MonospaceDataSm.copy(fontSize = 10.sp),
                color = SlateMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                route.samples.forEachIndexed { index, pt ->
                    val isSelected = pt.panoId == sample.panoId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) PrimaryActionBlue else PureWhiteCard)
                            .border(1.dp, if (isSelected) PrimaryActionBlue else BorderCanvas, RoundedCornerShape(10.dp))
                            .clickable { onSelectSample(pt) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#${index + 1} (${pt.panoId.take(4)})",
                            style = MonospaceDataSm.copy(fontSize = 10.sp),
                            color = if (isSelected) PureWhiteCard else DeepSlateText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorePill(
    label: String,
    score: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val (scoreColor, scoreText) = when (score) {
        2 -> Pair(VerifiedGreen, "Good (2)")
        1 -> Pair(WarningAmber, "Mod (1)")
        else -> Pair(EmergencyRose, "Low (0)")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(PureWhiteCard)
            .border(1.dp, BorderCanvas, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scoreColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MonospaceDataSm.copy(fontSize = 9.sp),
                color = SlateMuted
            )
            Text(
                text = "$score/2",
                style = MonospaceDataSm.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = scoreColor
            )
        }
    }
}
