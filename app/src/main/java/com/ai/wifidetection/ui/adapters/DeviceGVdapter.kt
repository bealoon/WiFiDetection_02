package com.ai.wifidetection.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.ai.wifidetection.R
import com.ai.wifidetection.beans.DeviceInfo

class DeviceGVAdapter(val context: Context) : BaseAdapter() {

    var mDeviceList: MutableList<DeviceInfo>? = mutableListOf()

    fun setDeviceInfoList(list: MutableList<DeviceInfo>){
        mDeviceList!!.clear()
        mDeviceList = list
    }

    override fun getCount(): Int {
        return mDeviceList!!.size
    }

    override fun getItem(position: Int): Any {
        return mDeviceList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var viewHolder: ViewHolder? = null
        val view: View
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_lv_item, null)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.tvDeviceMac?.text = mDeviceList?.get(position)?.mac
        viewHolder.tvDeviceName?.text = mDeviceList?.get(position)?.name
        viewHolder.checkBox?.isChecked = mDeviceList?.get(position)?.is_selected!!
        return view
    }

    class ViewHolder(viewItem: View) {
        var tvDeviceMac : TextView? = viewItem.findViewById(R.id.tv_lv_mac) as TextView
        var tvDeviceName : TextView? = viewItem.findViewById(R.id.tv_lv_name) as TextView
        var checkBox : CheckBox? = viewItem.findViewById(R.id.cb_lv_item) as CheckBox
    }
}
