package org.sugar.media.component;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.Date;
import java.util.Optional;

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
