package com.ai.wifidetection.ui.base

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Message
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.ai.wifidetection.R
import com.ai.wifidetection.utils.ActivityCollectorUtil
import com.ai.wifidetection.utils.AppTools
import com.ai.wifidetection.utils.animations.AnimationUtils
import com.ai.wifidetection.utils.dialogs.WarningDialog
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import com.hb.dialog.dialog.LoadingDialog


abstract class BaseActivity : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null
    private var warningCustomDialog: WarningDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollectorUtil.addActivity(this)
        initStatusBar()
    }

    private fun initStatusBar(){
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000)
        }
    }

    // 提示警告弹框
    fun showCustomWarningDialog(title: String?, msg: String, listener: WarningDialog.OnDialogClickListener){
        if (null == warningCustomDialog){
            warningCustomDialog = WarningDialog.Builder(this)
//                .setHeightOffSize(0.7f)
                .setOnClickListener(listener)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("正常")
                .setNegativeButton("异常")
                .setNegative2Button("其他")
                .create()
        }
        warningCustomDialog!!.show()
    }

    fun dismissCustomWarningDialog(){
        if (null != warningCustomDialog && warningCustomDialog!!.isShowing){
            warningCustomDialog!!.dismiss()
        }
    }

    fun showLoadingDialog(){
        loadingDialog = LoadingDialog(this)
        loadingDialog!!.setCanceledOnTouchOutside(false)
        loadingDialog!!.setMessage("加载中")
        loadingDialog!!.show()
    }

    fun dismissLoadingDialog(){
        if (loadingDialog!= null){
            loadingDialog!!.dismiss()
        }
    }

    fun finishGoTo(clz: Class<*>) {
        clearGoTo(clz)
        finish()
    }

    fun clearGoTo(clz: Class<*>) {
        val intent = getClearTopIntent(clz)
        startActivity(intent)
    }

    fun getClearTopIntent(clz: Class<*>) : Intent {
        val intent = Intent(this, clz)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        return intent
    }

    override fun onStop() {
        super.onStop()
//        if (OPEN_BACK_BOARD_CHECK && !isAppOnForeground() && !isFinishing()) {
//            AppTools.showToast(this, R.string.backboard_hing);
//        }
    }

    //判断是不是到后台
    private fun isAppOnForeground(): Boolean {
        val activityManager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageName = applicationContext.packageName
        val appProcesses = activityManager
            .runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName == packageName && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCustomWarningDialog()
        dismissLoadingDialog()
    }
}