package org.sugar.media.service.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.repository.gb.DeviceRepo;
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.utils.BeanConverterUtil;

import java.util.List;

import static org.sugar.media.sipserver.utils.SipCacheService.*;

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
    private SipCacheService sipCacheService;

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
    public void updateDevice(DeviceModel deviceModel) {
        this.deviceRepo.save(deviceModel);
    }

    public DeviceModel getDevice(String deviceId) {
        return this.deviceRepo.findAllByDeviceId(deviceId);
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
            StaticLog.warn("状态：{}", StatusEnum.offline.getStatus());
            this.sipCacheService.setDeviceStatus(bean.getDeviceId(), StatusEnum.offline.getStatus());
        }


    }
}
