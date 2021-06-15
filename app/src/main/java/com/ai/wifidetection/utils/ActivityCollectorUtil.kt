package com.ai.wifidetection.utils

import android.app.Activity

import java.util.ArrayList

object ActivityCollectorUtil {
    var mActivityList = ArrayList<Activity>()

    /**
     * onCreate()时添加
     *
     * @param activity
     */
    fun addActivity(activity: Activity) { //判断集合中是否已经添加，添加过的则不再添加
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity)
        }
    }

    /**
     * onDestroy() 时删除
     *
     * @param activity
     */
    fun removeActivity(activity: Activity) {
        mActivityList.remove(activity)
    }

    /**
     * 关闭所有Activity
     */
    fun finishAllActivity() {
        for (activity in mActivityList) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

}
