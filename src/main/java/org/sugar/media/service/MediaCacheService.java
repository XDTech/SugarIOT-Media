package org.sugar.media.service;

import cn.hutool.core.convert.Convert;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Date:2024/11/16 11:54:17
 * Author：Tobin
 * Description: 缓存流媒体服务器 在线状态
 */
@Service
public class MediaCacheService {
    private static final String REDIS_KEY = "media_status";


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // 保存媒体状态
    public void setMediaStatus(Long mediaId, String status) {
        stringRedisTemplate.opsForHash().put(REDIS_KEY, Convert.toStr(mediaId), status);
    }

    // 获取媒体状态
    public Object getMediaStatus(Long mediaId) {
        return stringRedisTemplate.opsForHash().get(REDIS_KEY, Convert.toStr(mediaId));
    }

    // 获取所有媒体状态
    public Map<Object, Object> getAllMediaStatus() {
        return stringRedisTemplate.opsForHash().entries(REDIS_KEY);
    }

    // 删除媒体状态
    public void removeMediaStatus(Long mediaId) {
        stringRedisTemplate.opsForHash().delete(REDIS_KEY, Convert.toStr(mediaId));
    }
}
