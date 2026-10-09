package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ProductLookupService
import com.example.data.db.AppDatabase
import com.example.data.model.AppSettings
import com.example.data.model.ScanItem
import com.example.data.model.SearchTarget
import com.example.util.SoundVibratorHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AutoOpenEvent {
    data class OpenBrowser(val url: String) : AutoOpenEvent()
    data class OpenInAppWeb(val url: String, val title: String) : AutoOpenEvent()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val scanDao = database.scanDao()
    private val productLookupService = ProductLookupService()
    private val feedbackHelper = SoundVibratorHelper(application)

    // App Settings
    private val _settings = MutableStateFlow(AppSettings.load(application))
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // History list from Room
    val scanHistory: StateFlow<List<ScanItem>> = scanDao.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently inspected item in Detail Sheet
    private val _activeDetailItem = MutableStateFlow<ScanItem?>(null)
    val activeDetailItem: StateFlow<ScanItem?> = _activeDetailItem.asStateFlow()

    // In-app web browser state
    private val _browserUrlState = MutableStateFlow<Pair<String, String>?>(null) // (url, title)
    val browserUrlState: StateFlow<Pair<String, String>?> = _browserUrlState.asStateFlow()

    // Events (e.g. auto-launching external browser)
    private val _autoOpenEvents = MutableSharedFlow<AutoOpenEvent>()
    val autoOpenEvents: SharedFlow<AutoOpenEvent> = _autoOpenEvents.asSharedFlow()

    // Search query in History
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // Favorites only toggle
    private val _historyFavoritesOnly = MutableStateFlow(false)
    val historyFavoritesOnly: StateFlow<Boolean> = _historyFavoritesOnly.asStateFlow()

    val sampleBarcodes: List<Pair<String, String>> = productLookupService.getSampleBarcodes()

    fun onBarcodeScanned(barcode: String, format: String) {
        val currentSettings = _settings.value

        // Haptic & Sound Feedback
        if (currentSettings.soundEnabled) {
            feedbackHelper.playBeep()
        }
        if (currentSettings.vibrationEnabled) {
            feedbackHelper.vibrate()
        }

        viewModelScope.launch {
            // Check if already in DB
            val existing = scanDao.getByBarcode(barcode)
            val itemToSave: ScanItem

            if (existing != null) {
                // Update timestamp and inspect
                itemToSave = existing.copy(timestamp = System.currentTimeMillis())
                scanDao.update(itemToSave)
            } else {
                // Lookup product metadata
                val details = productLookupService.lookupProduct(barcode)
                itemToSave = ScanItem(
                    barcode = barcode,
                    format = format,
                    productName = details.name,
                    brand = details.brand,
                    imageUrl = details.imageUrl,
                    category = details.category,
                    timestamp = System.currentTimeMillis()
                )
                val newId = scanDao.insert(itemToSave)
                itemToSave.copy(id = newId)
            }

            // Auto-Search decision
            if (currentSettings.autoOpenBrowser && currentSettings.autoSearchTarget != SearchTarget.ALL_HUB) {
                val url = currentSettings.autoSearchTarget.buildSearchUrl(barcode)
                _autoOpenEvents.emit(AutoOpenEvent.OpenBrowser(url))
            } else if (!currentSettings.continuousScan) {
                // Show Detail & Comparison sheet
                _activeDetailItem.value = itemToSave
            }
        }
    }

    fun openDetailSheet(item: ScanItem) {
        _activeDetailItem.value = item
    }

    fun closeDetailSheet() {
        _activeDetailItem.value = null
    }

    fun openInAppBrowser(url: String, title: String) {
        _browserUrlState.value = Pair(url, title)
    }

    fun closeInAppBrowser() {
        _browserUrlState.value = null
    }

    fun toggleFavorite(item: ScanItem) {
        viewModelScope.launch {
            val updated = item.copy(isFavorite = !item.isFavorite)
            scanDao.update(updated)
            if (_activeDetailItem.value?.id == item.id) {
                _activeDetailItem.value = updated
            }
        }
    }

    fun updateNote(item: ScanItem, note: String) {
        viewModelScope.launch {
            val updated = item.copy(note = note.trim().takeIf { it.isNotEmpty() })
            scanDao.update(updated)
            if (_activeDetailItem.value?.id == item.id) {
                _activeDetailItem.value = updated
            }
        }
    }

    fun deleteItem(item: ScanItem) {
        viewModelScope.launch {
            scanDao.delete(item)
            if (_activeDetailItem.value?.id == item.id) {
                _activeDetailItem.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            scanDao.clearAll()
        }
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryFavoritesOnly(favoritesOnly: Boolean) {
        _historyFavoritesOnly.value = favoritesOnly
    }

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        AppSettings.save(getApplication(), newSettings)
    }

    override fun onCleared() {
        super.onCleared()
        feedbackHelper.release()
    }
}
