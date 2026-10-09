package com.example.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ScanItem
import com.example.data.model.SearchTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    scanItem: ScanItem,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onToggleFavorite: (ScanItem) -> Unit,
    onUpdateNote: (ScanItem, String) -> Unit,
    onOpenSearchUrl: (url: String, title: String) -> Unit,
    onOpenExternalBrowser: (url: String) -> Unit
) {
    val context = LocalContext.current
    var isEditingNote by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(scanItem.note ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = CircleShape
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Format & Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = scanItem.format,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (scanItem.category != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = scanItem.category,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row {
                    // Favorite Toggle
                    IconButton(
                        onClick = { onToggleFavorite(scanItem) },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (scanItem.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "お気に入り切替",
                            tint = if (scanItem.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                val text = buildString {
                                    append("【商品バーコード検索】\n")
                                    if (!scanItem.productName.isNullOrBlank()) {
                                        append("商品名: ${scanItem.productName}\n")
                                    }
                                    if (!scanItem.brand.isNullOrBlank()) {
                                        append("ブランド: ${scanItem.brand}\n")
                                    }
                                    append("バーコード: ${scanItem.barcode}\n")
                                    append("Amazon: ${SearchTarget.AMAZON.buildSearchUrl(scanItem.barcode)}\n")
                                    append("Yahoo: ${SearchTarget.YAHOO.buildSearchUrl(scanItem.barcode)}\n")
                                    append("楽天: ${SearchTarget.RAKUTEN.buildSearchUrl(scanItem.barcode)}")
                                }
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "バーコードを共有"))
                        },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "共有")
                    }

                    // Close
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("detail_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "閉じる")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Product Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Product image or QR placeholder
                    if (!scanItem.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = scanItem.imageUrl,
                            contentDescription = scanItem.productName ?: "商品画像",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(4.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scanItem.productName ?: "読み取り完了",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!scanItem.brand.isNullOrBlank()) {
                            Text(
                                text = scanItem.brand,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barcode number chip with 1-tap copy
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Barcode", scanItem.barcode))
                                Toast.makeText(context, "バーコード番号をコピーしました", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = scanItem.barcode,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "コピー",
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Shopping & Price Comparison Hub
            Text(
                text = "自動検索・最安値・価格比較",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "各ショッピングサイトの最新価格やレビューを直接確認できます",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Shopping Service Cards Grid / Rows
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ShoppingServiceRow(
                    title = "Yahoo!ショッピング",
                    subtitle = "PayPayポイント・最安値ショップ検索",
                    icon = Icons.Default.ShoppingBag,
                    brandColor = Color(0xFFFF0033),
                    barcode = scanItem.barcode,
                    target = SearchTarget.YAHOO,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.YAHOO.buildSearchUrl(scanItem.barcode), "Yahoo!ショッピング") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.YAHOO.buildSearchUrl(scanItem.barcode)) }
                )

                ShoppingServiceRow(
                    title = "楽天市場",
                    subtitle = "楽天ポイント・取扱店舗まとめ",
                    icon = Icons.Default.Storefront,
                    brandColor = Color(0xFFBF0000),
                    barcode = scanItem.barcode,
                    target = SearchTarget.RAKUTEN,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.RAKUTEN.buildSearchUrl(scanItem.barcode), "楽天市場") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.RAKUTEN.buildSearchUrl(scanItem.barcode)) }
                )

                ShoppingServiceRow(
                    title = "Amazon",
                    subtitle = "プライム配送・カスタマーレビュー",
                    icon = Icons.Default.ShoppingBag,
                    brandColor = Color(0xFFFF9900),
                    barcode = scanItem.barcode,
                    target = SearchTarget.AMAZON,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.AMAZON.buildSearchUrl(scanItem.barcode), "Amazon") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.AMAZON.buildSearchUrl(scanItem.barcode)) }
                )

                ShoppingServiceRow(
                    title = "Google ショッピング",
                    subtitle = "全国ネット通販の価格を横断比較",
                    icon = Icons.Default.Search,
                    brandColor = Color(0xFF4285F4),
                    barcode = scanItem.barcode,
                    target = SearchTarget.GOOGLE,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.GOOGLE.buildSearchUrl(scanItem.barcode), "Google ショッピング") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.GOOGLE.buildSearchUrl(scanItem.barcode)) }
                )

                ShoppingServiceRow(
                    title = "メルカリ (Mercari)",
                    subtitle = "フリマ出品相場・中古取引価格を調べる",
                    icon = Icons.Default.ShoppingBag,
                    brandColor = Color(0xFFFF334B),
                    barcode = scanItem.barcode,
                    target = SearchTarget.MERCARI,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.MERCARI.buildSearchUrl(scanItem.barcode), "メルカリ") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.MERCARI.buildSearchUrl(scanItem.barcode)) }
                )

                ShoppingServiceRow(
                    title = "価格.com",
                    subtitle = "最安価格・価格推移・満足度レビュー",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    brandColor = Color(0xFF1B365D),
                    barcode = scanItem.barcode,
                    target = SearchTarget.KAKAKU,
                    onInAppClick = { onOpenSearchUrl(SearchTarget.KAKAKU.buildSearchUrl(scanItem.barcode), "価格.com") },
                    onExternalClick = { onOpenExternalBrowser(SearchTarget.KAKAKU.buildSearchUrl(scanItem.barcode)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // User Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "メモ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!isEditingNote) {
                    TextButton(
                        onClick = { isEditingNote = true },
                        modifier = Modifier.testTag("edit_note_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (scanItem.note.isNullOrBlank()) "メモを追加" else "編集", fontSize = 13.sp)
                    }
                }
            }

            if (isEditingNote) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_field"),
                        placeholder = { Text("例: 店舗名、購入価格、賞味期限など") },
                        singleLine = false,
                        maxLines = 3
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            noteText = scanItem.note ?: ""
                            isEditingNote = false
                        }) {
                            Text("キャンセル")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onUpdateNote(scanItem, noteText)
                                isEditingNote = false
                            },
                            modifier = Modifier.testTag("save_note_button")
                        ) {
                            Text("保存")
                        }
                    }
                }
            } else if (!scanItem.note.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = scanItem.note,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ShoppingServiceRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    brandColor: Color,
    barcode: String,
    target: SearchTarget,
    onInAppClick: () -> Unit,
    onExternalClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = brandColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // In-App browser preview button
                Button(
                    onClick = onInAppClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandColor
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("search_in_app_${target.name.lowercase()}")
                ) {
                    Text("検索", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // External browser icon button
                IconButton(
                    onClick = onExternalClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("search_external_${target.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "$title をブラウザで開く",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
