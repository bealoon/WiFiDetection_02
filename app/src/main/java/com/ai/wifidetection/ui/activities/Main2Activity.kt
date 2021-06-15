package com.ai.wifidetection.ui.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.ai.wifidetection.*
import com.ai.wifidetection.beans.DailyReport
import com.ai.wifidetection.beans.mqttbeans.DetResultBean
import com.ai.wifidetection.beans.mqttbeans.NotifySubscribeBean
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.ui.customview.barchart.BarChartManager
import com.ai.wifidetection.ui.customview.barchart.MyBarChartManager
import com.ai.wifidetection.utils.ActivityCollectorUtil
import com.ai.wifidetection.utils.DateTimeUtils
import com.ai.wifidetection.utils.DateTimeUtils.Companion.DATE_FORMAT_TIME_HMS
import com.ai.wifidetection.utils.LogUtil
import com.ai.wifidetection.utils.SharedPrefsUtil
import com.ai.wifidetection.utils.dialogs.WarningDialog
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.google.gson.Gson
import com.hb.dialog.myDialog.MyAlertDialog
import com.hb.dialog.myDialog.MyAlertInputDialog
import com.ohmerhe.kolley.request.Http
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.layout_title_bar.*
import kotlinx.android.synthetic.main.layout_title_bar.bar_left
import kotlinx.android.synthetic.main.layout_title_bar.bar_right
import kotlinx.android.synthetic.main.layout_title_bar.bar_title
import kotlinx.android.synthetic.main.layout_title_bar_common.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Exception
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


/**
 * 主界面（使用barChart作实时显示图）
 */
class Main2Activity : BaseActivity() {

    var mAlarmMode = 0
    var mSelectDate: String = ""

