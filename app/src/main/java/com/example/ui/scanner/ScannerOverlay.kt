package com.example.ui.scanner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean,
    isScanningActive: Boolean,
    onToggleTorch: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleScanning: () -> Unit,
    onManualInputClick: () -> Unit,
    onSelectSampleBarcode: (barcode: String) -> Unit,
    sampleBarcodes: List<Pair<String, String>>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserAnimation")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserFloat"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val boxWidth = (screenWidth * 0.82f).coerceAtMost(360.dp)
        val boxHeight = (boxWidth * 0.65f).coerceAtLeast(180.dp)

        // Semi-transparent cutout overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val left = (canvasW - boxWidth.toPx()) / 2f
            val top = (canvasH - boxHeight.toPx()) / 2.4f
            val right = left + boxWidth.toPx()
            val bottom = top + boxHeight.toPx()

            // Dark semi-transparent background
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = size
            )

            // Transparent cut-out viewfinder
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(left, top, right, bottom),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                )
            }
            drawPath(
                path = cutoutPath,
                color = Color.Transparent,
                blendMode = BlendMode.Clear
            )

            // Viewfinder Corner Accents
            val cornerLen = 32.dp.toPx()
            val strokeWidth = 5.dp.toPx()
            val cornerColor = Color(0xFF00E5FF)

            // Top-Left Corner
            drawLine(cornerColor, Offset(left, top + cornerLen), Offset(left, top), strokeWidth)
            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth)

            // Top-Right Corner
            drawLine(cornerColor, Offset(right - cornerLen, top), Offset(right, top), strokeWidth)
            drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth)

            // Bottom-Left Corner
            drawLine(cornerColor, Offset(left, bottom - cornerLen), Offset(left, bottom), strokeWidth)
            drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth)

            // Bottom-Right Corner
            drawLine(cornerColor, Offset(right - cornerLen, bottom), Offset(right, bottom), strokeWidth)
            drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth)

            // Animated Laser Beam inside viewfinder when active
            if (isScanningActive) {
                val currentLaserY = top + (bottom - top) * laserPosition
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x0000E5FF),
                            Color(0xFF00E5FF),
                            Color(0xFF76FF03),
                            Color(0xFF00E5FF),
                            Color(0x0000E5FF)
                        )
                    ),
                    topLeft = Offset(left + 8.dp.toPx(), currentLaserY - 2.dp.toPx()),
                    size = Size(boxWidth.toPx() - 16.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // Top Status & Instructions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isScanningActive) Color(0xFF00E676) else Color(0xFFFF5252),
                                CircleShape
                            )
                    )
                    Text(
                        text = if (isScanningActive) "バーコードを枠内に合わせてください" else "スキャン一時停止中",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom Controls & Test Chips
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera control buttons (Flash, Pause/Play, Switch Camera, Manual Input)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flashlight toggle
                FilledIconButton(
                    onClick = onToggleTorch,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("toggle_torch_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isTorchEnabled) Color(0xFFFFD600) else Color.White.copy(alpha = 0.25f),
                        contentColor = if (isTorchEnabled) Color.Black else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "ライト切替"
                    )
                }

                // Pause / Play scan
                FilledIconButton(
                    onClick = onToggleScanning,
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("toggle_scan_active_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isScanningActive) MaterialTheme.colorScheme.primary else Color(0xFF455A64),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isScanningActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "スキャン停止・再開",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Switch Camera (Back / Front)
                FilledIconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("switch_camera_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "カメラ切替"
                    )
                }

                // Manual Input
                FilledIconButton(
                    onClick = onManualInputClick,
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("manual_input_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.25f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "手動入力"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick test barcodes for immediate emulator/demo testing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "テスト用サンプルバーコード（タップで即時検索）",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sampleBarcodes) { (barcode, label) ->
                        AssistChip(
                            onClick = { onSelectSampleBarcode(barcode) },
                            label = { Text(label, fontSize = 12.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.Black.copy(alpha = 0.65f),
                                labelColor = Color.White
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = Color(0xFF00E5FF).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("sample_barcode_${barcode}")
                        )
                    }
                }
            }
        }
    }
}
