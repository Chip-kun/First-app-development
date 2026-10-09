package com.example.data.model

data class ImeiInfo(
    val imei: String,
    val isValidLength: Boolean,
    val isValidLuhn: Boolean,
    val estimatedBrand: String?,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun parse(raw: String): ImeiInfo {
            val cleaned = raw.filter { it.isDigit() }.trim()
            val validLength = cleaned.length == 15
            val validLuhn = if (validLength) checkLuhn(cleaned) else false
            val brand = if (cleaned.length >= 8) estimateBrand(cleaned.substring(0, 8)) else null

            return ImeiInfo(
                imei = cleaned,
                isValidLength = validLength,
                isValidLuhn = validLuhn,
                estimatedBrand = brand
            )
        }

        private fun checkLuhn(number: String): Boolean {
            var sum = 0
            var alternate = false
            for (i in number.length - 1 downTo 0) {
                var n = number[i].digitToInt()
                if (alternate) {
                    n *= 2
                    if (n > 9) n = (n % 10) + 1
                }
                sum += n
                alternate = !alternate
            }
            return sum % 10 == 0
        }

        private fun estimateBrand(tac: String): String {
            return when {
                tac.startsWith("35") && (tac.startsWith("352") || tac.startsWith("353") || tac.startsWith("354") || tac.startsWith("356") || tac.startsWith("357") || tac.startsWith("358") || tac.startsWith("359")) -> "Apple / 一般GSM端末"
                tac.startsWith("358") || tac.startsWith("357") -> "Apple iPhone / iPad"
                tac.startsWith("354") -> "Sony Xperia"
                tac.startsWith("355") -> "Samsung Galaxy"
                tac.startsWith("356") -> "Sharp AQUOS"
                tac.startsWith("86") -> "Xiaomi / OPPO"
                tac.startsWith("01") -> "Google Pixel"
                else -> "スマートフォン端末"
            }
        }

        fun extractImeiFromScannedText(text: String): String? {
            // 1. Check for standalone 14-16 digit numbers (standard IMEI is 15 digits)
            val regex = Regex("""\b(\d{14,16})\b""")
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1]
            }

            // 2. Strip non-digits and test length
            val digits = text.filter { it.isDigit() }
            if (digits.length in 14..16) {
                return digits
            }

            // 3. If barcode has extra characters or check digits
            if (digits.length > 16) {
                val candidate15 = digits.substring(0, 15)
                if (checkLuhn(candidate15)) {
                    return candidate15
                }
            }
            return if (digits.length >= 14) digits.take(15) else null
        }

        // Japanese carrier restriction check URLs (Verified official working endpoints)
        // docomo: direct search form + top page + official support guide
        const val DOCOMO_SEARCH_URL = "http://nw-restriction.nttdocomo.co.jp/search.php"
        const val DOCOMO_TOP_URL = "http://nw-restriction.nttdocomo.co.jp/top.php"
        const val DOCOMO_GUIDE_URL = "https://www.docomo.ne.jp/support/confirmation/"
        const val DOCOMO_URL = DOCOMO_SEARCH_URL

        // SoftBank: official verification portal + direct endpoint
        const val SOFTBANK_OFFICIAL_URL = "https://www.softbank.jp/mobile/support/3g/restriction/"
        const val SOFTBANK_DIRECT_URL = "https://ct11.my.softbank.jp/WBF/icv"
        const val SOFTBANK_URL = SOFTBANK_OFFICIAL_URL

        // au (KDDI)
        const val AU_URL = "https://au-cs0.kddi.com/FtHome"
        const val AU_GUIDE_URL = "https://www.au.com/support/service/mobile/network-riyoseigen/"

        // Rakuten Mobile
        const val RAKUTEN_URL = "https://network.mobile.rakuten.co.jp/restriction/"
        
        // 4-carrier batch checker (Snowy Skies ネットワーク利用制限チェッカー)
        const val MULTI_CHECKER_URL = "https://snowyskies.jp/imeiChecking/"
    }
}
