package org.sugar.media.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.service.node.NodeService;
import org.sugar.media.utils.LeastConnectionUtil;

import java.util.Optional;

/**
 * Date:2024/12/24 10:37:38
 * Author：Tobin
 * Description:
 */

@Service
public class LoadBalanceService {


    @Resource
    private NodeService nodeService;

    @Resource
    private MediaCacheService mediaCacheService;

    public NodeModel executeBalance() {

        String serverId = LeastConnectionUtil.leastConnections();

        if (StrUtil.isBlank(serverId)) return null;

        //
        Long mediaId = Convert.toLong(serverId);
        Optional<NodeModel> node = this.nodeService.getNode(mediaId);

        if (node.isEmpty()) return null;

        boolean online = this.mediaCacheService.isOnline(mediaId);
        if (!online) return null;

        Console.log("执行负载均衡策略{}", node.get().toString());

        return node.get();


    }
}
