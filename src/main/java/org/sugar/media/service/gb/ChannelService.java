package org.sugar.media.service.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.gb.SsrcInfoBean;
import org.sugar.media.enums.NetworkEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.gb.ChannelRepo;
import org.sugar.media.repository.gb.DeviceRepo;
import org.sugar.media.service.LoadBalanceService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.utils.JwtUtils;

import java.util.*;

/**
 * Date:2024/12/21 11:41:54
 * Author：Tobin
 * Description:
 */

@Service
public class ChannelService {


    @Resource
    private ChannelRepo channelRepo;

    @Resource
    private NodeService nodeService;

    @Resource
    private SsrcManager ssrcManager;

    @Resource
    private SipRequestSender sipRequestSender;

    @Resource
    private DeviceRepo deviceRepo;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private LoadBalanceService loadBalanceService;


    public List<DeviceChannelModel> getDeviceChannelList(Long deviceId) {

        return this.channelRepo.findByDeviceId(deviceId);


    }

    public List<DeviceChannelModel> getChannelList(Long tenantId) {

        return this.channelRepo.findByTenantId(tenantId);


    }

    public Optional<DeviceChannelModel> getChannel(Long channelId) {

        return this.channelRepo.findById(channelId);


    }

    @Transactional
    public DeviceChannelModel updateChannel(DeviceChannelModel deviceChannelModel) {

        return this.channelRepo.save(deviceChannelModel);


    }

    @Transactional
    public void createChannel(List<DeviceChannelModel> channelModels) {
        this.channelRepo.saveAll(channelModels);

    }


    @Transactional
    public void deleteChannel(DeviceChannelModel channelModel) {


        this.ssrcManager.releaseSsrcByCode(channelModel.getChannelCode());

        this.channelRepo.delete(channelModel);

    }


    @Transactional
    public void deleteAll(Long deviceId) {

//        List<DeviceChannelModel> deviceChannelList = this.getDeviceChannelList(deviceId);
//        for (DeviceChannelModel deviceChannelModel : deviceChannelList) {
//            this.ssrcManager.releaseSsrcByCode(deviceChannelModel.getChannelCode());
//        }

        this.channelRepo.deleteAllByDeviceId(deviceId);
    }

