package org.sugar.media.controller.node;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.ZlmRemoteConfigBean;
import org.sugar.media.beans.node.NodeBean;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.enums.SyncEnum;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.validation.NodeVal;
import org.sugar.media.validation.ZlmNodeVal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/node/zlm")
@Validated
public class ZlmNodeController {

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private UserSecurity userSecurity;


    @Resource
    private MediaCacheService mediaCacheService;

    @Resource
    private ZlmApiService zlmApiService;

    /**
     * create node
     *
     * @param nodeVal
     * @return
     */
    @PostMapping
    public ResponseEntity<?> createNode(@RequestBody @Validated({NodeVal.Create.class}) NodeVal nodeVal) {

        NodeModel nodeModel = new NodeModel();
        BeanUtil.copyProperties(nodeVal, nodeModel);
        nodeModel.setTypes(MediaServerEnum.zlm);


        this.zlmNodeService.createMediaAsync(nodeModel);



        return ResponseEntity.ok(ResponseBean.success("添加成功"));
    }

    @PutMapping
    public ResponseEntity<?> updateNode(@RequestBody @Validated({NodeVal.Update.class}) NodeVal nodeVal) {

        Optional<NodeModel> node = this.zlmNodeService.getNode(nodeVal.getId());

        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());


        NodeModel nodeModel = node.get();
        BeanUtil.copyProperties(nodeVal, nodeModel);
        this.zlmNodeService.updateMediaAsync(nodeModel, SyncEnum.hook);


        return ResponseEntity.ok(ResponseBean.success("修改成功"));
    }

    @PutMapping("/advance")
    public ResponseEntity<?> updateAdvance(@RequestBody @Validated() ZlmNodeVal nodeVal) {

        Optional<NodeModel> node = this.zlmNodeService.getNode(nodeVal.getId());

        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());


        NodeModel nodeModel = node.get();
        BeanUtil.copyProperties(nodeVal, nodeModel);
        this.zlmNodeService.updateMediaAsync(nodeModel, SyncEnum.base);

        return ResponseEntity.ok(ResponseBean.success("修改成功"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNode(@PathVariable Long id) {


        this.zlmNodeService.deleteNode(id);
        this.mediaCacheService.removeMediaStatus(id);
        return ResponseEntity.ok(ResponseBean.success("删除成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNode(@PathVariable  @NotNull(message = "ID不能为空") Long id) {

        Optional<NodeModel> node = this.zlmNodeService.getNode(id);

        return node.map(nodeModel -> ResponseEntity.ok(ResponseBean.success(nodeModel))).orElseGet(() -> ResponseEntity.ok(ResponseBean.fail("节点不存在")));
    }

    // 获取节点远程配置
    @GetMapping("/remote/{id}")
    public ResponseEntity<?> getRemoteNode(@PathVariable  @NotNull(message = "ID不能为空") Long id) {

        Optional<NodeModel> node = this.zlmNodeService.getNode(id);

        if(node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail("节点不存在"));

        // 判定是否在线
        boolean online = this.mediaCacheService.isOnline(node.get().getId());
        if (!online) return ResponseEntity.ok(ResponseBean.fail("节点不在线"));
        // 获取配置

        ZlmRemoteConfigBean serverConfig = this.zlmApiService.getServerConfig(node.get());

        if(ObjectUtil.isEmpty(serverConfig)) return ResponseEntity.ok(ResponseBean.fail("请检查流媒体服务是否正常"));



        return  ResponseEntity.ok(ResponseBean.success(serverConfig.getData()));
    }



    @PutMapping("/sync/{id}")
    public ResponseEntity<?> syncConfig(@PathVariable @NotNull(message = "ID不能为空") Long id) {

        NodeModel nodeModel = this.zlmNodeService.getNode(id).get();

        boolean written = this.zlmNodeService.syncAll(nodeModel);
        if (!written) return ResponseEntity.ok(ResponseBean.fail("同步失败"));

        return ResponseEntity.ok(ResponseBean.success("同步成功"));
    }

    @PostMapping("/restart/{id}")
    public ResponseEntity<?> restartZlm(@PathVariable @NotNull(message = "ID不能为空") Long id) {

        NodeModel nodeModel = this.zlmNodeService.getNode(id).get();


        CommonBean commonBean = this.zlmApiService.restartServer(nodeModel);


        if(ObjectUtil.isEmpty(commonBean)) return ResponseEntity.ok(ResponseBean.fail("重启失败"));



        return ResponseEntity.ok(ResponseBean.success(commonBean));
    }



    @GetMapping("/list")
    public ResponseEntity<?> getNodeList() {

        List<NodeModel> nodeList = this.zlmNodeService.getNodeList();

        List<NodeBean> list = BeanConverterUtil.convertList(nodeList, NodeBean.class);

        list = list.stream().peek(nodeBean -> nodeBean.setOnline(this.mediaCacheService.isOnline(nodeBean.getId()))).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBean.success(list));

    }


}
