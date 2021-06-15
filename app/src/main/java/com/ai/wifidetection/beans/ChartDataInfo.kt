package com.ai.wifidetection.beans

import android.graphics.Color

open class ChartDataInfo {
    var xAxisValues: MutableList<String> = mutableListOf()
    var barChartYs: MutableList<Float> = mutableListOf()
    var lineChartYs: MutableList<MutableList<Float>> = mutableListOf()
    var barName: String = ""
    var lineNames: MutableList<String> = mutableListOf()
    var barColor: Int = Color.DKGRAY
    var lineColors: MutableList<Int> = mutableListOf()
}