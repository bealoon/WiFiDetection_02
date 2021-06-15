package com.ai.wifidetection.ui.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.ai.wifidetection.AppServerUrls.URL_SERVER_REPORT_DAILY
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.ChartDataInfo
import com.ai.wifidetection.beans.DailyReport
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.ui.customview.combinedchart.CombinedChartManager
import com.ai.wifidetection.utils.DateTimeUtils
import com.ai.wifidetection.utils.DateTimeUtils.Companion.DATE_FORMAT_DATE
import com.ai.wifidetection.utils.DateTimeUtils.Companion.DATE_FORMAT_TIME
import com.ai.wifidetection.utils.LogUtil
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.google.gson.Gson
import com.ohmerhe.kolley.request.Http
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.layout_title_bar_common.*
import java.lang.Exception
import java.nio.charset.Charset
import java.text.SimpleDateFormat

class ReportActivity : BaseActivity() {
    var macAP: String? = null
    var mSelectDate: String = ""
    lateinit var mCombinedChartManager: CombinedChartManager
    lateinit var mChartDataInfo: ChartDataInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)

        //获取上个界面传过来得参数
        macAP = intent.getStringExtra("mac_ap")

        mSelectDate = DateTimeUtils.getTime(System.currentTimeMillis(), DATE_FORMAT_DATE)
        initView()
        initListener();
        initData()
    }

    @SuppressLint("SimpleDateFormat")
    private fun initData() {
        bar_right.text = mSelectDate
        getDayReport(macAP!!, mSelectDate)
    }

    private fun initListener() {

    }

    private fun initView() {
        initTitleBar()
        initChart()

    }

    //List<String> xAxisValues, List<Float> barChartYs, List<List<Float>> lineChartYs,
    //            String barName, List<String> lineNames, Integer barColor, List<Integer> lineColors
    private fun initChart(){
        mChartDataInfo = ChartDataInfo()
        // 初始化x轴
        mChartDataInfo.xAxisValues.clear()
        val today = DateTimeUtils.getTimeFromFormat(mSelectDate)
        for (i in 0 until 288){
            val datetime = DateTimeUtils.getTime(today!! + i * 5 * 60 * 1000L - 8 * 3600 * 1000, DATE_FORMAT_TIME)
            mChartDataInfo.xAxisValues.add(datetime)
            LogUtil.d(datetime)
        }
        mChartDataInfo.barName = "日报"
        mChartDataInfo.barColor = Color.BLUE

        //
        val colors = mutableListOf<Int>(Color.DKGRAY, Color.RED, Color.GREEN, Color.BLUE)
        val lineNames = mutableListOf("1", "2", "3", "4")
        mCombinedChartManager = CombinedChartManager(combinedChart, "直方图一", lineNames, Color.BLUE, colors)
        mCombinedChartManager.showCombinedChart(mChartDataInfo)
        mCombinedChartManager.setXAxis(mChartDataInfo.xAxisValues)
    }


    private fun initTitleBar(){
        bar_title.text = "日报"
        bar_left.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bar_right.setCompoundDrawablesWithIntrinsicBounds(null, null, this.resources.getDrawable(R.drawable.ic_action_date, null), null)
        }else{
            bar_right.setCompoundDrawablesWithIntrinsicBounds(null, null, this.resources.getDrawable(R.drawable.ic_action_date), null)
        }
        bar_left.setOnClickListener {
            onBackPressed()
        }
        bar_right.setOnClickListener {
            showDatePicker()
        }
    }

    //HTTP获取日报
    private fun getDayReport(macString: String, dateString: String){
        Http.get{
            url = URL_SERVER_REPORT_DAILY
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
                            mChartDataInfo.barChartYs = reportList.toMutableList()
                            LogUtil.d(reportList.toString())
                            runOnUiThread {
                                mCombinedChartManager.showCombinedChart(mChartDataInfo)
                            }
                        }
                        1004->{
                            runOnUiThread {
                                Toasty.warning(this@ReportActivity, "参数有误").show()
                            }
                        }
                        1007->{
                            runOnUiThread {
                                Toasty.warning(this@ReportActivity, "$mSelectDate\n报告暂未生成").show()
                            }
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    Toasty.error(this@ReportActivity, "报告数据有误").show()
                }


            }
            onFail {
                Toasty.error(this@ReportActivity, "获取失败，请检查网络")
            }
            onStart {
                showLoadingDialog()
            }
            onFinish {
                dismissLoadingDialog()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDatePicker(){
        val pickerDialog = TimePickerBuilder(this,
            OnTimeSelectListener { date, v ->
                LogUtil.d(date.toString())
                mSelectDate = DateTimeUtils.getFormatDate(SimpleDateFormat("yyyy-MM-dd"), date)
                runOnUiThread { bar_right.text = mSelectDate }
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
}
