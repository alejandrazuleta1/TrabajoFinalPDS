package com.udea.trabajofinalpds

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var xChart : LineChart
    private lateinit var yChart : LineChart
    private lateinit var zChart : LineChart
    private lateinit var fftChart : LineChart
    private var plotData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorAccelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensorAccelerometer != null) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        val btStart : Button = findViewById(R.id.btStart)
        val btEnd : Button = findViewById(R.id.btEnd)

        btStart.setOnClickListener {
            plotData = true
        }

        btEnd.setOnClickListener {
            plotData = false
            val processing = Processing()
            //val listValues = processing.analyse()
            val listValues = processing.analyseFFT()
            val listEntries = ArrayList<Entry>()
            val datafft = fftChart.data

            /*for (i in listValues.indices){
                listEntries.add(Entry(i.toFloat(), listValues[i]))
            }*/

            for (i in listValues.first.indices){
                listEntries.add(Entry(listValues.first[i].toFloat(), listValues.second[i].toFloat()))
            }

            val lineDataset = LineDataSet(listEntries, "FFT")
            if (datafft != null) {
                lineDataset.setDrawCircles(false)
                datafft.addDataSet(lineDataset)
                datafft.notifyDataChanged()
                fftChart.notifyDataSetChanged()
                fftChart.moveViewToX(datafft.entryCount.toFloat())
            }
        }

        val datax = LineData()
        datax.setValueTextColor(Color.WHITE)
        val datay = LineData()
        datay.setValueTextColor(Color.WHITE)
        val dataz = LineData()
        dataz.setValueTextColor(Color.WHITE)
        val datafft = LineData()
        datafft.setValueTextColor(Color.WHITE)

        xChart = findViewById(R.id.lineChartX)
        xChart.setDrawGridBackground(false)
        xChart.setTouchEnabled(false)
        xChart.setBackgroundColor(Color.WHITE)
        xChart.data = datax

        yChart = findViewById(R.id.lineChartY)
        yChart.setDrawGridBackground(false)
        yChart.setTouchEnabled(false)
        yChart.setBackgroundColor(Color.WHITE)
        yChart.data = datay

        zChart = findViewById(R.id.lineChartZ)
        zChart.setDrawGridBackground(false)
        zChart.setTouchEnabled(false)
        zChart.setBackgroundColor(Color.WHITE)
        zChart.data = dataz

        fftChart = findViewById(R.id.lineChartFFT)
        fftChart.setDrawGridBackground(false)
        fftChart.setTouchEnabled(false)
        fftChart.setBackgroundColor(Color.WHITE)
        fftChart.data = datafft
    }

    private fun addEntry(event: SensorEvent?) {
        val dataX = xChart.data
        val dataY = yChart.data
        val dataZ = zChart.data

        if (dataX != null) {
            var set = dataX.getDataSetByIndex(0)
            if (set == null) {
                set = createSet(0)
                dataX.addDataSet(set)
            }
            dataX.addEntry(Entry(set.entryCount.toFloat(), event!!.values[0]), 0)
            dataX.notifyDataChanged()
            xChart.notifyDataSetChanged()
            xChart.setVisibleXRangeMaximum(150f)
            xChart.moveViewToX(dataX.entryCount.toFloat())
        }

        if (dataY != null) {
            var set = dataY.getDataSetByIndex(0)
            if (set == null) {
                set = createSet(1)
                dataY.addDataSet(set)
            }
            dataY.addEntry(Entry(set.entryCount.toFloat(), event!!.values[1]), 0)
            dataY.notifyDataChanged()
            yChart.notifyDataSetChanged()
            yChart.setVisibleXRangeMaximum(150f)
            yChart.moveViewToX(dataY.entryCount.toFloat())
        }

        if (dataZ != null) {
            var set = dataZ.getDataSetByIndex(0)
            if (set == null) {
                set = createSet(2)
                dataZ.addDataSet(set)
            }
            dataZ.addEntry(Entry(set.entryCount.toFloat(), event!!.values[2]), 0)
            dataZ.notifyDataChanged()
            zChart.notifyDataSetChanged()
            zChart.setVisibleXRangeMaximum(150f)
            zChart.moveViewToX(dataZ.entryCount.toFloat())
        }
    }

    private fun createSet(index : Int): LineDataSet {
        val labels = ArrayList<String>()
        labels.add("Eje X")
        labels.add("Eje Y")
        labels.add("Eje Z")

        val colors = ArrayList<Int>()
        colors.add(Color.BLACK)
        colors.add(Color.BLUE)
        colors.add(Color.MAGENTA)

        val set = LineDataSet(null, labels[index])
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 3f
        set.color = colors[index]
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.2f
        return set
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(plotData){
            addEntry(event)
        }
    }
}