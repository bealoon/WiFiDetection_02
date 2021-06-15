package com.ai.wifidetection.beans

import android.telecom.Call

class DailyReport {
    var code = 0
    var data: Report? = null
    var msg = ""

    inner class Report{
        var date = ""
        var detail = ""
        var mac = ""

        fun getDetailList(): MutableList<Float>{
            val len = (detail.length/2).toInt()
            var detailList = mutableListOf<Float>()
            for (i in 0 until len){
                detailList.add(detail.substring(2*i, 2*i+1).toInt().toFloat())
            }
            return detailList
        }
    }
}