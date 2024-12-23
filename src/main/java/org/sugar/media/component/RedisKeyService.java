package org.sugar.media.component;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.Date;
import java.util.Optional;

/**
 * Date:2024/12/21 21:02:32
 * Author：Tobin
 * Description:
 */

@Service
public class RedisKeyService {

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private SipCacheService sipCacheService;

    // media key 过期或者删除
    public void mediaKeyPrefix(String expiredKey) {
        String[] split = expiredKey.split(MediaCacheService.REDIS_KEY_PREFIX);

        String mediaId = split[1];
        LeastConnectionUtil.removeServerList(mediaId);

        Optional<NodeModel> node = this.zlmNodeService.getNode(Convert.toLong(mediaId));
        node.ifPresent(nodeModel -> WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.mediaOffline, new Date(), nodeModel.getName())));

    }

    public void sipKeyPrefix(String expiredKey) {
        String[] split = expiredKey.split(SipCacheService.sip_device_keepalive_PREFIX);

        String deviceId = split[1];

        DeviceModel device = this.deviceService.getDevice(deviceId);

        if(ObjectUtil.isNotEmpty(device)&& this.sipCacheService.isOnline(deviceId)){
            WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.gbOffline, new Date(), device.getName()));
        }

    }
}
