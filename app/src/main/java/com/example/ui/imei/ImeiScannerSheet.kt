package com.example.ui.imei

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ImeiInfo
import com.example.ui.scanner.CameraPreviewView
import com.example.util.SoundVibratorHelper

@Composable
fun ImeiScannerSheet(
    onDismiss: () -> Unit,
    onImeiScanned: (imei: String) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onDismiss() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isTorchEnabled by remember { mutableStateOf(false) }
    var isScanningActive by remember { mutableStateOf(true) }
    var lastScannedImei by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val sampleImeis = remember {
        listOf(
            "iPhone 15" to "358432109876543",
            "Pixel 8" to "015893001234567",
            "Galaxy S24" to "355812009876543",
            "Xperia 1" to "354921008765432"
        )
    }

    fun handleImeiDetection(rawText: String) {
        val extracted = ImeiInfo.extractImeiFromScannedText(rawText)
        if (extracted != null && isScanningActive) {
            isScanningActive = false
            lastScannedImei = extracted
            SoundVibratorHelper.beep(context)
            SoundVibratorHelper.vibrate(context, 100)
            onImeiScanned(extracted)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("imei_scanner_sheet")
    ) {
        if (hasCameraPermission) {
            CameraPreviewView(
                modifier = Modifier.fillMaxSize(),
                isTorchEnabled = isTorchEnabled,
                useFrontCamera = false,
                isScanningActive = isScanningActive,
                onBarcodeScanned = { barcode, _ ->
                    handleImeiDetection(barcode)
                }
            )

            // Custom Viewfinder Overlay for IMEI barcodes
            ImeiViewfinderOverlay(
                modifier = Modifier.fillMaxSize(),
                isTorchEnabled = isTorchEnabled,
                onToggleTorch = { isTorchEnabled = !isTorchEnabled },
                onClose = onDismiss,
                onSelectSample = { sample ->
                    handleImeiDetection(sample)
                },
                sampleImeis = sampleImeis
            )
        } else {
            // Permission Request Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "カメラへのアクセス権限が必要です",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "スマホ外箱や本体シールのIMEIバーコード（Code-128等）をスキャンするためにカメラを使用します。",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("カメラ権限を許可する")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("閉じる")
                }
            }
        }
    }
}

@Composable
fun ImeiViewfinderOverlay(
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onClose: () -> Unit,
    onSelectSample: (String) -> Unit,
    sampleImeis: List<Pair<String, String>>
) {
    val laserTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by laserTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    BoxWithConstraints(modifier = modifier) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // A wider rectangular reticle tailored for long 15-digit barcode strips on phone boxes
        val reticleWidth = (screenWidth * 0.88f).coerceAtMost(360.dp)
        val reticleHeight = 160.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val rectW = reticleWidth.toPx()
            val rectH = reticleHeight.toPx()
            val left = (canvasW - rectW) / 2f
            val top = (canvasH - rectH) / 2f - 40.dp.toPx()

            // Dim darkened background
            drawRect(
                color = Color(0x99000000),
                size = size
            )

            // Transparent viewfinder window
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // Outer border
            drawRoundRect(
                color = Color(0x6600E5FF),
                topLeft = Offset(left, top),
                size = Size(rectW, rectH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Corner brackets
            val cornerLen = 28.dp.toPx()
            val cornerStroke = 4.dp.toPx()
            val cornerColor = Color(0xFF00E5FF)

            // Top-Left
            drawLine(cornerColor, Offset(left - 2, top), Offset(left + cornerLen, top), cornerStroke)
            drawLine(cornerColor, Offset(left, top - 2), Offset(left, top + cornerLen), cornerStroke)

            // Top-Right
            drawLine(cornerColor, Offset(left + rectW - cornerLen, top), Offset(left + rectW + 2, top), cornerStroke)
            drawLine(cornerColor, Offset(left + rectW, top - 2), Offset(left + rectW, top + cornerLen), cornerStroke)

            // Bottom-Left
            drawLine(cornerColor, Offset(left - 2, top + rectH), Offset(left + cornerLen, top + rectH), cornerStroke)
            drawLine(cornerColor, Offset(left, top + rectH - cornerLen), Offset(left, top + rectH + 2), cornerStroke)

            // Bottom-Right
            drawLine(cornerColor, Offset(left + rectW - cornerLen, top + rectH), Offset(left + rectW + 2, top + rectH), cornerStroke)
            drawLine(cornerColor, Offset(left + rectW, top + rectH - cornerLen), Offset(left + rectW, top + rectH + 2), cornerStroke)

            // Laser line
            val laserY = top + (rectH * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00E5FF),
                        Color.White,
                        Color(0xFF00E5FF),
                        Color.Transparent
                    )
                ),
                start = Offset(left + 8.dp.toPx(), laserY),
                end = Offset(left + rectW - 8.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(Color(0x66000000), CircleShape)
                    .testTag("imei_scanner_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "閉じる",
                    tint = Color.White
                )
            }

            Surface(
                color = Color(0x99000000),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IMEI専用スキャナー",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier
                    .background(Color(0x66000000), CircleShape)
                    .testTag("imei_scanner_torch_button")
            ) {
                Icon(
                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "フラッシュ",
                    tint = if (isTorchEnabled) Color(0xFFFFD54F) else Color.White
                )
            }
        }

        // Instruction Guide above/below the viewfinder
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xCC1E293B),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "箱や端末シールのバーコード（IMEI 15桁）を枠内に",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "読み取り完了時、IMEI貼り付け欄に自動入力されます",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Sample Quick Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "テスト用サンプルIMEI（タップで即入力）:",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(sampleImeis) { (label, imei) ->
                    AssistChip(
                        onClick = { onSelectSample(imei) },
                        label = { Text(label, color = Color.White, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0x881E293B)
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color(0x4400E5FF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("sample_imei_scanner_${label.lowercase()}")
                    )
                }
            }
        }
    }
}
