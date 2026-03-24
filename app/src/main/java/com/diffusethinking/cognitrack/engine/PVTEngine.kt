package com.diffusethinking.cognitrack.engine

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PVTEngine(
    val isBaseline: Boolean,
    val baselineRT: Double = 0.0
) {
    sealed class Phase {
        data object Ready : Phase()
        data object Waiting : Phase()
        data object Stimulus : Phase()
        data object FalseStart : Phase()
        data class Responded(val ms: Int) : Phase()
        data object Complete : Phase()
    }

    var phase by mutableStateOf<Phase>(Phase.Ready)
        private set
    var currentTrial by mutableIntStateOf(0)
        private set
    val trialResults = mutableStateListOf<Double>()
    var stimulusOnsetTime by mutableLongStateOf(0L)
        private set
    var testStartDate by mutableLongStateOf(System.currentTimeMillis())
        private set

    val totalTrials = if (isBaseline) 10 else 6

    private var activeJob: Job? = null

    val lapseThreshold: Double?
        get() = if (baselineRT > 0) baselineRT * 1.5 else null

    val lapseCount: Int
        get() = lapseThreshold?.let { threshold ->
            trialResults.count { it > threshold }
        } ?: 0

    fun handleTap(scope: CoroutineScope) {
        when (phase) {
            Phase.Ready -> advanceToNextTrial(scope)
            Phase.Waiting -> triggerFalseStart(scope)
            is Phase.Stimulus -> recordResponse(scope)
            else -> {}
        }
    }

    fun tearDown() {
        activeJob?.cancel()
        activeJob = null
    }

    fun pause(scope: CoroutineScope) {
        if (phase != Phase.Waiting && phase != Phase.Stimulus) return
        activeJob?.cancel()
        activeJob = null
        phase = Phase.Waiting
        startWaitingPeriod(scope)
    }

    private fun advanceToNextTrial(scope: CoroutineScope) {
        if (currentTrial == 0) {
            testStartDate = System.currentTimeMillis()
        }
        currentTrial++
        startWaitingPeriod(scope)
    }

    private fun startWaitingPeriod(scope: CoroutineScope) {
        phase = Phase.Waiting
        val delayMs = (Random.nextDouble(2.0, 6.0) * 1000).toLong()
        activeJob?.cancel()
        activeJob = scope.launch {
            delay(delayMs)
            stimulusOnsetTime = SystemClock.elapsedRealtime()
            phase = Phase.Stimulus
        }
    }

    private fun triggerFalseStart(scope: CoroutineScope) {
        activeJob?.cancel()
        phase = Phase.FalseStart
        activeJob = scope.launch {
            delay(1500L)
            startWaitingPeriod(scope)
        }
    }

    private fun recordResponse(scope: CoroutineScope) {
        activeJob?.cancel()
        val now = SystemClock.elapsedRealtime()
        val reactionMs = (now - stimulusOnsetTime).toDouble()
        trialResults.add(reactionMs)
        phase = Phase.Responded(ms = reactionMs.toInt())

        activeJob = scope.launch {
            delay(2000L)
            if (trialResults.size >= totalTrials) {
                phase = Phase.Complete
            } else {
                advanceToNextTrial(scope)
            }
        }
    }
}
