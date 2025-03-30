package org.sugar.media.controller.webhook;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.service.StreamService;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.tenant.TenantService;
import org.sugar.media.utils.AesUtil;
import org.sugar.media.utils.SecurityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Date:2025/03/30 11:02:20
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/hook")
public class HookController {


    @Resource
    private StreamService streamService;

    @Resource
    private TenantService tenantService;


    @Resource
    private ChannelService channelService;


    // 获取在线的设备
    @GetMapping("/stream/list")
    public ResponseEntity<?> getList(@RequestParam Integer code) {


        TenantModel tenant = this.tenantService.getTenant(code);
        if (ObjectUtil.isEmpty(tenant)) return ResponseEntity.ok(ResponseBean.fail());


        return ResponseEntity.ok(ResponseBean.success(this.streamService.getOnlineStreamList(tenant.getId())));
    }

    // 通过鉴权码播放摄像头，返回视频流地址

    @GetMapping("/stream/addr")
    public ResponseEntity<?> getStreamAddr(@RequestParam String secret) {


        String aesDecrypt = AesUtil.aesDecrypt(secret);

        JSON parse = JSONUtil.parse(aesDecrypt);


        String types = (String) parse.getByPath("types");

        Long id = (Long) parse.getByPath("id");
        Map<String, List<String>> map = new HashMap<>();
        switch (types) {
            case "live" -> map = this.streamService.getPushStreamAddr(id);
            case "proxy" -> map = this.streamService.getPullStreamAddr(id);
            case "rtp" -> {
                Optional<DeviceChannelModel> channel = this.channelService.getChannel(id);
                if (channel.isPresent()) {
                    map = this.channelService.inviteChannel(channel.get());
                }

            }
        }
        return ResponseEntity.ok(ResponseBean.success(map));
    }
}
