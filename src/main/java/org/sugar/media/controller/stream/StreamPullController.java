package org.sugar.media.controller.stream;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.StreamService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.utils.MediaUtil;
import org.sugar.media.validation.stream.StreamPullVal;

import java.util.List;
import java.util.Map;
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

    @Resource
    private MediaUtil mediaUtil;

    @Resource
    private StreamService streamService;


    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @GetMapping("/page/list")
    public ResponseEntity<?> getMStreamPullPageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<StreamPullModel> mStreamPullList = this.mStreamPullService.getMStreamPullPageList(pi, ps, name, this.userSecurity.getCurrentTenantId());

        List<StreamPullBean> streamPullBeans = BeanConverterUtil.convertList(mStreamPullList.getContent(), StreamPullBean.class);


        streamPullBeans = streamPullBeans.stream().peek((streamPullBean -> {
            streamPullBean.setStatus("1");
            if (streamPullBean.getNodeId() != null) {
                Optional<NodeModel> node = this.zlmNodeService.getNode(streamPullBean.getNodeId());
                node.ifPresent(nodeModel -> {
                    streamPullBean.setNodeName(nodeModel.getName());
                    streamPullBean.setSecret(this.streamService.getStreamCode(streamPullBean.getId(), streamPullBean.getApp()));
                    if (this.mediaCacheService.isOnline(nodeModel.getId())) {
                        // 加载流状态
                        StreamProxyInfoBean streamProxyInfo = this.zlmApiService.getStreamProxyInfo(streamPullBean.getStreamKey(), node.get());
                        if (streamProxyInfo.getCode() == 0) {
                            streamPullBean.setStatus(Convert.toStr(streamProxyInfo.getData().getStatus()));
                        }
                    }
                });
            }

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
            return ResponseEntity.ok(ResponseBean.fail("数据不存在"));
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


        mStreamPullBean.setStream(this.mediaUtil.genStreamId(mStreamPullBean.getStream()));

        StreamPullModel streamPullModel = this.mStreamPullService.onlyStream(mStreamPullBean.getApp(), mStreamPullBean.getStream());


        if (ObjectUtil.isNotEmpty(streamPullModel)) {
            return ResponseEntity.ok(ResponseBean.fail("Stream重复"));
        }

        StreamPullModel mStreamPull = new StreamPullModel();
        BeanUtil.copyProperties(mStreamPullBean, mStreamPull);

        mStreamPull.setTenantId(this.userSecurity.getCurrentTenantId());

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
        mStreamPullBean.setStream(this.mediaUtil.genStreamId(mStreamPullBean.getStream()));
        StreamPullModel streamPullModel = this.mStreamPullService.onlyStream(mStreamPullBean.getApp(), mStreamPullBean.getStream());

        if (ObjectUtil.isNotEmpty(streamPullModel) && !streamPullModel.getId().equals(mStreamPullBean.getId())) {
            return ResponseEntity.ok(ResponseBean.fail("Stream重复"));
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

        ThreadUtil.execute(() -> {
            if (mStreamPull.get().getNodeId() != null) {
                Optional<NodeModel> node = this.nodeService.getNode(mStreamPull.get().getNodeId());
                node.ifPresent(nodeModel -> this.zlmApiService.closeStreamProxy(mStreamPull.get(), nodeModel));
            }
        });
        return ResponseEntity.ok(ResponseBean.success());
    }


    /**
     * 手动拉流代理
     *
     * @param id
     * @return
     */
    @PostMapping("/proxy/{streamPullId}")
    public ResponseEntity<?> getStreamPlayerNode(@PathVariable("streamPullId") Long id) {

        Optional<StreamPullModel> mStreamPull = this.mStreamPullService.getMStreamPull(id);
        if (mStreamPull.isEmpty()) {
            return ResponseEntity.ok(ResponseBean.fail());
        }

        CommonBean commonBean = this.mStreamPullService.manualPullStream(mStreamPull.get());

        return ResponseEntity.ok(ResponseBean.createResponseBean(commonBean.getCode(), commonBean.getMsg()));
    }


    /**
     * 获取拉流代理播放地址
     * : 在此处应该指定节点
     *
     * @param id
     * @return
     */
    @GetMapping("/proxy/address/{streamPullId}")
    public ResponseEntity<?> getStreamPlayerAddress(@PathVariable("streamPullId") Long id) {

        Map<String, List<String>> pullStreamAddr = this.streamService.getPullStreamAddr(id);


        if (ObjectUtil.isNotEmpty(pullStreamAddr)) return ResponseEntity.ok(ResponseBean.fail("暂无播放地址"));

        return ResponseEntity.ok(ResponseBean.success(pullStreamAddr));
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


            this.mStreamPullService.resetStream(mStreamPull.get());
            return ResponseEntity.ok(ResponseBean.success());
        }


        return ResponseEntity.ok(ResponseBean.fail("断开失败，请检查流是否存在"));
    }


}

