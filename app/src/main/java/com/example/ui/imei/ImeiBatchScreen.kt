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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun ImeiBatchScreen(
    onOpenUrl: (url: String) -> Unit,
    onOpenInAppBrowser: (url: String, title: String) -> Unit,
    onNavigateToContinuousScanner: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val imeiList = remember { mutableStateListOf<ImeiInfo>() }

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
        Toast.makeText(context, "IMEI「$imei」をコピーしました。確認画面で貼り付けてください", Toast.LENGTH_LONG).show()
        onOpenUrl(url)
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
                                text = "IMEI (製造番号) 一括検索",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "利用制限（○△×）や端末モデル・買取相場を確認",
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

                // Multi-line Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("IMEI番号を貼り付け（複数行・カンマ区切り対応）\n例: 358432109876543") },
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                addImeisFromText(inputText)
                                inputText = ""
                            } else {
                                Toast.makeText(context, "IMEIを入力または貼り付けてください", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_imei_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("リストに追加", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotBlank()) {
                                addImeisFromText(clipText)
                                Toast.makeText(context, "クリップボードからIMEIを追加しました", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "クリップボードが空です", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("paste_imei_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("貼り付け", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToContinuousScanner,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("continuous_scan_imei_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("スキャン", fontSize = 13.sp)
                    }
                }

                // Sample IMEIs for instant test
                Spacer(modifier = Modifier.height(10.dp))
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
                            "358432109876543" to "iPhone",
                            "355987123456789" to "Galaxy",
                            "012345678901234" to "Pixel"
                        )
                        items(samples) { (sampleImei, label) ->
                            AssistChip(
                                onClick = {
                                    if (imeiList.none { it.imei == sampleImei }) {
                                        imeiList.add(0, ImeiInfo.parse(sampleImei))
                                    }
                                },
                                label = { Text("$label ($sampleImei)", fontSize = 11.sp) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("sample_imei_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multi-Carrier Quick Access Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "4大キャリア利用制限 判定サイト",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "各社公式窓口",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CarrierButton(name = "docomo", color = Color(0xFFCC0000), modifier = Modifier.weight(1f)) {
                        onOpenUrl(ImeiInfo.DOCOMO_URL)
                    }
                    CarrierButton(name = "au", color = Color(0xFFFF6600), modifier = Modifier.weight(1f)) {
                        onOpenUrl(ImeiInfo.AU_URL)
                    }
                    CarrierButton(name = "SoftBank", color = Color(0xFF757575), modifier = Modifier.weight(1f)) {
                        onOpenUrl(ImeiInfo.SOFTBANK_URL)
                    }
                    CarrierButton(name = "楽天", color = Color(0xFFBF0000), modifier = Modifier.weight(1f)) {
                        onOpenUrl(ImeiInfo.RAKUTEN_URL)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onOpenInAppBrowser(ImeiInfo.MULTI_CHECKER_URL, "4キャリア一括利用制限確認") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("open_multi_carrier_checker_button")
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("4社を一括確認できるサイトを開く", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (imeiList.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val text = imeiList.joinToString("\n") { it.imei }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("ImeiList", text))
                        Toast.makeText(context, "${imeiList.size}件のIMEIを一括コピーしました", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_all_imeis_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("一括コピー", fontSize = 12.sp)
                }
            }
        }

        // List of IMEIs or Empty State
        if (imeiList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "IMEIが登録されていません",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "スマホ外箱のバーコードをカメラで読み取るか、\n上のテキスト欄から番号を貼り付けてください",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
            ) {
                items(imeiList, key = { it.imei }) { item ->
                    ImeiCard(
                        item = item,
                        onDelete = { imeiList.remove(item) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("IMEI", item.imei))
                            Toast.makeText(context, "IMEIをコピーしました", Toast.LENGTH_SHORT).show()
                        },
                        onCarrierClick = { carrierUrl, carrierName ->
                            copyImeiAndOpen(item.imei, carrierUrl, carrierName)
                        },
                        onMarketPriceClick = { serviceUrl ->
                            onOpenUrl(serviceUrl)
                        }
                    )
                }
            }
        }
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
    onMarketPriceClick: (url: String) -> Unit
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

            // Action 1: 1-Tap Carrier Network Restriction Checkers (Copies IMEI and opens Carrier)
            Text(
                text = "キャリア判定（番号コピー＆各社窓口を開く）:",
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

            // Action 2: Second hand price / Mercari / Iosys check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val mercariUrl = "https://jp.mercari.com/search?keyword=${item.imei}"
                        onMarketPriceClick(mercariUrl)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("メルカリ相場", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val iosysUrl = "https://k-tai-iosys.com/search?keyword=${item.imei}"
                        onMarketPriceClick(iosysUrl)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("買取相場(イオシス)", fontSize = 11.sp)
                }
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
