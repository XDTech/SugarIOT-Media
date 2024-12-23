package org.sugar.media.component;

import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.sipserver.manager.SipCacheService;

/**
 * Date:2024/11/20 13:36:21
 * Author：Tobin
 * Description:
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {


    @Resource
    private RedisKeyService redisKeyService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
        // TODO Auto-generated constructor stub
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // message.toString()可以获取失效的key
        String expiredKey = message.toString();
        System.out.println(expiredKey + "=-===");
        System.out.println(message.getBody().toString() + "=-===");
        if (expiredKey.startsWith(MediaCacheService.REDIS_KEY_PREFIX)) {


            this.redisKeyService.mediaKeyPrefix(expiredKey);

        }

        // 国标设备保活过期
        if (expiredKey.startsWith(SipCacheService.sip_device_keepalive_PREFIX)) {


            this.redisKeyService.sipKeyPrefix(expiredKey);

        }


    }

}
