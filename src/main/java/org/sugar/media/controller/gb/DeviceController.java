package org.sugar.media.controller.gb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.beans.gb.ChannelBean;
import org.sugar.media.beans.gb.DeviceBean;
import org.sugar.media.enums.DeviceTypeEnum;
import org.sugar.media.enums.SocketMsgEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.server.WebSocketServer;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.sipserver.sender.SipRequestSender;
import org.sugar.media.sipserver.manager.SipCacheService;
import org.sugar.media.sipserver.utils.SipConfUtils;
import org.sugar.media.utils.BeanConverterUtil;
import org.sugar.media.validation.gb.DeviceRegisterVal;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private ChannelService channelService;

    @Resource
    private UserSecurity userSecurity;

    @Resource
    private SipCacheService sipCacheService;

    @Resource
    private SipRequestSender sipRequestSender;

    @Resource
    private SipConfUtils sipConfUtils;

    @GetMapping("/page/list")
    public ResponseEntity<?> getDevicePageList(@RequestParam Integer pi, @RequestParam Integer ps, @RequestParam(required = false) String name) {


        Page<DeviceModel> devicePageList = this.deviceService.getDevicePageList(pi, ps, name, this.userSecurity.getCurrentTenantId());

        List<DeviceBean> deviceBeans = BeanConverterUtil.convertList(devicePageList.getContent(), DeviceBean.class);


        deviceBeans = deviceBeans.stream().peek((deviceBean -> {
            deviceBean.setStatus(this.sipCacheService.isOnline(deviceBean.getDeviceId()) ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());

            int size = this.channelService.getDeviceChannelList(deviceBean.getId()).size();
            deviceBean.setChannel(size);


        })).toList();

        return ResponseEntity.ok(ResponseBean.success(devicePageList.getTotalElements(), deviceBeans));

    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDevice(@PathVariable Long deviceId) {

        Optional<DeviceModel> device = this.deviceService.getDevice(deviceId);

        if (device.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        DeviceBean deviceBean = new DeviceBean();

        BeanUtil.copyProperties(device.get(), deviceBean);
        deviceBean.setStatus(this.sipCacheService.isOnline(deviceBean.getDeviceId()) ? StatusEnum.online.getStatus() : StatusEnum.offline.getStatus());
        int size = this.channelService.getDeviceChannelList(deviceBean.getId()).size();
        deviceBean.setChannel(size);


        return ResponseEntity.ok(ResponseBean.success(deviceBean));
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
        String code = this.deviceService.createDeviceCode(tenantCode, deviceRegisterVal.getDeviceId());
        // 查询国标设备是否存在
        DeviceModel device = this.deviceService.getDevice(code);

        if (ObjectUtil.isNotEmpty(device)) {
            return ResponseEntity.ok(ResponseBean.fail("国标编码已存在"));
        }

        DeviceModel deviceModel = new DeviceModel();

        deviceModel.setName(deviceRegisterVal.getName());
        deviceModel.setPwd(deviceRegisterVal.getPwd());
        deviceModel.setDeviceId(code);
        deviceModel.setDeviceType(DeviceTypeEnum.valueOf(deviceRegisterVal.getDeviceType()));
        deviceModel.setTenantId(this.userSecurity.getCurrentTenantId());
        this.deviceService.createDevice(deviceModel);

        return ResponseEntity.ok(ResponseBean.success());

    }

    @PutMapping("/register")
    public ResponseEntity<?> updateDevice(@RequestBody @Validated(DeviceRegisterVal.Update.class) DeviceRegisterVal deviceRegisterVal) {

        Integer tenantCode = this.userSecurity.getCurrentTenantCode();
        String code = this.deviceService.createDeviceCode(tenantCode, deviceRegisterVal.getDeviceId());

        Optional<DeviceModel> deviceModel = this.deviceService.getDevice(deviceRegisterVal.getId());

        if (deviceModel.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        // 查询国标设备是否存在
        DeviceModel device = this.deviceService.getDevice(code);

        if (ObjectUtil.isNotEmpty(device) && !device.getId().equals(deviceModel.get().getId())) {
            return ResponseEntity.ok(ResponseBean.fail("国标编码已存在"));
        }


        // 如果是修改了sip id 先把之前的踢下线
        if (!code.equals(deviceModel.get().getDeviceId())) {
            if (this.sipCacheService.isOnline(deviceModel.get().getDeviceId())) {
                WebSocketServer.sendSystemMsg(new SocketMsgBean(SocketMsgEnum.gbOffline, new Date(), deviceRegisterVal.getName(),null));
            }
            this.sipCacheService.deleteDevice(deviceModel.get().getDeviceId());

        }


        deviceModel.get().setName(deviceRegisterVal.getName());
        deviceModel.get().setPwd(deviceRegisterVal.getPwd());
        deviceModel.get().setDeviceId(code);
        deviceModel.get().setDeviceType(DeviceTypeEnum.valueOf(deviceRegisterVal.getDeviceType()));
        this.deviceService.createDevice(deviceModel.get());

        return ResponseEntity.ok(ResponseBean.success());

    }


    /**
     * 重新同步 device catalog
     *
     * @param deviceId
     * @return
     */
    @GetMapping("/sync/info/catalog/{deviceId}")
    public ResponseEntity<?> syncCatalog(@PathVariable Long deviceId) {


        Optional<DeviceModel> device = this.deviceService.getDevice(deviceId);

        if (device.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        if (!this.sipCacheService.isOnline(device.get().getDeviceId())) {
            return ResponseEntity.ok(ResponseBean.fail("国标设备不在线"));
        }

        DeviceBean deviceBean = new DeviceBean();
        BeanUtil.copyProperties(device.get(), deviceBean);
        this.sipRequestSender.sendDeviceInfo(deviceBean);

        return ResponseEntity.ok(ResponseBean.success());

    }


    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long deviceId) {


        Optional<DeviceModel> device = this.deviceService.getDevice(deviceId);

        if (device.isEmpty()) return ResponseEntity.ok(ResponseBean.fail());

        this.deviceService.deleteDevice(device.get());


        return ResponseEntity.ok(ResponseBean.success());


    }

    // 国标的系统配置

    @GetMapping("/system/config")
    public ResponseEntity<?> systemConfig() {
        return ResponseEntity.ok(ResponseBean.success(this.sipConfUtils));

    }

}
