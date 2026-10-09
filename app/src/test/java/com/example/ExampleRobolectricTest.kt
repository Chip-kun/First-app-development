package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ProductLookupService
import com.example.data.model.SearchTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("バーコード検索", appName)
    }

    @Test
    fun testSearchTargetUrls() {
        val testBarcode = "4901005511170"
        val yahooUrl = SearchTarget.YAHOO.buildSearchUrl(testBarcode)
        assertTrue(yahooUrl.contains("shopping.yahoo.co.jp"))
        assertTrue(yahooUrl.contains(testBarcode))

        val rakutenUrl = SearchTarget.RAKUTEN.buildSearchUrl(testBarcode)
        assertTrue(rakutenUrl.contains("search.rakuten.co.jp"))
        assertTrue(rakutenUrl.contains(testBarcode))

        val amazonUrl = SearchTarget.AMAZON.buildSearchUrl(testBarcode)
        assertTrue(amazonUrl.contains("amazon.co.jp"))
        assertTrue(amazonUrl.contains(testBarcode))

        val mercariUrl = SearchTarget.MERCARI.buildSearchUrl(testBarcode)
        assertTrue(mercariUrl.contains("mercari.com"))
        assertTrue(mercariUrl.contains(testBarcode))
    }

    @Test
    fun testProductLookupOriginDetection() {
        val service = ProductLookupService()
        val jpOrigin = service.getBarcodeOriginCountry("4901005511170")
        assertTrue(jpOrigin.contains("日本"))

        val usOrigin = service.getBarcodeOriginCountry("012345678905")
        assertTrue(usOrigin.contains("アメリカ"))
    }

    @Test
    fun testImeiParsingAndExtraction() {
        val rawBarcode = "IMEI: 358432109876543"
        val extracted = com.example.data.model.ImeiInfo.extractImeiFromScannedText(rawBarcode)
        assertEquals("358432109876543", extracted)

        val pureDigits = "015893001234567"
        val extractedPure = com.example.data.model.ImeiInfo.extractImeiFromScannedText(pureDigits)
        assertEquals("015893001234567", extractedPure)

        val imeiInfo = com.example.data.model.ImeiInfo.parse("358432109876543")
        assertTrue(imeiInfo.isValidLength)
        assertEquals(15, imeiInfo.imei.length)

        assertEquals("https://snowyskies.jp/imeiChecking/", com.example.data.model.ImeiInfo.MULTI_CHECKER_URL)
        assertTrue(com.example.data.model.ImeiInfo.DOCOMO_URL.contains("docomo"))
        assertTrue(com.example.data.model.ImeiInfo.SOFTBANK_URL.contains("softbank"))
    }
}
