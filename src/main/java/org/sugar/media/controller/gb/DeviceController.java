package org.sugar.media.controller.gb;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.enums.DeviceTypeEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.sipserver.utils.SipCacheService;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.validation.gb.DeviceRegisterVal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Date:2024/12/14 17:24:17
 * Author：Tobin
 * Description: 国标设备
 */

@RestController
@RequestMapping("/gb/device")
@Validated
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private SipCacheService sipCacheService;

    @GetMapping("/page/list")
    public ResponseEntity<?> getDevicePageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<DeviceModel> devicePageList = this.deviceService.getDevicePageList(pi, ps, name, this.userSecurity.getCurrentTenantId());

        List<DeviceBean> deviceBeans = BeanConverterUtil.convertList(devicePageList.getContent(), DeviceBean.class);


        deviceBeans = deviceBeans.stream().peek((deviceBean -> {
            deviceBean.setStatus(this.sipCacheService.isOnline(deviceBean.getDeviceId()) ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());
        })).toList();

        return ResponseEntity.ok(ResponseBean.success(devicePageList.getTotalElements(), deviceBeans));

    }

    /**
     * 用户注册国标设备 基本信息
     *
     * @param deviceRegisterVal
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(@RequestBody @Validated DeviceRegisterVal deviceRegisterVal) {

        Integer tenantCode = this.userSecurity.getCurrentTenantCode();
        String code = StrUtil.format("{}0000{}", tenantCode, deviceRegisterVal.getDeviceId());
        // 查询国标设备是否存在
        DeviceModel device = this.deviceService.getDevice(code);

        if (ObjectUtil.isNotEmpty(device)) {
            return ResponseEntity.ok(ResponseBean.fail("国标编码已存在"));
        }

        DeviceModel deviceModel = new DeviceModel();

        deviceModel.setName(deviceRegisterVal.getDeviceName());
        deviceModel.setPwd(deviceRegisterVal.getPwd());
        deviceModel.setDeviceId(code);
        deviceModel.setDeviceType(DeviceTypeEnum.valueOf(deviceRegisterVal.getDeviceType()));
        deviceModel.setTenantId(this.userSecurity.getCurrentTenantId());
        this.deviceService.createDevice(deviceModel);

        return ResponseEntity.ok(ResponseBean.success());

    }


}
