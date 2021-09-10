package com.udea.trabajofinalpds

data class FftSample(
    val index: Long,
    val binCount: Int,
    val samplesCount: Int,
    val sampleRate: Float,
    val magnitude: List<Double>,
    val phase: List<Double>,
    val frequency: List<Double>,
    val time: Long
)
