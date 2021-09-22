package com.udea.trabajofinalpds

import io.wavebeans.lib.io.input
import io.wavebeans.lib.io.sine
import io.wavebeans.lib.stream.fft.fft
import io.wavebeans.lib.stream.fft.inverseFft
import io.wavebeans.lib.stream.flatten
import io.wavebeans.lib.stream.plus
import io.wavebeans.lib.stream.trim
import io.wavebeans.lib.stream.window.hamming
import io.wavebeans.lib.stream.window.window
import java.util.concurrent.TimeUnit

class Processing {
    fun analyse(): List<Float> {
        val o = (880.sine() + 440.sine() + 220.sine())
            .trim(1000, TimeUnit.MILLISECONDS)

        val values = o.asSequence(44100.0f)
            .drop(500)
            .take(400)
            .map { it.toFloat() }
            .toList()

        return values
    }

    fun analyseFFT(): Pair<List<Double>, List<Double>> {
        val windowSize = 801
        val stepSize = 256
        val fftSize = 1024

        val signal = (400.sine())
            .window(windowSize, stepSize)

        val hammingFft = signal
            .hamming()
            .fft(fftSize)

        val valuesFFT = hammingFft
            .asSequence(1024f)
            .take(1024)
            .first()

        return Pair(valuesFFT.frequency().toList(),valuesFFT.magnitude().toList())
    }
}
