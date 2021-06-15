package com.ai.wifidetection.ui.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ai.wifidetection.*
import com.ai.wifidetection.beans.mqttbeans.DetResultBean
import com.ai.wifidetection.beans.mqttbeans.NotifySubscribeBean
import com.ai.wifidetection.ui.customview.linechart.DynamicLineChartManager
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.utils.ActivityCollectorUtil
import com.ai.wifidetection.utils.DateTimeUtils
import com.ai.wifidetection.utils.DateTimeUtils.Companion.DATE_FORMAT_TIME_HMS
import com.ai.wifidetection.utils.LogUtil
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import com.hb.dialog.myDialog.MyAlertDialog
import kotlinx.android.synthetic.main.activity_main1.*
import kotlinx.android.synthetic.main.layout_title_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.system.exitProcess

/**
 * 主界面（使用lineChart作实时显示图）
 */
class Main1Activity : BaseActivity() {

    var macString: String? = null
    lateinit var dynamicLineChartManager: DynamicLineChartManager
    var myApp: MyApplication? = null

    private val REFERSH_PERIOD_MS = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)
        myApp = application as MyApplication

        macString = intent.getStringExtra("mac")
//        SharedPrefsUtil.putValue(this, "mac", macString!!)
        initTitleBar()
        initLineChart()

        tvDevice.setOnClickListener {
            val mDialog = MyAlertDialog(this).builder()
                .setTitle("确认")
                .setMsg("是否切换设备？")
                .setPositiveButton("确认") {

                    val intent = Intent(this, SelectStationActivity::class.java)
                    intent.putExtra("mac", macString)
                    startActivity(intent)
                }
                .setNegativeButton("取消"){

                }
            mDialog.show()
        }
        EventBus.getDefault().register(this)
    }

    private fun initTitleBar(){
        bar_title.text = macString!!.toLowerCase(Locale.ROOT)
        bar_right.setCompoundDrawablesWithIntrinsicBounds(null, null, this.resources.getDrawable(R.drawable.ic_action_router), null)
        bar_left.visibility = View.INVISIBLE
        bar_right.setOnClickListener {
            val mDialog = MyAlertDialog(this).builder()
                .setTitle("确认")
                .setMsg("是否切换路由器？")
                .setPositiveButton("确认") {
//                    MyMQTTService.unSubscribeToTopic()
                    val bean = NotifySubscribeBean()
                    bean.isSubscribe = false
                    EventBus.getDefault().post(bean)
                    MyBuffer.is_start_mqtt_service = false
                    finishGoTo(StartActivity::class.java)
                }
                .setNegativeButton("取消"){

                }
            mDialog.show()
        }
    }

    private fun initLineChart(){

        dynamicLineChartManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DynamicLineChartManager(
                lineChart,
                "prop",
                getColor(R.color.colorAccent)
            )
        }else{
            DynamicLineChartManager(
                lineChart,
                "prop",
                Color.BLACK
            )
        }
        dynamicLineChartManager.setYAxis(100f, -5f, 1)

        for (i in 0..100){
            dynamicLineChartManager.addEntry(0f)
        }

    }

    var tstamp_last = 0L
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun mqttArrived(messageEvent: DetResultBean){
        val tstamp_current = System.currentTimeMillis()
        if (tstamp_current - tstamp_last > REFERSH_PERIOD_MS){
            var propVal = messageEvent.value.toFloat()
            if (propVal < 2){
                propVal = Random().nextFloat()*2
            }
            runOnUiThread {
                dynamicLineChartManager.addEntry(propVal, messageEvent.result)
                dynamicLineChartManager.setDescription(DateTimeUtils.getTime(System.currentTimeMillis(), DATE_FORMAT_TIME_HMS))
                when (messageEvent.result) {
                    PREDICT_STATUS.somebody -> tvDetectionResult.text = "有人"
                    PREDICT_STATUS.nobody -> tvDetectionResult.text = "无人"
                    else -> tvDetectionResult.text = "未知"
                }
            }
            tstamp_last = tstamp_current
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i("DetailActivity: onDestroy")
        EventBus.getDefault().unregister(this)
    }

    /** 上次点击返回键的时间 */
    var lastBackPressed = 0L;
    /** 两次点击的间隔时间 */
    private val QUIT_INTERVAL = 2000;
    override fun onBackPressed() {
        val backPressed = System.currentTimeMillis()
        if (backPressed - lastBackPressed > QUIT_INTERVAL) {
            lastBackPressed = backPressed;
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
        } else {
//            SharedPrefsUtil.putValue(this, "mac", "")
            myApp!!.stopMqttService()
            ActivityCollectorUtil.finishAllActivity()
            exitProcess(0);
        }
    }
}
