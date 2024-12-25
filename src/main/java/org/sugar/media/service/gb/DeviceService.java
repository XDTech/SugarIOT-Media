package org.sugar.media.service.gb;

import cn.hutool.core.bean.BeanUtil;
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
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.repository.gb.DeviceRepo;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.manager.SsrcManager;
import org.sugar.media.utils.BeanConverterUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.sugar.media.sipserver.manager.SipCacheService.*;

/**
 * Date:2024/12/13 10:53:08
 * Author：Tobin
 * Description:
 */

@Service
public class DeviceService {

    @Resource
    private DeviceRepo deviceRepo;

    @Resource
    private ChannelService channelService;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private SsrcManager ssrcManager;

    public List<DeviceModel> getDeviceList() {
        return this.deviceRepo.findAll();
    }

    @Transactional
    public void createDevice(DeviceModel deviceModel) {

        this.deviceRepo.save(deviceModel);
        // 同步更新缓存数据
        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(deviceModel, deviceBean);
        this.sipCacheService.setSipDevice(deviceModel.getDeviceId(), deviceBean);

    }


    @Transactional
    public void deleteDevice(DeviceModel deviceModel) {

        // 首先移除缓存
        this.sipCacheService.deleteDevice(deviceModel.getDeviceId());
        this.sipCacheService.delSipDevice(deviceModel.getDeviceId());
        this.channelService.deleteAll(deviceModel.getId());
        this.deviceRepo.delete(deviceModel);
    }


    @Transactional
    public void updateDevice(DeviceModel deviceModel) {
        this.deviceRepo.save(deviceModel);
    }

    public DeviceModel getDevice(String deviceCode) {
        return this.deviceRepo.findAllByDeviceId(deviceCode);
    }
    public List<DeviceModel> getDeviceList(Long tenantId) {
        return this.deviceRepo.findAllByTenantId(tenantId);
    }


    public Optional<DeviceModel> getDevice(Long deviceId) {
        return this.deviceRepo.findById(deviceId);
    }


    // 分页查询
    public Page<DeviceModel> getDevicePageList(Integer pi, Integer ps, String name, Long tenantId) {
        // Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pi - 1, ps);
        Specification<DeviceModel> specification = (Root<DeviceModel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

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
        return this.deviceRepo.findAll(specification, pageRequest);

    }

    public void write2cache() {


        // 先删除所有键
        this.sipCacheService.deleteKeysWithPrefixUsingScan(sip_device_keepalive_PREFIX);
        this.sipCacheService.deleteKeysWithPrefixUsingScan(SIP_DEVICE_KEY);
        this.sipCacheService.deleteKeysWithPrefixUsingScan(deviceCSEQ_PREFIX);


        List<DeviceModel> deviceList = this.getDeviceList();
        if (deviceList.isEmpty()) return;

        List<DeviceBean> deviceBeans = BeanConverterUtil.convertList(deviceList, DeviceBean.class);

        for (DeviceBean bean : deviceBeans) {
            // 每次重启 都重置一下cseq
            this.sipCacheService.resetCSeqFromRedis(bean.getDeviceId());

            // 设备写入缓存
            this.sipCacheService.setSipDevice(bean.getDeviceId(), bean);

            // 保活写入缓存
            this.sipCacheService.setDeviceStatus(bean.getDeviceId(), StatusEnum.offline.getStatus());
        }


    }

    /**
     * 6位租户编码+0000+7位流水号
     *
     * @param tenantCode
     * @param deviceId
     * @return
     */
    public String createDeviceCode(Integer tenantCode, String deviceId) {
        return StrUtil.format("{}0000{}", tenantCode, deviceId);
    }
}
