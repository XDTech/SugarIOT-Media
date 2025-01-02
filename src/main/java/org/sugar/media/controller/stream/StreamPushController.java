package org.sugar.media.controller.stream;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.beans.hooks.zlm.CloseStreamBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.beans.stream.StreamPushBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.MonitorService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.MediaUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Date:2025/01/02 09:09:42
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/stream/push")
@Validated
public class StreamPushController {

    @Resource
    private StreamPushService streamPushService;


    @Resource
    private UserSecurity userSecurity;

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private NodeService nodeService;

    @Resource
    private MonitorService monitorService;

    @Resource
    private MediaUtil mediaUtil;

    @Resource
    private MediaCacheService mediaCacheService;

    @Resource
    private ZlmApiService zlmApiService;


    @GetMapping("/page/list")
    public ResponseEntity<?> getMStreamPushPageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<StreamPushModel> mStreamPushList = this.streamPushService.getMStreamPushPageList(pi, ps, name, this.userSecurity.getCurrentTenantId());

        List<StreamPushBean> streamPushBeans = BeanConverterUtil.convertList(mStreamPushList.getContent(), StreamPushBean.class);

        List<StreamInfoBean> mediaListAll = this.zlmNodeService.getMediaListAll();

        Console.log(mediaListAll.toString());

        streamPushBeans = streamPushBeans.stream().peek((streamPushBean -> {


            streamPushBean.setStatus(StatusEnum.offline);
            Optional<StreamInfoBean> streamInfo = mediaListAll.stream().filter(s -> s.getApp().equals(streamPushBean.getApp())).filter(s -> s.getStream().equals(streamPushBean.getStream())).filter(s -> s.getNodeId().equals(streamPushBean.getNodeId())).findFirst();

            if (streamInfo.isPresent()) {
                streamPushBean.setStatus(StatusEnum.online);
                streamPushBean.setTotalReaderCount(streamInfo.get().getTotalReaderCount());
                streamPushBean.setAliveSecond(streamInfo.get().getAliveSecond());
                streamPushBean.setBytesSpeed(this.monitorService.formatByte(streamInfo.get().getBytesSpeed()));

            }


        })).toList();


        return ResponseEntity.ok(ResponseBean.success(mStreamPushList.getTotalElements(), streamPushBeans));


    }


    @GetMapping("/addr/{id}")
    public ResponseEntity<?> genAddr(@PathVariable("id") Long id) {

        Optional<StreamPushModel> streamPushModel = this.streamPushService.getStreamPush(id);
        if (streamPushModel.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail("数据不存在"));
        }

        Optional<NodeModel> node = this.nodeService.getNode(streamPushModel.get().getNodeId());

        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail("数据不存在"));

        if (!this.mediaCacheService.isOnline(node.get().getId()))
            return ResponseEntity.ok(ResponseBean.fail("播放节点离线"));

        return ResponseEntity.ok(ResponseBean.success(this.mediaUtil.genAddr(node.get(), streamPushModel.get().getApp(), streamPushModel.get().getStream())));

    }


    @PostMapping("/close/{pushId}")
    public ResponseEntity<?> sendBy(@PathVariable Long pushId) {
        Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(pushId);
        if (streamPush.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        Optional<NodeModel> node = this.nodeService.getNode(streamPush.get().getNodeId());
        node.ifPresent(nodeModel -> this.zlmApiService.closeSteam(streamPush.get().getApp(), streamPush.get().getStream(), nodeModel));


        return ResponseEntity.ok(ResponseBean.success());
    }
}
