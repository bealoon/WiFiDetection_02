package com.ai.wifidetection

import com.ai.wifidetection.MyConstant.HTTP_SERVER_IP
import com.ai.wifidetection.MyConstant.HTTP_SERVER_PORT

/**
 * 基本信息对象
 * MQTT连接信息
 * HTTP服务器信息
 */
object MyConstant{

    var MQTT_SERVER_IP = "47.101.68.172"
    var MQTT_SERVER_PORT = 1883
    var MQTT_SERVER_USERNAME = "user01"
    var MQTT_SERVER_PWD = "password"

    val HTTP_SERVER_IP = "47.101.68.172"
    val HTTP_SERVER_PORT = 8001
}

/**
 * 预测结果值
 */
object PREDICT_STATUS {            //1 有人无人摔倒状态值
    var unknow = 0                      //未知
    var somebody = 1                    //活动
    var nobody = 2                      //
}

/**
 * 提示字段枚举
 * --使实时曲线、柱状图颜色随之改变
 */
object PREDICT_SCORE {
    var env_NG = 0                      //有环境噪音
    var env_OK = 1                      //环境较好
}


/**
 * 选择设备模式-手动、自动
 */
object MODE_SELECT2_STA {
    var auto = 0                        //自动
    var manual = 1                      //手动
}


/**
 * 监听主题项
 */
object TOPIC_ALL{
    var topic_device_result = "/perspicace/%s/huawei"
    var topic_device_list = "/mtk_devlist_upload/%s"
}

/**
 * 报警模式枚举
 */
object MODE_ALARM{
    var MODE_ALARM_CLOSE = 0                // 关闭报警模式
    var MODE_ALARM_ENDOWMENT = 2            // 养老报警模式 - 长时间无人活动报警
    var MODE_ALARM_SECURITY = 1             // 安防报警模式 - 有人活动报警
}

/**
 * 权限请求码、回复码枚举
 */
object AppPermissionCode{
    // request参数
    const val CODE_REQ_QR = 1001 // // 打开扫描界面请求码


    const val CODE_PERMISSION_CAMERA = 2001 // 打开摄像头
    const val CODE_PERMISSION_FINE_LOCATION = 2002 // 读写文件
    const val CODE_PERMISSION_EXTERNAL_STORAGE = 2003 // 读写文件
}


val  CODE_REQUEST_ACTIVITY_SELECT_STATION = 101
val  CODE_RESULT_ACTIVITY_SELECT_STATION = 102

object AppServerUrls{
    val URL_SERVER_REPORT_DAILY = "http://${HTTP_SERVER_IP}:${HTTP_SERVER_PORT}/report/index"
}

