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

        // Japanese carrier restriction check URLs
        const val DOCOMO_URL = "http://nw-restriction.nttdocomo.co.jp/top.php"
        const val AU_URL = "https://my.au.com/cmn/WOS/WUEIP010/WUEIP010_01.pc"
        const val SOFTBANK_URL = "https://ct11.my.softbank.jp/w/"
        const val RAKUTEN_URL = "https://network.mobile.rakuten.co.jp/restriction/"
        const val MULTI_CHECKER_URL = "https://snow-white.cocolog-nifty.com/first/2014/03/4-3fa0.html"
    }
}
