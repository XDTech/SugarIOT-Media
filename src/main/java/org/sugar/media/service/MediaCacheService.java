package org.sugar.media.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.sugar.media.enums.StatusEnum;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Date:2024/11/16 11:54:17
 * Author：Tobin
 * Description: 缓存流媒体服务器 在线状态
 */
@Service
public class MediaCacheService {

    private static final long TTL_ms = 10*1000; // 每个媒体的 TTL 为 10 秒
    public static  String REDIS_KEY_PREFIX = "media_status:"; // Key 的前缀


    private static final long ttl_delay=5*1000;
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // 保存媒体状态并设置独立的 TTL 毫秒
    public void setMediaStatus(Long mediaId,String status, Float seconds) {

        long expTime=TTL_ms+ttl_delay;
        String key = REDIS_KEY_PREFIX + mediaId;

        // 秒转毫秒
        if(seconds!=null){
           expTime=Convert.toLong(seconds*1000)+ttl_delay;
        }
        stringRedisTemplate.opsForValue().set(key, status, expTime, TimeUnit.MILLISECONDS);
    }

    // 获取媒体状态
    public String getMediaStatus(Long mediaId) {
        String key = REDIS_KEY_PREFIX + mediaId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 删除媒体状态
    public void removeMediaStatus(Long mediaId) {
        String key = REDIS_KEY_PREFIX + mediaId;
        stringRedisTemplate.delete(key);
    }

    // 判断媒体是否在线
    public boolean isOnline(Long mediaId) {
        String status = this.getMediaStatus(mediaId);


        if (StrUtil.isBlank(status)) return false;


        return status.equals(StatusEnum.online.getStatus());
    }

}
