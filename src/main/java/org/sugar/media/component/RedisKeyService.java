package org.sugar.media.component;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.Date;
import java.util.Optional;

/**
 * Date:2024/12/21 21:02:32
 * Author：Tobin
 * Description:
 */

@Slf4j
@Service
public class RedisKeyService {

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private DeviceService deviceService;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private ChannelService channelService;


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

        if (ObjectUtil.isNotEmpty(device)) {

            // 把该设备下所有的通道都离线
            this.channelService.updateChannelStatus(device.getId(), StatusEnum.offline);
            WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.gbOffline, new Date(), device.getName()));


        }

    }

    // 国标设备ssrc过期监听

    public void sipSsrcKeyPrefix(String expiredKey) {
        String[] split = expiredKey.split(SipCacheService.SIP_SSRC_KEY);

        String ssrc = split[1];

        log.warn("[ssrc过期未使用]:{}", ssrc);
        this.ssrcManager.releaseSsrc(ssrc);

    }
}
