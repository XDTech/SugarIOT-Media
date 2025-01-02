package org.sugar.media.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.security.UserSecurity;
import org.sugar.media.service.node.NodeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date:2025/01/01 09:58:29
 * Author：Tobin
 * Description:
 */
@Component
public class MediaUtil {


    @Resource
    private UserSecurity userSecurity;

    @Resource
    private NodeService nodeService;

    public String genStreamId(String streamId) {
        return StrUtil.format("{}_{}", this.userSecurity.getCurrentTenantCode(), streamId);
    }

    public Map<String, List<String>> genAddr(NodeModel nodeModel, String app, String stream) {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("key", "666");
        String token = JwtUtils.createToken(tokenMap);
        Map<String, List<String>> map = new HashMap<>();
        String appStream = StrUtil.format("{}/{}", app, stream);
        String host = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
        String sslHost = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpsPort());

        this.nodeService.addFmp4MediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addRtmpMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addTsMediaSource(map, host, sslHost, appStream, token);
        this.nodeService.addHlsMediaSource(map, host, sslHost, appStream, token);

        return map;
    }
}
