package org.sugar.media.controller.stream;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.MediaCacheService;
import org.sugar.media.service.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.validation.stream.StreamPullVal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * (MStreamPull)表控制层
 *
 * @author Tobin
 * @since 2024-11-27 12:42:56
 */
@RestController
@RequestMapping("/stream/pull")
@Validated
public class StreamPullController {
    /**
     * 服务对象
     */
    @Resource
    private StreamPullService mStreamPullService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private ZlmNodeService zlmNodeService;

    @Resource
    private NodeService nodeService;

    @Resource
    private ZlmApiService zlmApiService;

    @Resource
    private MediaCacheService mediaCacheService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @GetMapping("/page/list")
    public ResponseEntity<?> getMStreamPullPageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<StreamPullModel> mStreamPullList = this.mStreamPullService.getMStreamPullPageList(pi, ps, name, this.userSecurity.getCurrentZid());

        List<StreamPullBean> streamPullBeans = BeanConverterUtil.convertList(mStreamPullList.getContent(), StreamPullBean.class);


        streamPullBeans = streamPullBeans.stream().peek((streamPullBean -> {
            Optional<NodeModel> node = this.zlmNodeService.getNode(streamPullBean.getNodeId());
            streamPullBean.setStatus("1");
            node.ifPresent(nodeModel -> {

                streamPullBean.setNodeName(nodeModel.getName());

                if (this.mediaCacheService.isOnline(nodeModel.getId())) {
                    // 加载流状态
                    StreamProxyInfoBean streamProxyInfo = this.zlmApiService.getStreamProxyInfo(streamPullBean.getStreamKey(), node.get());
                    if(streamProxyInfo.getCode()==0){
                        StaticLog.info("{}",Convert.toStr(streamProxyInfo.toString()));
                        streamPullBean.setStatus(Convert.toStr(streamProxyInfo.getData().getStatus()));
                    }
                }
            });
        })).collect(Collectors.toList());


        return ResponseEntity.ok(ResponseBean.success(mStreamPullList.getTotalElements(), streamPullBeans));


    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMStreamPull(@PathVariable("id") Long id) {

        Optional<StreamPullModel> mStreamPull = this.mStreamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("数据不存在");
        }
        StreamPullBean streamPullBean = new StreamPullBean();
        BeanUtil.copyProperties(mStreamPull.get(), streamPullBean);
        return ResponseEntity.ok(ResponseBean.success(streamPullBean));

    }

    /**
     * 新增数据
     *
     * @param mStreamPullBean 实体
     * @return 新增结果
     */


    @PostMapping
    public ResponseEntity<?> createMStreamPull(@RequestBody @Validated StreamPullVal mStreamPullBean) {


        StreamPullModel streamPullModel = this.mStreamPullService.onlyStream(this.userSecurity.getCurrentZid(), mStreamPullBean.getApp(), mStreamPullBean.getStream());


        if (ObjectUtil.isNotEmpty(streamPullModel)) {
            return ResponseEntity.ok(ResponseBean.fail("App+Stream重复"));
        }

        StreamPullModel mStreamPull = new StreamPullModel();
        BeanUtil.copyProperties(mStreamPullBean, mStreamPull);

        mStreamPull.setZid(this.userSecurity.getCurrentZid());

        CommonBean commonBean = this.mStreamPullService.autoPullStream(mStreamPull);

        return ResponseEntity.ok(ResponseBean.createResponseBean(commonBean.getCode(), commonBean.getMsg()));


    }

    /**
     * 编辑数据
     *
     * @param mStreamPullBean 实体
     * @return 编辑结果
     */

    @PutMapping
    public ResponseEntity<?> updateMStreamPull(@RequestBody @Validated(StreamPullVal.Update.class) StreamPullVal mStreamPullBean) {

        Optional<StreamPullModel> mStreamPullOptional = this.mStreamPullService.getMStreamPull(mStreamPullBean.getId());
        if (mStreamPullOptional.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }

        // 判断是否存在

        StreamPullModel streamPullModel = this.mStreamPullService.onlyStream(this.userSecurity.getCurrentZid(), mStreamPullBean.getApp(), mStreamPullBean.getStream());


        if (ObjectUtil.isNotEmpty(streamPullModel) && !streamPullModel.getId().equals(mStreamPullBean.getId())) {
            return ResponseEntity.ok(ResponseBean.fail("App+Stream重复"));
        }

        StreamPullModel mStreamPull = mStreamPullOptional.get();


        BeanUtil.copyProperties(mStreamPullBean, mStreamPull, "createdAt");


        this.mStreamPullService.updateMStreamPull(mStreamPull);
        return ResponseEntity.ok(ResponseBean.success());

    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMStreamPullById(@PathVariable("id") Long id) {

        Optional<StreamPullModel> mStreamPull = this.mStreamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }

        this.mStreamPullService.deleteMStreamPull(id);
        return ResponseEntity.ok(ResponseBean.success());
    }


    @PostMapping("/proxy/{streamPullId}")
    public ResponseEntity<?> getStreamPlayerNode(@PathVariable("streamPullId") Long id) {

        Optional<StreamPullModel> mStreamPull = this.mStreamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }


        CommonBean commonBean = this.mStreamPullService.playStreamPull(mStreamPull.get());

        StaticLog.info("{}", commonBean.toString());
        if (commonBean.getCode().equals(0)) {
            mStreamPull.get().setNodeId(commonBean.getNodeId());
            mStreamPull.get().setStreamKey(Convert.toStr(commonBean.getData().get("key")));
            this.mStreamPullService.updateMStreamPull(mStreamPull.get());
        }

        return ResponseEntity.ok(ResponseBean.success());
    }

    @DeleteMapping("/close/proxy/{streamPullId}")
    public ResponseEntity<?> delStreamProxy(@PathVariable("streamPullId") Long id) {

        Optional<StreamPullModel> mStreamPull = this.mStreamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }
        // 获取节点
        if (mStreamPull.get().getNodeId() == null) {
            return ResponseEntity.ok(ResponseBean.fail("没有分配节点"));
        }

        Optional<NodeModel> node = this.nodeService.getNode(mStreamPull.get().getNodeId());
        if (node.isEmpty()) return ResponseEntity.ok(ResponseBean.fail("节点不存在"));

        CommonBean commonBean = this.zlmApiService.closeStreamProxy(mStreamPull.get(), node.get());


        if (commonBean.getCode().equals(0) && Convert.toBool(commonBean.getData().get("flag"))) {
            return ResponseEntity.ok(ResponseBean.success());
        }

        return ResponseEntity.ok(ResponseBean.fail("断开失败，请检查流是否存在"));
    }

}

