package org.sugar.media.controller.screen;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sugar.media.beans.ResponseBean;
import org.sugar.media.beans.hooks.zlm.StreamInfoBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.screen.ScreenBean;
import org.sugar.media.beans.stream.StreamPullBean;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;
import org.sugar.media.model.gb.DeviceChannelModel;
import org.sugar.media.model.gb.DeviceModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.model.stream.StreamPushModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.StreamService;
import org.sugar.media.service.gb.ChannelService;
import org.sugar.media.service.gb.DeviceService;
import org.sugar.media.service.media.MediaCacheService;
import org.sugar.media.service.media.ZlmApiService;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.service.node.ZlmNodeService;
import org.sugar.media.service.stream.StreamPullService;
import org.sugar.media.service.stream.StreamPushService;
import org.sugar.media.sipserver.manager.SipCacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Date:2025/01/04 14:37:15
 * Author：Tobin
 * Description:
 */

@RestController
@RequestMapping("/screen")
@Validated
public class ScreenController {


    @Resource
    private UserSecurity userSecurity;

    @Resource
    private StreamService streamService;


    @GetMapping("/list")
    public ResponseEntity<?> getList() {


        Long tenantId = this.userSecurity.getCurrentTenantId();


        return ResponseEntity.ok(ResponseBean.success(this.streamService.getOnlineStreamList(tenantId)));
    }

}
