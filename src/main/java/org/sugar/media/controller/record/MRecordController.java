package org.sugar.media.controller.record;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.record.RecordModel;
import org.sugar.media.beans.MRecordBean;

import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.record.RecordService;
import org.sugar.media.beans.ResponseBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import cn.hutool.core.bean.BeanUtil;

import java.util.Map;
import java.util.Optional;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import org.sugar.media.sipserver.utils.SipConfUtils;
import org.sugar.media.utils.BeanConverterUtil;

/**
 * (MRecord)表控制层
 *
 * @author Tobin
 * @since 2025-03-24 17:33:27
 */
@RestController
@RequestMapping("/record")
@Validated  //单参数校验时我们需要，在方法的类上加上@Validated注解，否则校验不生效。
public class MRecordController {
    /**
     * 服务对象
     */
    @Resource
    private RecordService mRecordService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private NodeService nodeService;

    @Resource
    private MediaCacheService mediaCacheService;



    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @GetMapping("/page/list")
    public ResponseEntity<?> getMRecordPageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) Long startDate, @RequestParam(required = false) Long endDate, @RequestParam(required = false) String app, @RequestParam(required = false) String stream) {

        Long tenantId = this.userSecurity.getCurrentTenantId();
        Page<RecordModel> mRecordList = this.mRecordService.getRecordPageList(pi, ps, startDate, endDate, tenantId, app, stream);


        List<MRecordBean> mRecordBeans = BeanConverterUtil.convertList(mRecordList.getContent(), MRecordBean.class);

        List<NodeModel> nodeAll = this.nodeService.getNodeAll();

        Map<Long, NodeModel> nodeModelMap = nodeAll.stream().collect(Collectors.toMap(NodeModel::getId, s -> s));

        mRecordBeans = mRecordBeans.stream().peek(s -> {
            NodeModel nodeModel = nodeModelMap.get(Convert.toLong(s.getMediaServerId()));
            if (ObjectUtil.isNotEmpty(nodeAll) && this.mediaCacheService.isOnline(nodeModel.getId())) {
                s.setPlayUrl(StrUtil.format("http://{}:{}/{}", nodeModel.getRemoteIp(), nodeModel.getHttpPort(), s.getUrl()));
            }

        }).collect(Collectors.toList());


        return ResponseEntity.ok(ResponseBean.success(mRecordList.getTotalElements(), mRecordBeans));


    }


}

