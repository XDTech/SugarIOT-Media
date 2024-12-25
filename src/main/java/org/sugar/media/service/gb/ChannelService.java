package org.sugar.media.service.gb;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.repository.gb.ChannelRepo;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.utils.JwtUtils;

import java.util.*;

/**
 * Date:2024/12/21 11:41:54
 * Author：Tobin
 * Description:
 */

@Service
public class ChannelService {


    public String channelCodeMid = "0000132";
    @Resource
    private ChannelRepo channelRepo;

    @Resource
    private NodeService nodeService;

    @Resource
    private SsrcManager ssrcManager;


    public List<DeviceChannelModel> getDeviceChannelList(Long deviceId) {

        return this.channelRepo.findByDeviceId(deviceId);


    }

    public Optional<DeviceChannelModel> getChannel(Long channelId) {

        return this.channelRepo.findById(channelId);


    }

    @Transactional
    public void createChannel(List<DeviceChannelModel> channelModels) {
        this.channelRepo.saveAll(channelModels);

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
                predicatesList.add(cb.like(root.get("name"), "%" + name + "%"));
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
        String host = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
        String sslHost = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpsPort());

        this.nodeService.addFmp4MediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addRtmpMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addTsMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addHlsMediaSource(map, host, sslHost, appStream, token);

        return map;

    }


    public String genChannelCode(Integer tenantCode, String code) {
        return StrUtil.format("{}{}{}", tenantCode, this.channelCodeMid, code);
    }

    /**
     * @param tenantCode
     * @param channelCode 设备传过来的20位编码
     * @return 000 0132
     */
    public boolean checkChannelCode(String tenantCode, String channelCode) {

        // 判断长度是否20位
        if (channelCode.length() != 20) {
            return false;
        }
        // 判断前6位租户编码是否相同
        String tenant = channelCode.substring(0, 6);
        if (!tenantCode.equals(tenant)) {
            return false;
        }

        String check = channelCode.substring(6, 13);

        if (!check.equals(this.channelCodeMid)) {
            return false;
        }

        return true;

    }
}
