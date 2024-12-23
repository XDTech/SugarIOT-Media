package org.sugar.media.component;

import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.stereotype.Component;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.sipserver.manager.SipCacheService;

/**
 * Date:2024/11/20 13:36:21
 * Author：Tobin
 * Description:
 */
@Component
@Data
public class RedisRemoveListener implements MessageListener {

    @Resource
    private RedisKeyService redisKeyService;
    //监听主题
    private final PatternTopic topic = new PatternTopic("__keyevent@*__:del");

    /**
     * @param message 消息
     * @param pattern 主题
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.startsWith(MediaCacheService.REDIS_KEY_PREFIX)) {


            this.redisKeyService.mediaKeyPrefix(expiredKey);

        }

        // 国标设备保活过期
        if (expiredKey.startsWith(SipCacheService.sip_device_keepalive_PREFIX)) {


            this.redisKeyService.sipKeyPrefix(expiredKey);

        }

    }


}
