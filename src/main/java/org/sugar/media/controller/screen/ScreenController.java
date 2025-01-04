package org.sugar.media.controller.screen;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.screen.ScreenBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.sipserver.manager.SipCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Date:2025/01/04 14:37:15
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/screen")
@Validated
public class ScreenController {


    @Resource
    private DeviceService deviceService;

    @Resource
    private UserSecurity userSecurity;


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


    @GetMapping("/list")
    public ResponseEntity<?> getList() {


        Long tenantId = this.userSecurity.getCurrentTenantId();

        List<ScreenBean> screenBeanList = new ArrayList<>();
        // 国标树
        List<DeviceModel> deviceList = this.deviceService.getDeviceList(tenantId);

        List<StreamPushModel> pushModels = this.streamPushService.findAllByTenantId(tenantId);

        // 过滤国标流
        Map<Long, StreamPushModel> pushModelMap = pushModels.stream().filter(s -> s.getRelevanceId() != null).collect(Collectors.toMap(StreamPushModel::getRelevanceId, s -> s));

        List<StreamInfoBean> mediaListAll = this.zlmNodeService.getMediaListAll();


        Map<String, StreamInfoBean> streamInfoBeanMap = mediaListAll.stream().collect(Collectors.toMap(s -> StrUtil.format("{}{}{}", s.getApp(), s.getStream(), s.getNodeId()), s -> s));
        // 根节点
        for (DeviceModel deviceModel : deviceList) {
            ScreenBean screenBean = new ScreenBean();
            screenBean.setName(deviceModel.getName());
            screenBean.setTypes(AppEnum.rtp);
            screenBean.setStatus(this.sipCacheService.isOnline(deviceModel.getDeviceId()) ? StatusEnum.online : StatusEnum.offline);
            screenBean.setId(deviceModel.getId());
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

            // 查找流
//            StreamPushModel streamPushModel = pushModelMap.get(deviceChannelModel.getId());
//
//            if (ObjectUtil.isNotEmpty(streamPushModel)) {
//
//                StreamInfoBean streamInfoBean = streamInfoBeanMap.get(StrUtil.format("{}{}{}", streamPushModel.getApp(), streamPushModel.getStream(), streamPushModel.getNodeId()));
//
//                if (ObjectUtil.isNotEmpty(streamInfoBean)) {
//
//                    screenBean.setStatus(StatusEnum.online);
//                }
//            }

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

                StreamInfoBean streamInfoBean = streamInfoBeanMap.get(StrUtil.format("{}{}{}", pushModel.getApp(), pushModel.getStream(), pushModel.getNodeId()));

                if (ObjectUtil.isNotEmpty(streamInfoBean)) {

                    screenBean.setStatus(StatusEnum.online);
                }
                screenBeanList.add(screenBean);

            }
        }

        return ResponseEntity.ok(ResponseBean.success(screenBeanList));
    }

}