    var macAP: String? = null
    var macSta: String? = null
    lateinit var myBarChartManager: MyBarChartManager
    lateinit var mReportBarChartManager: BarChartManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)

        initData()
        initView()
        initListener()

        getDayReport(macAP!!, mSelectDate)
    }

    //HTTP获取日报
    private fun getDayReport(macString: String, dateString: String){
        Http.get{
            url = AppServerUrls.URL_SERVER_REPORT_DAILY
            params {
                "mac" - macString
                "date" - dateString
            }
            headers{
                "Cookie" - "129983=123456"
            }
            onSuccess {
                val response = it.toString(Charset.forName("utf-8"))
//                LogUtil.d(" $url?mac=${macString}&date=${dateString}  >>> $response")
                try {
                    val report = Gson().fromJson(response, DailyReport::class.java) ?: return@onSuccess
                    when(report.code){
                        1000->{ //成功
                            val reportList = report.data!!.getDetailList()
                            LogUtil.d(reportList.toString())
                            runOnUiThread {
                                for (i in reportList.indices){
                                    mReportBarChartManager.addEntry(reportList[i])
                                }
                            }
                        }
                        1004->{
                            runOnUiThread {
                                Toasty.warning(this@Main2Activity, "参数有误").show()
                            }
                        }
                        1007->{
                            runOnUiThread {
                                Toasty.warning(this@Main2Activity, "$mSelectDate\n报告暂未生成").show()
                            }
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    Toasty.error(this@Main2Activity, "报告数据有误").show()
                }


            }
            onFail {
                Toasty.error(this@Main2Activity, "获取失败，请检查网络")
            }
            onStart {
                showLoadingDialog()
            }
            onFinish {
                dismissLoadingDialog()
            }
        }
    }
    private fun initData() {
        //获取上个界面传过来得参数
        macAP = intent.getStringExtra("mac_ap")
        macSta = intent.getStringExtra("mac_sta")
        // 获取报警模式
        mAlarmMode = SharedPrefsUtil.getValue(this, "alarm_mode", MODE_ALARM.MODE_ALARM_CLOSE)
        // 获取前一天的报告
        mSelectDate = DateTimeUtils.getTime(System.currentTimeMillis() - 24 * 3600 * 1000, DateTimeUtils.DATE_FORMAT_DATE)

        tvReportDate.text = mSelectDate
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_REQUEST_ACTIVITY_SELECT_STATION && resultCode == CODE_RESULT_ACTIVITY_SELECT_STATION) {
            macSta = data?.getStringExtra("mac_sta") //得到新Activity 关闭后返回的数据
            if (!macSta.isNullOrEmpty()) {
                tvDeviceSta.text = macSta
            }
        }
    }

    private fun initListener() {
        EventBus.getDefault().register(this)
        tvDevice.setOnClickListener {
            val mDialog = MyAlertDialog(this).builder()
                .setTitle("确认")
                .setMsg("是否切换设备？")
                .setPositiveButton("确认") {

                    goSelectStaActivity(macAP!!)
                }
                .setNegativeButton("取消") {

                }
            mDialog.show()
        }

        tvNextReport.setOnClickListener {
            val reportIntent = Intent(this, ReportActivity::class.java)
            reportIntent.putExtra("mac_ap", macAP)
            startActivity(reportIntent)
        }

        tvHintStatus.setOnLongClickListener {
            runOnUiThread {
                showCustomWarningDialog("注意", "房间内有人入侵", object : WarningDialog.OnDialogClickListener {
                    override fun onPositiveButtonClick(dialogInterface: DialogInterface) {
                        Toasty.success(this@Main2Activity, "正常").show()
                        dialogInterface.dismiss()
                    }

                    override fun onNegativeButtonClick(dialogInterface: DialogInterface) {
                        Toasty.success(this@Main2Activity, "异常").show()
                        dialogInterface.dismiss()
                    }

                    override fun onNegative2ButtonClick(dialogInterface: DialogInterface) {
                        runOnUiThread { showInputSuggestDialog() }
                        dialogInterface.dismiss()
                    }
                })
            }
            true
        }

        tvReportDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun initView() {
        //初始化View
        initTitleBar()
        initBarChart()

        //如果未获取到sta设备mac(即：没有配置ping设备) 则进入到选择设备界面
        if (!macSta.isNullOrEmpty()) {
            tvDeviceSta.text = macSta
        } else {
            goSelectStaActivity(macAP!!)
        }

        //Switch按钮点击事件
        switch_alarm_mode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                rg_alarm.visibility = View.VISIBLE
            } else {
                rg_alarm.visibility = View.GONE
                mAlarmMode = MODE_ALARM.MODE_ALARM_CLOSE
                SharedPrefsUtil.putValue(this, "alarm_mode", mAlarmMode)
            }
        }

        //报警模式选择
        rg_alarm.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_alarm_endowment -> {
                    mAlarmMode = MODE_ALARM.MODE_ALARM_ENDOWMENT
                    SharedPrefsUtil.putValue(this, "alarm_mode", mAlarmMode)
                }
                R.id.rb_alarm_security -> {
                    mAlarmMode = MODE_ALARM.MODE_ALARM_SECURITY
                    SharedPrefsUtil.putValue(this, "alarm_mode", mAlarmMode)
                }
            }
        }

        // 初始化报警模式
        when (mAlarmMode) {
            MODE_ALARM.MODE_ALARM_CLOSE -> {
                switch_alarm_mode.isChecked = false
                rg_alarm.visibility = View.GONE
            }
            MODE_ALARM.MODE_ALARM_ENDOWMENT -> {
                switch_alarm_mode.isChecked = true
                rg_alarm.visibility = View.VISIBLE
                rb_alarm_endowment.isChecked = true
            }
            MODE_ALARM.MODE_ALARM_SECURITY -> {
                switch_alarm_mode.isChecked = true
                rg_alarm.visibility = View.VISIBLE
                rb_alarm_security.isChecked = true
            }
        }
    }

    private fun initTitleBar() {
        bar_title.text = macAP!!.toLowerCase(Locale.ROOT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bar_right.setCompoundDrawablesWithIntrinsicBounds(null, null, this.resources.getDrawable(R.drawable.ic_action_router, null), null)
        }else{
            bar_right.setCompoundDrawablesWithIntrinsicBounds(null, null, this.resources.getDrawable(R.drawable.ic_action_router), null)
        }
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
                .setNegativeButton("取消") {

                }
            mDialog.show()
        }
    }

    private fun initBarChart() {
        val xBarLabel = arrayListOf("1", "2", "3", "4")
        myBarChartManager =
            MyBarChartManager(
                barChart,
                "0",
                Color.RED,
                xBarLabel,
                20
            )
        for (i in 0..100) {
            myBarChartManager.addEntry(0f)
        }

        mReportBarChartManager = BarChartManager(barChartReport, "日报", Color.BLUE, xBarLabel)
    }

    private fun showInputSuggestDialog(){
        val myAlertInputDialog = MyAlertInputDialog(this).builder()
            .setTitle("请输入您的评价")
            .setEditText("")
        myAlertInputDialog.setPositiveButton("确认") {
            runOnUiThread {
                Toasty.success(this, myAlertInputDialog.result)
            }
            myAlertInputDialog.dismiss()
        }.setNegativeButton("暂不评价") {
            myAlertInputDialog.dismiss()
        }
        myAlertInputDialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDatePicker(){
        val pickerDialog = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->
                LogUtil.d(date.toString())
                mSelectDate = DateTimeUtils.getFormatDate(SimpleDateFormat("yyyy-MM-dd"), date)
                runOnUiThread { tvReportDate.text = mSelectDate }
                Handler().postDelayed({getDayReport(macAP!!, mSelectDate)}, 200)
            })
            .setType(booleanArrayOf(true, true, true, false, false, false)) //显示时间项（年， 月， 日， 时， 分， 秒）
            .setCancelText("取消")//取消按钮文字
            .setSubmitText("确定")//确认按钮文字
            .setOutSideCancelable(true)
            .setTitleText("请选择报告日期")
            .setDate(DateTimeUtils.getDateFromFormat(mSelectDate))
            .isCenterLabel(true)    //是否只显示中间选中项的label文字，false则每项item全部都带有label
            .isDialog(false)
            .build()
        pickerDialog.show()
    }

    private fun goSelectStaActivity(macApString: String) {
        val intent = Intent(this, SelectSta2Activity::class.java)
//        val intent = Intent(this, SelectStationActivity::class.java)
        intent.putExtra("mac_ap", macApString)
        startActivityForResult(intent, CODE_REQUEST_ACTIVITY_SELECT_STATION)
    }

    private val REFERSH_PERIOD_MS = 300
    private var tstamp_last = 0L
    private var detResult: DetResultBean? = null
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun mqttArrived(messageEvent: DetResultBean) {
        // 一段时间内，获得多包数据，取最大值显示；
        if (detResult != null){
            if (detResult!!.value < messageEvent.value){
                detResult = messageEvent
            }
        }else{
            detResult = messageEvent
        }
        val tstamp_current = System.currentTimeMillis()
        if (tstamp_current - tstamp_last > REFERSH_PERIOD_MS) {
            var propVal = detResult!!.value.toFloat()
            if (propVal < 2) {
                propVal = Random().nextFloat() * 2
            }
            runOnUiThread {
                myBarChartManager.addEntry(propVal, detResult!!)
                MyBuffer.det_status = messageEvent.result
                myBarChartManager.setDescription(
                    DateTimeUtils.getTime(
                        System.currentTimeMillis(),
                        DATE_FORMAT_TIME_HMS
                    )
                )
                when (messageEvent.result) {
                    PREDICT_STATUS.somebody -> tvDetectionResult.text = "有人"
                    PREDICT_STATUS.nobody -> tvDetectionResult.text = "无人"
                    else -> tvDetectionResult.text = "未知"
                }
            }
            tstamp_last = tstamp_current
            detResult = null
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
            (application as MyApplication).stopMqttService()
            ActivityCollectorUtil.finishAllActivity()
            exitProcess(0);
        }
    }
}
