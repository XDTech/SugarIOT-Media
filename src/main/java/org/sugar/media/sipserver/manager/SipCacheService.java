package org.sugar.media.sipserver.manager;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date:2024/12/12 10:50:34
 * Author：Tobin
 * Description: 缓存sip需要的数据
 */

@Slf4j
@Service
public class SipCacheService {

    public static String SIP_DEVICE_KEY = "sip_device:"; // 已经在数据库中添加的国标设备

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;


    public void setSipDevice(String deviceId, DeviceBean deviceBean) {


        String key = SIP_DEVICE_KEY + deviceId;

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(deviceBean));
    }

    public void delSipDevice(String deviceId) {


        String key = SIP_DEVICE_KEY + deviceId;
        stringRedisTemplate.delete(key);
    }

    // 获取媒体状态
    public DeviceBean getSipDevice(String deviceId) {
        String key = SIP_DEVICE_KEY + deviceId;
        String s = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(s)) return null;
        JSON parse = JSONUtil.parse(s);
        return parse.toBean(DeviceBean.class);
    }


    // 使用 Redis 操作
    public static String deviceCSEQ_PREFIX = "device:cseq:";

    public Long getNextCSeqFromRedis(String deviceId) {
        String key = deviceCSEQ_PREFIX + deviceId;

        // 如果键不存在，设置初始值
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().set(key, "1");
            return 1L;
        }

        return stringRedisTemplate.opsForValue().increment(key);
    }

    public void resetCSeqFromRedis(String deviceId) {
        String key = deviceCSEQ_PREFIX + deviceId;

        stringRedisTemplate.opsForValue().set(key, "1");
    }


    // 国标设备保活======================

    private static final long sip_device_keepalive = 60; // 系统默认60s保活


    public static String sip_device_keepalive_PREFIX = "sip_device_keepalive:"; // Key 的前缀

    private static final long keepalive_delay = 5;// 延迟5s

    public void setDeviceStatus(String deviceId, String status) {

        long expTime = sip_device_keepalive + keepalive_delay;
        String key = sip_device_keepalive_PREFIX + deviceId;


        stringRedisTemplate.opsForValue().set(key, status, expTime, TimeUnit.SECONDS);
    }

    public String getDeviceStatus(String deviceId) {
        String key = sip_device_keepalive_PREFIX + deviceId;
        return stringRedisTemplate.opsForValue().get(key);
    }


    // 删除和过期都能触发设备的重新认证
    public void deleteDevice(String deviceId) {


        String key = sip_device_keepalive_PREFIX + deviceId;
        stringRedisTemplate.delete(key);
    }

    public boolean isOnline(String deviceId) {

        String status = this.getDeviceStatus(deviceId);


        if (StrUtil.isBlank(status)) return false;


        return status.equals(StatusEnum.online.getStatus());
    }


    public void deleteKeysWithPrefixUsingScan(String prefix) {
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").build();
        List<String> keysToDelete = new ArrayList<>();

        try (Cursor<String> scan = stringRedisTemplate.scan(options)) {
            // key -> keys.add(key)
            scan.forEachRemaining(keysToDelete::add);
        }


        if (!keysToDelete.isEmpty()) {
            stringRedisTemplate.delete(keysToDelete); // 批量删除匹配的键
        }
    }


    // ===== ssrc====
    /**
     * 监测ssrc过期，用于释放ssrc
     * 每个ssrc 有10s 存活期，如果再次期间没有被使用，则利用过期回调释放
     * ssrc 格式为 ssrc:channelCode
     */
    public static String SIP_SSRC_KEY = "sip_ssrc:"; // ssrc key

    private static final long SIP_SSRC_TIMEOUT = 10; //

    public void setSsrc(String ssrc, String channelCode) {
        String key = SIP_SSRC_KEY + ssrc;
        stringRedisTemplate.opsForValue().set(key, channelCode, SIP_SSRC_TIMEOUT, TimeUnit.SECONDS);
    }

    public void deleteSsrc(String ssrc) {
        String key = SIP_SSRC_KEY + ssrc;
        stringRedisTemplate.delete(key);

        log.warn("[ssrc使用]:{}", ssrc);
    }


}
