package org.sugar.media.controller.node;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.enums.MediaServerEnum;
import org.sugar.media.enums.ResponseEnum;
import org.sugar.media.model.UserModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.validation.NodeVal;
import org.sugar.media.validation.UserVal;

@RestController
@RequestMapping("/node")
@Validated
public class NodeController {

    @Resource
    private NodeService nodeService;

    @Resource
    private UserSecurity userSecurity;

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
        nodeModel.setTypes(MediaServerEnum.valueOf(nodeVal.getTypes()));
        nodeModel.setZid(this.userSecurity.getCurrentAdminUser().getZid());



        boolean sync = this.nodeService.createMediaSync(nodeModel,true);

        if (!sync) return ResponseEntity.ok(ResponseBean.custom(ResponseEnum.custom, "添加成功，但流媒体配置自动同步失败，请自行同步"));


        return ResponseEntity.ok(ResponseBean.success("添加成功，且流媒体配置同步成功"));
    }


}
