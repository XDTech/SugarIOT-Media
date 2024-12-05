package org.sugar.media.service.node;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.repository.node.NodeRepo;

import java.util.*;

/**
 * Date:2024/12/03 10:50:48
 * Author：Tobin
 * Description: node表基础操作service
 */

@Service
public class NodeService {

    @Autowired
    private NodeRepo nodeRepo;


    public Optional<NodeModel> getNode(Long id) {

        return this.nodeRepo.findById(id);
    }


    public void dibbleSowing() {

    }

    // 获取节点的播放url

    public Map<String, List<String>> createNodePlayerUrl(StreamPullModel streamPullModel, NodeModel nodeModel) {


        switch (nodeModel.getTypes()) {
            case zlm -> {

                return this.createZlmUrl(streamPullModel, nodeModel);

            }
            default -> {
                return null;
            }

        }

    }


    /**
     * @param streamPullModel
     * @param nodeModel
     * @return
     * @see <a href="https://docs.zlmediakit.com/zh/guide/media_server/play_url_rules.html">zlm播放url规则</a>
     */
    public Map<String, List<String>> createZlmUrl(StreamPullModel streamPullModel, NodeModel nodeModel) {

        Map<String, List<String>> map = new HashMap<>();
        String appStream = StrUtil.format("{}/{}", streamPullModel.getApp(), streamPullModel.getStream());
        String host = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
        String sslHost = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpsPort());


        // rtmp url (ws/wss/http/https .flv)
        if (streamPullModel.isEnableRtmp()) {
            String ws = StrUtil.format("ws://{}/{}.live.flv", host, appStream);
            String wss = StrUtil.format("wss://{}/{}.live.flv", sslHost, appStream);
            String http = StrUtil.format("http://{}/{}.live.flv", host, appStream);
            String https = StrUtil.format("https://{}/{}.live.flv", sslHost, appStream);

            List<String> list = new ArrayList<>();
            list.add(ws);
            list.add(wss);
            list.add(http);
            list.add(https);
            map.put("RtmpMediaSource", list);
        }

        // ts url (ws/wss/http/https .ts)
        if (streamPullModel.isEnableTs()) {
            String ws = StrUtil.format("ws://{}/{}.live.ts", host, appStream);
            String wss = StrUtil.format("wss://{}/{}.live.ts", sslHost, appStream);
            String http = StrUtil.format("http://{}/{}.live.ts", host, appStream);
            String https = StrUtil.format("https://{}/{}.live.ts", sslHost, appStream);


            List<String> list = new ArrayList<>();
            list.add(ws);
            list.add(wss);
            list.add(http);
            list.add(https);
            map.put("TSMediaSource", list);
        }
        // hls (ts fmp4)

        if (streamPullModel.isEnableHls()) {

            String http = StrUtil.format("http://{}/{}/hls.m3u8", host, appStream);
            String https = StrUtil.format("https://{}/{}/hls.m3u8", sslHost, appStream);

            List<String> list = new ArrayList<>();

            list.add(http);
            list.add(https);
            map.put("HlsMediaSource", list);

        }

        // fmp4
        if (streamPullModel.isEnableFmp4()) {


            String ws = StrUtil.format("ws://{}/{}.live.mp4", host, appStream);
            String wss = StrUtil.format("wss://{}/{}.live.mp4", sslHost, appStream);
            String http = StrUtil.format("http://{}/{}.live.mp4", host, appStream);
            String https = StrUtil.format("https://{}/{}.live.mp4", sslHost, appStream);

            List<String> list = new ArrayList<>();
            list.add(ws);
            list.add(wss);
            list.add(http);
            list.add(https);
            map.put("FMP4MediaSource", list);

        }
        return map;


    }

}
