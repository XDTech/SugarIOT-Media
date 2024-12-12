package org.sugar.media.sipserver.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.concurrent.TimeUnit;

/**
 * Date:2024/12/12 10:50:34
 * Author：Tobin
 * Description: 缓存sip需要的数据
 */

@Service
public class SipCacheService {

    public static String SIP_DEVICE_KEY = "sip_device:"; // 已经在数据库中添加的国标设备

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    public void setSipDevice(String deviceId, DeviceBean deviceBean) {


        String key = SIP_DEVICE_KEY + deviceId;

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(deviceBean));
    }

    // 获取媒体状态
    public DeviceBean getSipDevice(String deviceId) {
        String key = SIP_DEVICE_KEY + deviceId;
        String s = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(s)) return null;
        JSON parse = JSONUtil.parse(s);
        return parse.toBean(DeviceBean.class);
    }


}
