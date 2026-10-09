package com.example.data.model

import java.net.URLEncoder

enum class SearchTarget(
    val title: String,
    val description: String,
    val iconName: String,
    val primaryColorHex: Long
) {
    ALL_HUB(
        title = "アプリ内で比較",
        description = "各通販サイトのボタンをまとめて一覧表示",
        iconName = "hub",
        primaryColorHex = 0xFF00796B
    ),
    YAHOO(
        title = "Yahoo!ショッピング",
        description = "PayPayポイントが貯まる・使える",
        iconName = "shopping_bag",
        primaryColorHex = 0xFFFF0033
    ),
    RAKUTEN(
        title = "楽天市場",
        description = "楽天ポイント還元・豊富な品揃え",
        iconName = "storefront",
        primaryColorHex = 0xFFBF0000
    ),
    AMAZON(
        title = "Amazon",
        description = "最速配送・カスタマーレビュー",
        iconName = "local_shipping",
        primaryColorHex = 0xFFFF9900
    ),
    GOOGLE(
        title = "Google ショッピング",
        description = "全国のネットショップ最安値を横断比較",
        iconName = "search",
        primaryColorHex = 0xFF4285F4
    ),
    MERCARI(
        title = "メルカリ",
        description = "フリマ相場・中古価格・売れ筋チェック",
        iconName = "sell",
        primaryColorHex = 0xFFFF334B
    ),
    KAKAKU(
        title = "価格.com",
        description = "価格推移・最安値店舗・クチコミ",
        iconName = "trending_down",
        primaryColorHex = 0xFF1B365D
    );

    fun buildSearchUrl(barcodeOrQuery: String): String {
        val encoded = try {
            URLEncoder.encode(barcodeOrQuery.trim(), "UTF-8")
        } catch (_: Exception) {
            barcodeOrQuery.trim()
        }
        return when (this) {
            ALL_HUB -> "https://www.google.com/search?q=$encoded"
            YAHOO -> "https://shopping.yahoo.co.jp/search?p=$encoded"
            RAKUTEN -> "https://search.rakuten.co.jp/search/mall/$encoded/"
            AMAZON -> "https://www.amazon.co.jp/s?k=$encoded"
            GOOGLE -> "https://www.google.com/search?tbm=shop&q=$encoded"
            MERCARI -> "https://jp.mercari.com/search?keyword=$encoded"
            KAKAKU -> "https://kakaku.com/search_results/$encoded/"
        }
    }
}
