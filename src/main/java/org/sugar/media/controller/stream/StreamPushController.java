package org.sugar.media.controller.stream;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.beans.stream.StreamPushBean;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.MonitorNetworkService;
import org.sugar.media.service.StreamService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.MediaUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private StreamService streamService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private NodeService nodeService;

    @Resource
    private MonitorNetworkService monitorNetworkService;

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


        streamPushBeans = streamPushBeans.stream().peek((streamPushBean -> {


            streamPushBean.setStatus(StatusEnum.offline);

            if (streamPushBean.getTypes().equals(AppEnum.rtp)) {
                streamPushBean.setSecret(this.streamService.getStreamCode(streamPushBean.getRelevanceId(), AppEnum.rtp.toString()));
            }

            if (streamPushBean.getTypes().equals(AppEnum.live)) {
                streamPushBean.setSecret(this.streamService.getStreamCode(streamPushBean.getId(), AppEnum.live.toString()));
            }

            Optional<StreamInfoBean> streamInfo = mediaListAll.stream().filter(s -> s.getApp().equals(streamPushBean.getApp())).filter(s -> s.getStream().equals(streamPushBean.getStream())).filter(s -> s.getNodeId().equals(streamPushBean.getNodeId())).findFirst();

            if (streamInfo.isPresent()) {
                streamPushBean.setStatus(StatusEnum.online);
                streamPushBean.setTotalReaderCount(streamInfo.get().getTotalReaderCount());
                streamPushBean.setAliveSecond(streamInfo.get().getAliveSecond());
                streamPushBean.setBytesSpeed(this.monitorNetworkService.formatByte(streamInfo.get().getBytesSpeed()));

            }


        })).toList();


        return ResponseEntity.ok(ResponseBean.success(mStreamPushList.getTotalElements(), streamPushBeans));


    }


    @GetMapping("/addr/{id}")
    public ResponseEntity<?> genAddr(@PathVariable("id") Long id) {


        Map<String, List<String>> pushStreamAddr = this.streamService.getPushStreamAddr(id);

        if (ObjectUtil.isEmpty(pushStreamAddr)) return ResponseEntity.ok(ResponseBean.fail());


        return ResponseEntity.ok(ResponseBean.success(pushStreamAddr));

    }


    @GetMapping("/{pushId}")
    public ResponseEntity<?> getInfo(@PathVariable Long pushId) {
        Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(pushId);
        return streamPush.<ResponseEntity<?>>map(streamPushModel -> ResponseEntity.ok(ResponseBean.success(streamPushModel))).orElseGet(() -> ResponseEntity.ok(ResponseBean.fail()));


    }


    @PostMapping("/close/{pushId}")
    public ResponseEntity<?> sendBy(@PathVariable Long pushId) {
        Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(pushId);
        if (streamPush.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        Optional<NodeModel> node = this.nodeService.getNode(streamPush.get().getNodeId());
        node.ifPresent(nodeModel -> this.zlmApiService.closeSteam(streamPush.get().getApp(), streamPush.get().getStream(), nodeModel));


        return ResponseEntity.ok(ResponseBean.success());
    }


    @DeleteMapping("/{pushId}")
    public ResponseEntity<?> deletePushStream(@PathVariable Long pushId) {
        Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(pushId);
        if (streamPush.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        this.streamPushService.deletePushStream(streamPush.get());
        return ResponseEntity.ok(ResponseBean.success());
    }


    @PutMapping("/{pushId}")
    public ResponseEntity<?> updatePushStream(@PathVariable Long pushId, @RequestParam String name) {
        Optional<StreamPushModel> streamPush = this.streamPushService.getStreamPush(pushId);
        if (streamPush.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        streamPush.get().setName(name);


        this.streamPushService.updatePushStream(streamPush.get());
        return ResponseEntity.ok(ResponseBean.success());
    }
}