    public Page<DeviceChannelModel> getChannelPageList(Integer pi, Integer ps, String name, Long tenantId) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<DeviceChannelModel> specification = (Root<DeviceChannelModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            // 用于暂时存放查询条件的集合
            List<Predicate> predicatesList = new ArrayList<>();

            if (!StrUtil.isEmpty(name)) {
                predicatesList.add(cb.like(root.get("channelName"), "%" + name + "%"));
            }
            if (tenantId != null) {
                predicatesList.add(cb.equal(root.get("tenantId"), tenantId));
            }
            // --------------------------------------------
            // 模糊查询
            /**
             if (!StrUtil.isEmpty(username)) {
             predicatesList.add(cb.like(root.get("username"), "%" + username + "%"));
             }
             if (!StrUtil.isEmpty(status)) {
             predicatesList.add(cb.equal(root.get("status"), UserStatusEnum.valueOf(status)));
             }
             **/
            Predicate[] p = new Predicate[predicatesList.size()];
            query.where(predicatesList.toArray(p));
            query.orderBy(cb.desc(root.get("createdAt")));
            return query.getGroupRestriction();

        };
        return this.channelRepo.findAll(specification, pageRequest);

    }

    /**
     * 生成国标播放地址
     *
     * @param nodeModel
     * @return
     */
    public Map<String, List<String>> genAddr(NodeModel nodeModel, String ssrc) {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("type", "gb");
        String token = JwtUtils.createToken(tokenMap);
        Map<String, List<String>> map = new HashMap<>();
        String appStream = StrUtil.format("{}/{}", "rtp", ssrc);
        String host = StrUtil.format("{}:{}", nodeModel.getRemoteIp(), nodeModel.getHttpPort());
        String sslHost = StrUtil.format("{}:{}", nodeModel.getRemoteIp(), nodeModel.getHttpsPort());

        this.nodeService.addFmp4MediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addRtmpMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addTsMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addHlsMediaSource(map, host, sslHost, appStream, token);

        return map;

    }


    public String genGBStream(String deviceCode, String channelCode) {
        return StrUtil.format("{}_{}", deviceCode, channelCode);
    }


    @Transactional
    public void updateChannelStatus(Long deviceId, StatusEnum statusEnum) {

        this.channelRepo.updateStatusByDevice(deviceId, statusEnum);

    }

    // 通过 channel code 发送send by

    public void sendByCode(String channelCode) {
        SsrcInfoBean ssrcInfoBean = this.ssrcManager.getSsrcByCode(channelCode);

        if (ObjectUtil.isNotEmpty(ssrcInfoBean)) {
            this.sipRequestSender.sendBye(ssrcInfoBean);
        }

    }


    public void inviteChannels(List<DeviceChannelModel> channelModels) {
        for (DeviceChannelModel channelModel : channelModels) {
            if (!channelModel.isEnablePull()) continue;
            ThreadUtil.execute(() -> {
                this.inviteChannel(channelModel);
            });
        }

    }


    // 发送invite 生成播放地址
    public Map<String, List<String>> inviteChannel(DeviceChannelModel channel) {

        if (channel.getStatus().equals(StatusEnum.offline)) {
            return null;
        }

        Optional<DeviceModel> device = this.deviceRepo.findById(channel.getDeviceId());
        if (device.isEmpty()) return null;

        if (!this.sipCacheService.isOnline(device.get().getDeviceId())) {
            return null;
        }


        //
        SsrcInfoBean ssrcByCode = this.ssrcManager.getSsrcByCode(channel.getChannelCode());

        if (ObjectUtil.isNotEmpty(ssrcByCode)) {
            Console.log("该设备存在ssrc:{}", ssrcByCode);

            Optional<NodeModel> node = this.nodeService.getNode(ssrcByCode.getNodeId());

            if (node.isPresent()) {
                return this.genAddr(node.get(), StrUtil.format("{}_{}", device.get().getDeviceId(), channel.getChannelCode()));
            }
        }

        NodeModel node = this.loadBalanceService.executeBalance();


        if (ObjectUtil.isEmpty(node)) return null;

        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(device.get(), deviceBean);


        ChannelBean channelBean = new ChannelBean();

        BeanUtil.copyProperties(channel, channelBean);


        deviceBean.setNodeHost(device.get().getNetType().equals(NetworkEnum.private_net) ? node.getIp() : node.getRemoteIp());
        deviceBean.setNodePort(device.get().getNetType().equals(NetworkEnum.private_net) ? node.getRtpPort() : node.getRemoteRtpPort());
        deviceBean.setNodeId(node.getId());

        SsrcInfoBean ssrcInfoBean = new SsrcInfoBean();
        ssrcInfoBean.setDeviceCode(device.get().getDeviceId());
        ssrcInfoBean.setChannelCode(channelBean.getChannelCode());
        ssrcInfoBean.setChannelId(channel.getId());
        ssrcInfoBean.setName(channel.getChannelName());
        ssrcInfoBean.setDeviceHost(device.get().getHost());
        ssrcInfoBean.setDevicePort(device.get().getPort());
        ssrcInfoBean.setNodeId(node.getId());
        ssrcInfoBean.setTenantId(device.get().getTenantId());
        ssrcInfoBean.setTransport(device.get().getTransport());
        ssrcInfoBean.setEnablePull(device.get().isEnablePull());
        ssrcInfoBean.setEnableMp4(device.get().isEnableMp4());
        ssrcInfoBean.setAutoClose(device.get().getAutoClose());

        String playSsrc = this.ssrcManager.createPlaySsrc(ssrcInfoBean);
        ssrcInfoBean.setSsrc(playSsrc);

        this.sipRequestSender.sendInvite(deviceBean, channelBean, playSsrc);

        return this.genAddr(node, this.genGBStream(device.get().getDeviceId(), channel.getChannelCode()));
    }
}
