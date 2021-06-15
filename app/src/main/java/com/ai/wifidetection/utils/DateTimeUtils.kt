package com.ai.wifidetection.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * TimeUtils
 *
 */
class DateTimeUtils private constructor() {

    init {
        throw AssertionError()
    }

    companion object {

        @SuppressLint("SimpleDateFormat")
        val DEFAULT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm")
        @SuppressLint("SimpleDateFormat")
        val DEFAULT_DATE_FORMAT2 = SimpleDateFormat("yyyy/MM/dd HH:mm")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_DATE = SimpleDateFormat("yyyy-MM-dd")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_TIME = SimpleDateFormat("HH:mm")

        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_DATE_V2 = SimpleDateFormat("yyyy年MM月dd日")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_TIME_V2 = SimpleDateFormat("HH:mm")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_TIME_HMS = SimpleDateFormat("HH:mm:ss")

        fun getDate(): String {
            return getDate(System.currentTimeMillis())
        }
        private fun getDate(timeInMillis: Long): String {
            val dateFormat = DATE_FORMAT_DATE
            return dateFormat.format(timeInMillis)
        }
        fun getDateFromFormat(day: String): Calendar? {
            val dateFormat = DATE_FORMAT_DATE
            val date = dateFormat.parse(day)
            val calendar =  Calendar.getInstance()
            calendar.time = date!!
            return calendar
        }
        fun getTimeFromFormat(day: String): Long? {
            val dateFormat = DATE_FORMAT_DATE
            val date = dateFormat.parse(day)
            return date!!.time
        }
        /**
         * long time to string
         *
         * @param timeInMillis
         * @param dateFormat
         * @return
         */
        @JvmOverloads
        fun getTime(timeInMillis: Long, dateFormat: SimpleDateFormat = DEFAULT_DATE_FORMAT): String {
            return dateFormat.format(Date(timeInMillis))
        }

        /**
         * long time to string
         *
         * @param timeInMillis
         * @return
         */

        fun getTime2String(timeInMillis: Long): String {
            return getTime(timeInMillis)
        }

        /**
         * get current time in milliseconds
         *
         * @return
         */
        private val currentTimeInLong: Long
            get() = System.currentTimeMillis()

        /**
         * get current time in milliseconds, format is [.DEFAULT_DATE_FORMAT]
         *
         * @return
         */
        val currentTimeInString: String
            get() = getTime(currentTimeInLong)

        /**
         * get current time in milliseconds
         *
         * @return
         */
        fun getCurrentTimeInString(dateFormat: SimpleDateFormat): String {
            return getTime(currentTimeInLong, dateFormat)
        }

        /**
         * get current time in milliseconds
         *
         * @return
         */
        fun getFormatDate(dateFormat: SimpleDateFormat, date: Date): String {
            return dateFormat.format(date)
        }

        private fun getUnitFormatDate(timeDiff: Long): String {
            var dateDesc = ""
            dateDesc = when {
                timeDiff < 60 -> {
                    "$timeDiff 秒"
                }
                timeDiff < 3600 -> {
                    val min = timeDiff / 60
                    val second = timeDiff % 60
                    "$min 分 $second 秒"
                }
                else -> {
                    val hour = timeDiff / 3600
                    val min = timeDiff % 3600 / 60
                    val second = timeDiff % 60
                    "$hour 时 $min 分 $second 秒"
                }
            }
            return dateDesc
        }

        fun getTimeDiff(timeInMillis: Long, timeInMillisStart: Long): String {
            return getUnitFormatDate(timeInMillis - timeInMillisStart)
        }
    }
}
