package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ProductDetails(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val imageUrl: String?,
    val category: String?,
    val originCountry: String? = null
)

class ProductLookupService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Pre-registered popular Japanese retail sample barcodes for instant offline/test response
    private val popularJanDatabase = mapOf(
        "4902777008592" to ProductDetails(
            barcode = "4902777008592",
            name = "明治 チョコレート効果 カカオ72% 75g",
            brand = "明治 (Meiji)",
            imageUrl = "https://images.openfoodfacts.org/images/products/490/277/700/8592/front_ja.4.400.jpg",
            category = "菓子・チョコレート",
            originCountry = "日本 (49)"
        ),
        "4901005511170" to ProductDetails(
            barcode = "4901005511170",
            name = "江崎グリコ ポッキー チョコレート 2袋",
            brand = "江崎グリコ (Glico)",
            imageUrl = "https://images.openfoodfacts.org/images/products/490/100/551/1170/front_ja.12.400.jpg",
            category = "菓子・プレッツェル",
            originCountry = "日本 (49)"
        ),
        "4987035332510" to ProductDetails(
            barcode = "4987035332510",
            name = "大塚製薬 ポカリスエット 500ml",
            brand = "大塚製薬 (Otsuka)",
            imageUrl = "https://images.openfoodfacts.org/images/products/498/703/533/2510/front_ja.14.400.jpg",
            category = "飲料・スポーツドリンク",
            originCountry = "日本 (49)"
        ),
        "4901330573024" to ProductDetails(
            barcode = "4901330573024",
            name = "カルビー じゃがりこ サラダ 60g",
            brand = "カルビー (Calbee)",
            imageUrl = "https://images.openfoodfacts.org/images/products/490/133/057/3024/front_ja.11.400.jpg",
            category = "スナック菓子",
            originCountry = "日本 (49)"
        ),
        "4902102072625" to ProductDetails(
            barcode = "4902102072625",
            name = "コカ・コーラ 500ml PET",
            brand = "日本コカ・コーラ",
            imageUrl = "https://images.openfoodfacts.org/images/products/490/210/207/2625/front_ja.4.400.jpg",
            category = "炭酸飲料",
            originCountry = "日本 (49)"
        ),
        "4902370550733" to ProductDetails(
            barcode = "4902370550733",
            name = "Nintendo Switch (有機ELモデル) Joy-Con(L)/(R) ホワイト",
            brand = "任天堂 (Nintendo)",
            imageUrl = null,
            category = "ゲーム機本体",
            originCountry = "日本 (49)"
        ),
        "4901777235985" to ProductDetails(
            barcode = "4901777235985",
            name = "サントリー 伊右衛門 特茶 500ml",
            brand = "サントリー (Suntory)",
            imageUrl = "https://images.openfoodfacts.org/images/products/490/177/723/5985/front_ja.4.400.jpg",
            category = "特定保健用食品・緑茶",
            originCountry = "日本 (49)"
        ),
        "4901301010568" to ProductDetails(
            barcode = "4901301010568",
            name = "花王 アタックZERO ワンハンドタイプ 本体 380g",
            brand = "花王 (Kao)",
            imageUrl = null,
            category = "日用品・洗濯洗剤",
            originCountry = "日本 (49)"
        )
    )

    suspend fun lookupProduct(barcode: String): ProductDetails = withContext(Dispatchers.IO) {
        val cleanBarcode = barcode.trim()

        // 1. Check local catalog first for immediate response
        popularJanDatabase[cleanBarcode]?.let { return@withContext it }

        val origin = getBarcodeOriginCountry(cleanBarcode)

        // 2. Query Open Food Facts API
        try {
            val url = "https://world.openfoodfacts.org/api/v0/product/$cleanBarcode.json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BarcodeSearchApp - Android - Version 1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val status = json.optInt("status", 0)
                        if (status == 1 && json.has("product")) {
                            val productJson = json.getJSONObject("product")
                            val name = productJson.optString("product_name_ja")
                                .takeIf { it.isNotBlank() }
                                ?: productJson.optString("product_name").takeIf { it.isNotBlank() }
                            val brand = productJson.optString("brands").takeIf { it.isNotBlank() }
                            val imageUrl = productJson.optString("image_url").takeIf { it.isNotBlank() }
                                ?: productJson.optString("image_front_url").takeIf { it.isNotBlank() }
                            val category = productJson.optString("categories").takeIf { it.isNotBlank() }
                                ?: productJson.optString("categories_tags").takeIf { it.isNotBlank() }

                            return@withContext ProductDetails(
                                barcode = cleanBarcode,
                                name = name,
                                brand = brand,
                                imageUrl = imageUrl,
                                category = category,
                                originCountry = origin
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Network fallback or offline
        }

        // 3. Fallback product info with origin heuristics
        ProductDetails(
            barcode = cleanBarcode,
            name = null,
            brand = null,
            imageUrl = null,
            category = null,
            originCountry = origin
        )
    }

    fun getBarcodeOriginCountry(barcode: String): String {
        val b = barcode.trim()
        if (b.length == 13 || b.length == 8 || b.length == 12) {
            if (b.startsWith("45") || b.startsWith("49")) return "日本 (JANコード)"
            if (b.startsWith("40") || b.startsWith("41") || b.startsWith("42") || b.startsWith("43") || b.startsWith("44")) return "ドイツ"
            if (b.startsWith("50")) return "イギリス"
            if (b.startsWith("30") || b.startsWith("31") || b.startsWith("32") || b.startsWith("33") || b.startsWith("34") || b.startsWith("35") || b.startsWith("36") || b.startsWith("37")) return "フランス"
            if (b.startsWith("880")) return "韓国"
            if (b.startsWith("690") || b.startsWith("691") || b.startsWith("692") || b.startsWith("693") || b.startsWith("694") || b.startsWith("695")) return "中国"
            if (b.startsWith("471")) return "台湾"
            if (b.startsWith("00") || b.startsWith("01") || b.startsWith("02") || b.startsWith("03") || b.startsWith("04") || b.startsWith("05") || b.startsWith("06") || b.startsWith("07") || b.startsWith("08") || b.startsWith("09")) return "アメリカ / カナダ (UPC)"
        }
        return "国際規格"
    }

    fun getSampleBarcodes(): List<Pair<String, String>> {
        return listOf(
            "4901005511170" to "ポッキー (グリコ)",
            "4987035332510" to "ポカリスエット (大塚製薬)",
            "4901330573024" to "じゃがりこ (カルビー)",
            "4902777008592" to "チョコレート効果72% (明治)",
            "4902102072625" to "コカ・コーラ 500ml",
            "4902370550733" to "Nintendo Switch (任天堂)"
        )
    }
}
