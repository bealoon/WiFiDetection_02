package com.ai.wifidetection.beans.mqttbeans

import com.ai.wifidetection.beans.DeviceInfo

class DeviceListBean {
    var command: String? = null
    var msg: String? = null
    var sta: List<DeviceInfo>? = null
    var mac: String? = null
    var ip: String? = null
}

object COMMAND_UPLOAD{
    var req_set_csi_mac = "req_set_csi_mac"
    var query_mac = "query_mac"
    var query_list = "query_list"
}