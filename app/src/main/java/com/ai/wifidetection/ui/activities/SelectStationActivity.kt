package com.ai.wifidetection.ui.activities

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import com.ai.wifidetection.CODE_REQUEST_ACTIVITY_SELECT_STATION
import com.ai.wifidetection.CODE_RESULT_ACTIVITY_SELECT_STATION
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.mqttbeans.COMMAND_UPLOAD
import com.ai.wifidetection.beans.mqttbeans.DeviceListBean
import com.ai.wifidetection.beans.mqttbeans.NotifySubscribeBean
import com.ai.wifidetection.beans.mqttbeans.RunStatusBean
import com.ai.wifidetection.services.MyMQTTService
import com.ai.wifidetection.ui.adapters.DeviceGVAdapter
import com.ai.wifidetection.ui.base.BaseActivity
import com.ai.wifidetection.utils.LogUtil
import com.ai.wifidetection.utils.statusbar.StatusBarUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_select_station.*
import kotlinx.android.synthetic.main.layout_title_bar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SelectStationActivity : BaseActivity() {

    var macAP: String = ""
    var macSta: String = ""
    var mDetail: String? = null
    var mRVAdapter: DeviceGVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_station)
        //用来设置整体下移，状态栏沉浸
        StatusBarUtil.setRootViewFitsSystemWindows(this, true)

        macAP = intent.getStringExtra("mac_ap")!!

        initTitleBar()
        initRecycleList()
        initListener()
    }

    private fun initListener() {
        EventBus.getDefault().register(this)
        btn_auto_sta.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        // 获取设备列表
        MyMQTTService.publish("{\"command\":\"query_list\"}", macAP)
        //查询ping设备
        val topic_query_mac = "{\"command\":\"query_mac\"}"
        MyMQTTService.publish(topic_query_mac, macAP)
    }

    /**
     * 初始化list
     */
    private fun initRecycleList() {
        mRVAdapter = DeviceGVAdapter(this)
        gv_device.adapter = mRVAdapter
        val emptyView = TextView(this)
        emptyView.text = "未查询到设备"
        emptyView.layoutParams = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )
        emptyView.visibility = View.GONE
        (gv_device.parent as ViewGroup).addView(emptyView)
        gv_device.emptyView = emptyView
        gv_device.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val deviceList = mRVAdapter!!.mDeviceList
                for (i in deviceList!!.indices){
                    deviceList[i].is_selected = i == position
                }
                mRVAdapter!!.notifyDataSetChanged()
                // 获取设备列表
//                macSta = deviceList[position].mac!!.replace(":","").replace("-","")
                macSta = deviceList[position].mac!!
                val topic0 = "{\"command\":\"req_set_csi_mac\", \"mac\":\"%s\", \"ip\":\"%s\"}".format(macSta, deviceList[position].ip)
                MyMQTTService.publish(topic0, macAP)
                //查询ping设备
                val topic1 = "{\"command\":\"query_mac\"}"
                MyMQTTService.publish(topic1, macAP)
            }

        refreshLayout.setOnRefreshListener {
            MyMQTTService.publish("{\"command\":\"query_list\"}", macAP)
            //查询ping设备
            val topic1 = "{\"command\":\"query_mac\"}".format(macSta)
            MyMQTTService.publish(topic1, macAP)
            it.finishRefresh(1000 /*,false*/) //传入false表示刷新失败
        }
    }

    /**
     * 接收设备列表的mqtt消息
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun mqttArrived(messageEvent: DeviceListBean?){
        println(">>>mqttArrived "+ messageEvent!!.command)
        when(messageEvent.command!!){
            COMMAND_UPLOAD.query_list->{
                mRVAdapter!!.mDeviceList = messageEvent.sta!!.toMutableList()
                mRVAdapter!!.notifyDataSetChanged()
            }
            COMMAND_UPLOAD.query_mac->{
                var isQueryOk = false
                for (i in mRVAdapter!!.mDeviceList!!.indices){
                    if (mRVAdapter!!.mDeviceList?.get(i)?.mac == messageEvent.mac) {
                        mRVAdapter!!.mDeviceList?.get(i)?.is_selected = true
                        isQueryOk = true
                    }else{
                        mRVAdapter!!.mDeviceList?.get(i)?.is_selected = false
                    }
                }
                mRVAdapter!!.notifyDataSetChanged()
                if (isQueryOk){
                    runOnUiThread {
                        Toasty.success(this, "STA设备设置成功！")
                        macSta = messageEvent.mac!!
                    }
                }else{
                    macSta = ""
                }
            }
            COMMAND_UPLOAD.req_set_csi_mac->{
                LogUtil.e(messageEvent.msg)
            }
        }

    }

    private fun initTitleBar(){
        bar_title.text = "设备列表"
        bar_left.setOnClickListener {
            onBackPressed()
        }
        bar_right.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        val backIntent = Intent()
        backIntent.putExtra("mac_sta", macSta)
        this.setResult(CODE_RESULT_ACTIVITY_SELECT_STATION)
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
