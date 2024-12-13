package org.sugar.media.service.gb;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.repository.gb.DeviceRepo;
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.utils.BeanConverterUtil;

import java.util.List;

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

    public void createDevice(DeviceModel deviceModel) {

        this.deviceRepo.save(deviceModel);
        // 同步更新缓存数据
        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(deviceModel, deviceBean);
        this.sipCacheService.setSipDevice(deviceModel.getDeviceId(), deviceBean);

    }

    public DeviceModel getDevice(String deviceId) {
        return this.deviceRepo.findAllByDeviceId(deviceId);
    }


    public void write2cache() {


        List<DeviceModel> deviceList = this.getDeviceList();
        if (deviceList.isEmpty()) return;

        List<DeviceBean> deviceBeans = BeanConverterUtil.convertList(deviceList, DeviceBean.class);

        for (DeviceBean bean : deviceBeans) {
            this.sipCacheService.setSipDevice(bean.getDeviceId(), bean);

        }


    }
}
