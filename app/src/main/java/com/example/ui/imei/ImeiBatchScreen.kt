package com.example.ui.imei

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImeiInfo
import java.net.URLEncoder

@Composable
fun ImeiBatchScreen(
    onOpenUrl: (url: String) -> Unit,
    onOpenInAppBrowser: (url: String, title: String) -> Unit,
    onNavigateToContinuousScanner: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val imeiList = remember { mutableStateListOf<ImeiInfo>() }

    // Dialog state for viewing all official carrier URLs
    var showCarrierUrlsDialog by remember { mutableStateOf(false) }

    // Dedicated IMEI Scanner Sheet state
    var showImeiScanner by remember { mutableStateOf(false) }

    // Model name price search dialog state
    var modelSearchDialogInitialText by remember { mutableStateOf<String?>(null) }

    fun addImeisFromText(text: String) {
        val lines = text.split("\n", ",", " ", "\t")
        for (line in lines) {
            val digits = line.filter { it.isDigit() }.trim()
            if (digits.length in 14..16) {
                if (imeiList.none { it.imei == digits }) {
                    imeiList.add(0, ImeiInfo.parse(digits))
                }
            }
        }
    }

    fun copyImeiAndOpen(imei: String, url: String, carrierName: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("IMEI", imei))
        Toast.makeText(
            context,
            "IMEI「$imei」をコピーしました！\n${carrierName}の入力欄に貼り付けてください",
            Toast.LENGTH_LONG
        ).show()
        onOpenUrl(url)
    }

    fun copyText(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label をコピーしました", Toast.LENGTH_SHORT).show()
    }

    // When IMEI Scanner is opened
    if (showImeiScanner) {
        ImeiScannerSheet(
            onDismiss = { showImeiScanner = false },
            onImeiScanned = { scannedImei ->
                // Automatically set into the input text field
                inputText = scannedImei
                // Also add to the list so user can immediately query
                if (imeiList.none { it.imei == scannedImei }) {
                    imeiList.add(0, ImeiInfo.parse(scannedImei))
                }
                showImeiScanner = false
                Toast.makeText(
                    context,
                    "IMEI「$scannedImei」をスキャンし、入力欄に自動入力しました！",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Input & Actions Section
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "IMEI (製造番号) 照会・判定",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "利用制限判定（○△×）＆ Snowy Skies一括確認",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (imeiList.isNotEmpty()) {
                        IconButton(
                            onClick = { imeiList.clear() },
                            modifier = Modifier.testTag("clear_all_imei_button")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "一括クリア")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Multi-line Input Field (IMEI貼り付け欄)
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("IMEI貼り付け欄") },
                    placeholder = { Text("15桁のIMEI番号を入力または貼り付け\nスキャナーで読み取るとここに自動入力されます") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("imei_batch_input_field"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "クリア")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Action Row: IMEI Scanner + List Add + Clipboard Paste
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dedicated IMEI Camera Scanner Button
                    Button(
                        onClick = { showImeiScanner = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("open_imei_scanner_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("IMEIスキャン", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                addImeisFromText(inputText)
                                inputText = ""
                                Toast.makeText(context, "IMEIをリストに追加しました", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "IMEIを入力またはスキャンしてください", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_imei_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("追加", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotBlank()) {
                                val extracted = ImeiInfo.extractImeiFromScannedText(clipText) ?: clipText.filter { it.isDigit() }
                                if (extracted.isNotBlank()) {
                                    inputText = extracted
                                    addImeisFromText(extracted)
                                    Toast.makeText(context, "IMEI「$extracted」を貼り付け＆追加しました", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "クリップボードに有効なIMEIがありません", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "クリップボードが空です", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("paste_imei_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("貼付", fontSize = 13.sp)
                    }
                }

                // Sample IMEIs for instant test
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "サンプル: ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val samples = listOf(
                            "iPhone" to "358432109876543",
                            "Pixel" to "015893001234567",
                            "Galaxy" to "355812009876543",
                            "Xperia" to "354921008765432"
                        )
                        items(samples) { (label, sampleImei) ->
                            AssistChip(
                                onClick = {
                                    inputText = sampleImei
                                    if (imeiList.none { it.imei == sampleImei }) {
                                        imeiList.add(0, ImeiInfo.parse(sampleImei))
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("sample_imei_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Important Guidance Banner about IMEI vs Buyback Price
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "IMEI（製造番号）は利用制限判定用です",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "端末の個別シリアル番号のため、買取店やメルカリの相場検索ではIMEIではなく『iPhone 15』等の機種名で査定されます。",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4-Carrier Batch Checker (Snowy Skies) - Highlight Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Section Header: Snowy Skies Batch Checker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "一括判定",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "4キャリア一括判定 (Snowy Skies)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = { showCarrierUrlsDialog = true },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("全URL一覧", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Snowy Skies URL row
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ImeiInfo.MULTI_CHECKER_URL,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { copyText(ImeiInfo.MULTI_CHECKER_URL, "一括判定URL") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "URLコピー",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons for Snowy Skies
                val activeImei = imeiList.firstOrNull()?.imei ?: inputText.filter { it.isDigit() }.takeIf { it.length in 14..16 }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!activeImei.isNullOrBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("IMEI", activeImei))
                                Toast.makeText(
                                    context,
                                    "IMEI「$activeImei」をコピーしました！\nSnowy Skiesの入力欄に貼り付けてください",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            onOpenUrl(ImeiInfo.MULTI_CHECKER_URL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("open_multi_carrier_checker_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (activeImei != null) "Snowy Skiesで一括判定（番号コピー済）" else "Snowy Skiesを開く",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onOpenInAppBrowser(ImeiInfo.MULTI_CHECKER_URL, "4キャリア一括判定 (Snowy Skies)")
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("open_multi_carrier_inapp_button")
                    ) {
                        Text("アプリ内表示", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Carrier Direct Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "各社公式窓口（直通）:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "タップで番号コピー＆直通",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CarrierButton(name = "docomo", color = Color(0xFFCC0000), modifier = Modifier.weight(1f)) {
                        if (activeImei != null) copyImeiAndOpen(activeImei, ImeiInfo.DOCOMO_URL, "docomo")
                        else onOpenUrl(ImeiInfo.DOCOMO_URL)
                    }
                    CarrierButton(name = "au", color = Color(0xFFFF6600), modifier = Modifier.weight(1f)) {
                        if (activeImei != null) copyImeiAndOpen(activeImei, ImeiInfo.AU_URL, "au")
                        else onOpenUrl(ImeiInfo.AU_URL)
                    }
                    CarrierButton(name = "SoftBank", color = Color(0xFF757575), modifier = Modifier.weight(1f)) {
                        if (activeImei != null) copyImeiAndOpen(activeImei, ImeiInfo.SOFTBANK_URL, "SoftBank")
                        else onOpenUrl(ImeiInfo.SOFTBANK_URL)
                    }
                    CarrierButton(name = "楽天", color = Color(0xFFBF0000), modifier = Modifier.weight(1f)) {
                        if (activeImei != null) copyImeiAndOpen(activeImei, ImeiInfo.RAKUTEN_URL, "楽天モバイル")
                        else onOpenUrl(ImeiInfo.RAKUTEN_URL)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // List Header with count & batch copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "スキャン・登録済みIMEI (${imeiList.size}件)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (imeiList.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val text = imeiList.joinToString("\n") { it.imei }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("IMEI一覧", text))
                        Toast.makeText(context, "${imeiList.size}件のIMEIを一括コピーしました", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_all_imeis_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全件コピー", fontSize = 12.sp)
                }
            }
        }

        // Empty State or List of IMEIs
        if (imeiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "登録されたIMEIはありません",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "上の「IMEIスキャン」ボタンでバーコードを読むか、\nIMEI番号を入力・貼り付けしてください",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showImeiScanner = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("カメラでIMEIをスキャン")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(imeiList, key = { it.imei }) { item ->
                    ImeiCard(
                        item = item,
                        onDelete = { imeiList.remove(item) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("IMEI", item.imei))
                            Toast.makeText(context, "IMEI「${item.imei}」をコピーしました", Toast.LENGTH_SHORT).show()
                        },
                        onCarrierClick = { carrierUrl, carrierName ->
                            copyImeiAndOpen(item.imei, carrierUrl, carrierName)
                        },
                        onSearchModelPrice = { initialKeyword ->
                            modelSearchDialogInitialText = initialKeyword
                        }
                    )
                }
            }
        }
    }

    // Dialog showing full list of verified carrier check URLs
    if (showCarrierUrlsDialog) {
        CarrierUrlsListDialog(
            onDismiss = { showCarrierUrlsDialog = false },
            onOpenUrl = onOpenUrl,
            onCopyUrl = { url, name -> copyText(url, name) }
        )
    }

    // Dialog for searching market buyback price by phone model name
    modelSearchDialogInitialText?.let { initialModel ->
        ModelPriceSearchDialog(
            initialModelName = initialModel,
            onDismiss = { modelSearchDialogInitialText = null },
            onOpenUrl = onOpenUrl
        )
    }
}

@Composable
fun CarrierButton(name: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ImeiCard(
    item: ImeiInfo,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onCarrierClick: (url: String, name: String) -> Unit,
    onSearchModelPrice: (initialKeyword: String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("imei_card_${item.imei}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: IMEI number + status badge + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.imei,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "コピー",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (item.isValidLength) Color(0xFF00897B).copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (item.isValidLength) "正規15桁" else "桁数不正 (${item.imei.length}桁)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isValidLength) Color(0xFF00897B) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!item.estimatedBrand.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.estimatedBrand,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Action 1: 4-Carrier batch checker for this card (Snowy Skies)
            Button(
                onClick = {
                    onCarrierClick(ImeiInfo.MULTI_CHECKER_URL, "4キャリア一括判定 (Snowy Skies)")
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("card_snowy_skies_button_${item.imei}"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Snowy Skiesで4社一括判定（番号コピー済）", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 2: Individual carrier checks
            Text(
                text = "各社直接判定（番号コピー＆窓口起動）:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CarrierCheckChip(label = "docomo", color = Color(0xFFCC0000), modifier = Modifier.weight(1f)) {
                    onCarrierClick(ImeiInfo.DOCOMO_URL, "docomo")
                }
                CarrierCheckChip(label = "au", color = Color(0xFFFF6600), modifier = Modifier.weight(1f)) {
                    onCarrierClick(ImeiInfo.AU_URL, "au")
                }
                CarrierCheckChip(label = "SoftBank", color = Color(0xFF757575), modifier = Modifier.weight(1f)) {
                    onCarrierClick(ImeiInfo.SOFTBANK_URL, "SoftBank")
                }
                CarrierCheckChip(label = "楽天", color = Color(0xFFBF0000), modifier = Modifier.weight(1f)) {
                    onCarrierClick(ImeiInfo.RAKUTEN_URL, "楽天モバイル")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action 3: Second-hand price by Phone Model Name (not raw IMEI)
            OutlinedButton(
                onClick = {
                    val defaultModel = when {
                        item.estimatedBrand?.contains("iPhone") == true -> "iPhone"
                        item.estimatedBrand?.contains("Pixel") == true -> "Pixel"
                        item.estimatedBrand?.contains("Galaxy") == true -> "Galaxy"
                        item.estimatedBrand?.contains("Xperia") == true -> "Xperia"
                        item.estimatedBrand?.contains("AQUOS") == true -> "AQUOS"
                        else -> "スマートフォン"
                    }
                    onSearchModelPrice(defaultModel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("機種名で買取相場を調べる (イオシス / メルカリ)", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun CarrierCheckChip(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        modifier = modifier.height(30.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun CarrierUrlsListDialog(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyUrl: (url: String, name: String) -> Unit
) {
    val carrierList = listOf(
        CarrierItem(
            name = "4社一括判定 (Snowy Skies)",
            note = "docomo・au・SoftBank・楽天を一括判定",
            url = ImeiInfo.MULTI_CHECKER_URL,
            color = Color(0xFF00897B)
        ),
        CarrierItem(
            name = "NTTドコモ (直接照会画面)",
            note = "公式 ネットワーク利用制限携帯電話機確認",
            url = ImeiInfo.DOCOMO_SEARCH_URL,
            color = Color(0xFFCC0000)
        ),
        CarrierItem(
            name = "NTTドコモ (トップ案内)",
            note = "利用制限の確認案内トップページ",
            url = ImeiInfo.DOCOMO_TOP_URL,
            color = Color(0xFFCC0000)
        ),
        CarrierItem(
            name = "ソフトバンク (公式確認窓口)",
            note = "SoftBank / Y!mobile ネットワーク利用制限確認",
            url = ImeiInfo.SOFTBANK_OFFICIAL_URL,
            color = Color(0xFF757575)
        ),
        CarrierItem(
            name = "ソフトバンク (直接照会)",
            note = "製造番号直接入力ページ",
            url = ImeiInfo.SOFTBANK_DIRECT_URL,
            color = Color(0xFF757575)
        ),
        CarrierItem(
            name = "au (KDDI)",
            note = "au ネットワーク利用制限携帯電話機照会",
            url = ImeiInfo.AU_URL,
            color = Color(0xFFFF6600)
        ),
        CarrierItem(
            name = "楽天モバイル",
            note = "楽天モバイル ネットワーク利用制限確認",
            url = ImeiInfo.RAKUTEN_URL,
            color = Color(0xFFBF0000)
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("判定先URL一覧 (全社確認済み)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(carrierList) { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.color
                                )
                                Row {
                                    IconButton(
                                        onClick = { onCopyUrl(item.url, item.name) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "URLコピー",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onOpenUrl(item.url) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.OpenInBrowser,
                                            contentDescription = "ブラウザで開く",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = item.note,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.url,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

data class CarrierItem(
    val name: String,
    val note: String,
    val url: String,
    val color: Color
)

@Composable
fun ModelPriceSearchDialog(
    initialModelName: String,
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    var modelName by remember { mutableStateOf(initialModelName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("機種名で買取相場を検索", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "IMEI（シリアル番号）ではなく、スマホの機種名（例: iPhone 14, Pixel 7a）を入力すると実際の買取相場を調べられます。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("機種名・モデル名") },
                    placeholder = { Text("例: iPhone 14 128GB") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val encoded = URLEncoder.encode(modelName.trim(), "UTF-8")
                            val iosysUrl = "https://k-tai-iosys.com/search?keyword=$encoded"
                            onOpenUrl(iosysUrl)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("イオシス買取相場", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val encoded = URLEncoder.encode(modelName.trim(), "UTF-8")
                            val mercariUrl = "https://jp.mercari.com/search?keyword=$encoded"
                            onOpenUrl(mercariUrl)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("メルカリ相場", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
