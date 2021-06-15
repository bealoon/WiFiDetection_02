package com.ai.wifidetection.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.DeviceInfo

// ① 创建Adapter
class DeviceRecyclerViewAdapter() :
    RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder>() {

    public var mDeviceList: MutableList<DeviceInfo> = mutableListOf()

    //② 创建ViewHolder
    class ViewHolder internal constructor(v: View?) : RecyclerView.ViewHolder(v!!) {
        var tvDeviceName : TextView? = v!!.findViewById(R.id.tv_lv_name) as TextView
        var checkBox : CheckBox? = v!!.findViewById(R.id.cb_lv_item) as CheckBox
    }

    //③ 在Adapter中实现3个方法
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.tvDeviceName!!.text = mDeviceList[position].mac
        holder.checkBox!!.isChecked = mDeviceList[position].is_selected
        holder.itemView.setOnClickListener {
            //item 点击事件
        }
    }

    override fun getItemCount(): Int {
        return mDeviceList.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        //LayoutInflater.from指定写法
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_lv_item, parent, false))
    }

}