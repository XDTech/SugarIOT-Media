package org.sugar.media.component;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.enums.SocketMsgEnum;
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
