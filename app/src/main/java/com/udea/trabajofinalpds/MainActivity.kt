package com.udea.trabajofinalpds

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.pow


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var xChart : LineChart
    private lateinit var yChart : LineChart
    private lateinit var zChart : LineChart
    private var plotData = false
    private lateinit var datax : ArrayList<Float>
    private lateinit var datay : ArrayList<Float>
    private lateinit var dataz : ArrayList<Float>
    private var ts = 200000F*(10F.pow(-6))
    private var fs = 1/ts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorAccelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensorAccelerometer != null) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val module = py.getModule("processing")

        val btStart : Button = findViewById(R.id.btStart)
        val btEnd : Button = findViewById(R.id.btEnd)
        val tv_distance : TextView = findViewById(R.id.tv_distance)
        val tv_velocity : TextView = findViewById(R.id.tv_velocity)
        val tv_jumps : TextView = findViewById(R.id.tv_jumps)

        datax = ArrayList()
        datay = ArrayList()
        dataz = ArrayList()

        btStart.setOnClickListener {
            plotData = true
            datax = ArrayList()
            datay = ArrayList()
            dataz = ArrayList()
        }

        btEnd.setOnClickListener {
            plotData = false
            autocorrelation(module)
            tv_distance.text = getDistance(module)
            tv_velocity.text = getVelocity(module)
            tv_jumps.text = getJumps(module)
        }

        initializeCharts()
    }

    private fun getDistance(module: PyObject) : String {
        try {
            val distance = module.callAttr("getDistance", datax.toArray(), datay.toArray(), dataz.toArray())
            return distance.toString() + " m"
        } catch (e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        return ""
    }

    private fun getVelocity(module: PyObject) : String {
        try {
            val velocity = module.callAttr("getVelocity", datax.toArray(), datay.toArray(), dataz.toArray())
            return velocity.toString() + " m/s"
        } catch (e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        return ""
    }

    private fun getJumps(module: PyObject) : String {
        try {
            val jumps = module.callAttr("getJumps", datay.toArray())
            return jumps.toString()
        } catch (e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
        return ""
    }

    private fun autocorrelation(module: PyObject) {
        try {
            var bytes = module.callAttr("plot_autocorrelation", datax.toArray())
                .toJava(ByteArray::class.java)
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            findViewById<ImageView>(R.id.imageView_X).setImageBitmap(bitmap)

            bytes = module.callAttr("plot_autocorrelation", datay.toArray())
                    .toJava(ByteArray::class.java)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            findViewById<ImageView>(R.id.imageView_Y).setImageBitmap(bitmap)

            bytes = module.callAttr("plot_autocorrelation", dataz.toArray())
                    .toJava(ByteArray::class.java)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            findViewById<ImageView>(R.id.imageView_Z).setImageBitmap(bitmap)

            currentFocus?.let {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(it.windowToken, 0)
            }
        } catch (e: PyException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeCharts() {
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
            datax.add(event.values[0])
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
            datay.add(event.values[1])
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
            dataz.add(event.values[2])
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