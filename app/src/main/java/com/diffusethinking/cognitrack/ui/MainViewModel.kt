package com.diffusethinking.cognitrack.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diffusethinking.cognitrack.data.AppDatabase
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.data.TestRecordDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ActiveScreen {
    data object Onboarding : ActiveScreen()
    data object Tabs : ActiveScreen()
    data class Test(
        val isBaseline: Boolean,
        val isGuestMode: Boolean,
        val guestName: String
    ) : ActiveScreen()

    data class Results(
        val trials: List<Double>,
        val isBaseline: Boolean,
        val startDate: Long,
        val isGuestMode: Boolean,
        val guestName: String
    ) : ActiveScreen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("cognitrack_prefs", Context.MODE_PRIVATE)
    private val dao: TestRecordDao = AppDatabase.getInstance(application).testRecordDao()

    var activeScreen by mutableStateOf<ActiveScreen>(
        if (prefs.getBoolean("hasCompletedOnboarding", false)) ActiveScreen.Tabs
        else ActiveScreen.Onboarding
    )
        private set

    var selectedTab by mutableIntStateOf(0)

    var baselineRT by mutableDoubleStateOf(
        Double.fromBits(prefs.getLong("baselineRT", 0.0.toBits()))
    )
        private set

    var baselineDate by mutableDoubleStateOf(
        Double.fromBits(prefs.getLong("baselineDate", 0.0.toBits()))
    )
        private set

    var guestMode by mutableStateOf(prefs.getBoolean("guestMode", false))
        private set

    var guestName by mutableStateOf(prefs.getString("guestName", "") ?: "")
        private set

    val allRecords: StateFlow<List<TestRecord>> = dao.getAllRecordsSorted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordsAscending: StateFlow<List<TestRecord>> = dao.getAllRecordsAscending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: ActiveScreen) {
        activeScreen = screen
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean("hasCompletedOnboarding", true).apply()
    }

    fun updateBaselineRT(value: Double) {
        baselineRT = value
        prefs.edit().putLong("baselineRT", value.toBits()).apply()
    }

    fun updateBaselineDate(value: Double) {
        baselineDate = value
        prefs.edit().putLong("baselineDate", value.toBits()).apply()
    }

    fun updateGuestMode(value: Boolean) {
        guestMode = value
        prefs.edit().putBoolean("guestMode", value).apply()
        if (!value) {
            guestName = ""
            prefs.edit().putString("guestName", "").apply()
        }
    }

    fun updateGuestName(value: String) {
        guestName = value
        prefs.edit().putString("guestName", value).apply()
    }

    fun saveRecord(record: TestRecord) {
        viewModelScope.launch {
            dao.insert(record)
        }
    }

    fun deleteRecord(record: TestRecord) {
        viewModelScope.launch {
            dao.delete(record)
            if (record.isBaseline) recalculateBaseline()
        }
    }

    fun deleteRecords(records: List<TestRecord>) {
        viewModelScope.launch {
            records.forEach { dao.delete(it) }
            if (records.any { it.isBaseline }) recalculateBaseline()
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            dao.deleteAll()
            updateBaselineRT(0.0)
            updateBaselineDate(0.0)
            updateGuestMode(false)
        }
    }

    fun importData(records: List<TestRecord>, baseline: Double, baseDate: Double, guest: Boolean, gName: String) {
        viewModelScope.launch {
            dao.deleteAll()
            records.forEach { dao.insert(it) }
            updateBaselineRT(baseline)
            updateBaselineDate(baseDate)
            guestMode = guest
            prefs.edit().putBoolean("guestMode", guest).apply()
            guestName = gName
            prefs.edit().putString("guestName", gName).apply()
        }
    }

    fun updateRecord(record: TestRecord) {
        viewModelScope.launch {
            dao.update(record)
        }
    }

    suspend fun getAllRecordsList(): List<TestRecord> = dao.getAllRecordsList()

    private suspend fun recalculateBaseline() {
        val latest = dao.getLatestBaseline()
        if (latest != null) {
            updateBaselineRT(latest.averageRT)
            updateBaselineDate(latest.startDate / 1000.0)
        } else {
            updateBaselineRT(0.0)
            updateBaselineDate(0.0)
        }
    }
}
