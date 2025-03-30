package org.sugar.media.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.screen.ScreenBean;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.utils.AesUtil;
import org.sugar.media.utils.MediaUtil;
import org.sugar.media.utils.SecurityUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Date:2025/03/30 11:07:51
 * Author：Tobin
 * Description:
 */

@Service
public class StreamService {


    @Resource
    private DeviceService deviceService;
    @Resource
    private ChannelService channelService;

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private StreamPushService streamPushService;

    @Resource
    private StreamPullService streamPullService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private NodeService nodeService;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private MediaCacheService mediaCacheService;

    @Resource
    private MediaUtil mediaUtil;
    private SecurityUtils securityUtils = new SecurityUtils();


    public List<ScreenBean> getOnlineStreamList(Long tenantId) {
        List<ScreenBean> screenBeanList = new ArrayList<>();
        // 国标树
        List<DeviceModel> deviceList = this.deviceService.getDeviceList(tenantId);

        List<StreamPushModel> pushModels = this.streamPushService.findAllByTenantId(tenantId);

        // 过滤国标流
//        Map<Long, StreamPushModel> pushModelMap = pushModels.stream().filter(s -> s.getRelevanceId() != null).collect(Collectors.toMap(StreamPushModel::getRelevanceId, s -> s));

        List<StreamInfoBean> mediaListAll = this.zlmNodeService.getMediaListAll();


        Map<String, StreamInfoBean> streamInfoBeanMap = mediaListAll.stream().collect(Collectors.toMap(s -> StrUtil.format("{}{}{}", s.getApp(), s.getStream(), s.getNodeId()), s -> s));
        // 根节点
        for (DeviceModel deviceModel : deviceList) {
            ScreenBean screenBean = new ScreenBean();
            screenBean.setName(deviceModel.getName());
            screenBean.setTypes(AppEnum.rtp);
            screenBean.setStatus(this.sipCacheService.isOnline(deviceModel.getDeviceId()) ? StatusEnum.online : StatusEnum.offline);
            screenBean.setId(deviceModel.getId());
            screenBean.setDeviceCode(deviceModel.getDeviceId());
            screenBean.setTenantId(deviceModel.getTenantId());


            screenBeanList.add(screenBean);
        }


        // 通道列表
        List<DeviceChannelModel> channelList = this.channelService.getChannelList(tenantId);

        for (DeviceChannelModel deviceChannelModel : channelList) {
            ScreenBean screenBean = new ScreenBean();
            screenBean.setParentId(deviceChannelModel.getDeviceId());
            screenBean.setName(deviceChannelModel.getChannelName());
            screenBean.setStatus(deviceChannelModel.getStatus());
            screenBean.setTypes(AppEnum.rtp);
            screenBean.setId(deviceChannelModel.getId());
            screenBean.setNodeType("1");

            Optional<ScreenBean> beanOptional = screenBeanList.stream().filter(s -> s.getId().equals(deviceChannelModel.getDeviceId())).findFirst();
            beanOptional.ifPresent(bean -> screenBean.setDeviceCode(bean.getDeviceCode()));
            screenBean.setChannelCode(deviceChannelModel.getChannelCode());

            screenBean.setApp("rtp");
            screenBean.setStream(screenBean.getDeviceCode() + "_" + deviceChannelModel.getChannelCode());
            screenBean.setTenantId(deviceChannelModel.getTenantId());

            screenBean.setSecret(this.getStreamCode(deviceChannelModel.getId(), AppEnum.rtp.toString()));
            screenBeanList.add(screenBean);


        }


        // 拉流代理列表
        List<StreamPullModel> streamPullModels = this.streamPullService.streamPushList(tenantId);

        for (StreamPullModel stream : streamPullModels) {

            ScreenBean screenBean = new ScreenBean();
            screenBean.setTypes(AppEnum.proxy);
            screenBean.setId(stream.getId());
            screenBean.setName(stream.getName());
            screenBean.setStatus(StatusEnum.offline);
            screenBean.setNodeType("1");
            screenBean.setApp(stream.getApp());
            screenBean.setStream(stream.getStream());
            screenBean.setSecret(this.getStreamCode(stream.getId(), AppEnum.proxy.toString()));
            screenBean.setTenantId(stream.getTenantId());
            if (stream.getNodeId() != null) {
                Optional<NodeModel> node = this.nodeService.getNode(stream.getNodeId());

                if (node.isEmpty()) continue;

                StreamProxyInfoBean streamProxyInfo = this.zlmApiService.getStreamProxyInfo(stream.getStreamKey(), node.get());
                if (streamProxyInfo.getCode() == 0) {
                    screenBean.setStatus(streamProxyInfo.getData().getStatus() == 0 ? StatusEnum.online : StatusEnum.offline);
                }
            }

            screenBeanList.add(screenBean);

        }


        // 直播流
        for (StreamPushModel pushModel : pushModels) {

            if (pushModel.getApp().equals(AppEnum.live.toString())) {
                ScreenBean screenBean = new ScreenBean();
                screenBean.setName(pushModel.getName());
                screenBean.setStatus(StatusEnum.offline);
                screenBean.setId(pushModel.getId());
                screenBean.setNodeType("1");
                screenBean.setTypes(AppEnum.live);
                screenBean.setApp(pushModel.getApp());
                screenBean.setStream(pushModel.getStream());
                screenBean.setTenantId(pushModel.getTenantId());
                screenBean.setSecret(this.getStreamCode(pushModel.getId(), AppEnum.live.toString()));
                StreamInfoBean streamInfoBean = streamInfoBeanMap.get(StrUtil.format("{}{}{}", pushModel.getApp(), pushModel.getStream(), pushModel.getNodeId()));

                if (ObjectUtil.isNotEmpty(streamInfoBean)) {

                    screenBean.setStatus(StatusEnum.online);
                }
                screenBeanList.add(screenBean);

            }
        }

        return screenBeanList;
    }


    // 生成鉴权码

    /**
     * {
     * id:"cameraId"
     * types:"rtp/live..."
     * }
     */

    public String getStreamCode(Long id, String types) {

        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("types", types);

        return AesUtil.aesEncrypt(JSONUtil.toJsonStr(map));

    }


    public Map<String, List<String>> getPullStreamAddr(Long id) {

        Optional<StreamPullModel> mStreamPull = this.streamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return null;
        }


        Optional<NodeModel> playerNode = this.streamPullService.getPlayerNode(mStreamPull.get());


        if (playerNode.isEmpty()) return null;


        // 在线-->获取地址
        Map<String, List<String>> nodePlayerUrl = this.nodeService.createNodePlayerUrl(mStreamPull.get(), playerNode.get());


        if (nodePlayerUrl.isEmpty()) return null;


        return nodePlayerUrl;

    }

    public Map<String, List<String>> getPushStreamAddr(Long id){

        Optional<StreamPushModel> streamPushModel = this.streamPushService.getStreamPush(id);
        if (streamPushModel.isEmpty()) {
            return null;
        }

        Optional<NodeModel> node = this.nodeService.getNode(streamPushModel.get().getNodeId());

        if (node.isEmpty()) return null;

        if (!this.mediaCacheService.isOnline(node.get().getId()))
            return null;


        return this.mediaUtil.genAddr(node.get(), streamPushModel.get().getApp(), streamPushModel.get().getStream());

    }
}
