package org.sugar.media.config;

import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.sugar.media.service.MediaCacheService;

/**
 * Date:2024/11/20 13:36:21
 * Author：Tobin
 * Description:
 */
    @Component
    public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {





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
            System.out.println(expiredKey+"=-===");
            System.out.println(message.getBody().toString()+"=-===");
            if (expiredKey.startsWith(MediaCacheService.REDIS_KEY_PREFIX)) {

            }

        }

}
