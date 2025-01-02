package org.sugar.media.service.node;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.repository.node.NodeRepo;
import org.sugar.media.utils.JwtUtils;

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


    public List<NodeModel> getNodeAll(List<Long> ids) {

        return this.nodeRepo.findAllByIdIn(ids);
    }

    public List<NodeModel> getNodeAll() {

        return this.nodeRepo.findAll();
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

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("streamId", streamPullModel.getId());
        String token = JwtUtils.createToken(tokenMap);
        Map<String, List<String>> map = new HashMap<>();
        String appStream = StrUtil.format("{}/{}", streamPullModel.getApp(), streamPullModel.getStream());
        String host = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
        String sslHost = StrUtil.format("{}:{}", nodeModel.getIp(), nodeModel.getHttpsPort());


        // rtmp url (ws/wss/http/https .flv)
        if (streamPullModel.isEnableRtmp()) {
            this.addRtmpMediaSource(map, host, sslHost, appStream, token);
        }

        // ts url (ws/wss/http/https .ts)
        if (streamPullModel.isEnableTs()) {
            this.addTsMediaSource(map, host, sslHost, appStream, token);
        }
        // hls (ts fmp4)

        if (streamPullModel.isEnableHls()) {

            this.addHlsMediaSource(map, host, sslHost, appStream, token);

        }

        // fmp4
        if (streamPullModel.isEnableFmp4()) {


            this.addFmp4MediaSource(map, host, sslHost, appStream, token);
        }
        return map;


    }


    public void addRtmpMediaSource(Map<String, List<String>> map, String host, String sslHost, String appStream, String token) {
        List<String> list = new ArrayList<>();
        list.add(StrUtil.format("ws://{}/{}.live.flv?sign={}", host, appStream, token));
        list.add(StrUtil.format("wss://{}/{}.live.flv?sign={}", sslHost, appStream, token));
        list.add(StrUtil.format("http://{}/{}.live.flv?sign={}", host, appStream, token));
        list.add(StrUtil.format("https://{}/{}.live.flv?sign={}", sslHost, appStream, token));
        map.put("RtmpMediaSource", list);
    }

    public void addTsMediaSource(Map<String, List<String>> map, String host, String sslHost, String appStream, String token) {
        List<String> list = new ArrayList<>();
        list.add(StrUtil.format("ws://{}/{}.live.ts?sign={}", host, appStream, token));
        list.add(StrUtil.format("wss://{}/{}.live.ts?sign={}", sslHost, appStream, token));
        list.add(StrUtil.format("http://{}/{}.live.ts?sign={}", host, appStream, token));
        list.add(StrUtil.format("https://{}/{}.live.ts?sign={}", sslHost, appStream, token));
        map.put("TSMediaSource", list);
    }

    public void addHlsMediaSource(Map<String, List<String>> map, String host, String sslHost, String appStream, String token) {
        List<String> list = new ArrayList<>();
        list.add(StrUtil.format("http://{}/{}/hls.m3u8?sign={}", host, appStream, token));
        list.add(StrUtil.format("https://{}/{}/hls.m3u8?sign={}", sslHost, appStream, token));
        map.put("HlsMediaSource", list);
    }

    public void addFmp4MediaSource(Map<String, List<String>> map, String host, String sslHost, String appStream, String token) {
        List<String> list = new ArrayList<>();
        list.add(StrUtil.format("ws://{}/{}.live.mp4?sign={}&s=1", host, appStream, token));
        list.add(StrUtil.format("wss://{}/{}.live.mp4?sign={}", sslHost, appStream, token));
        list.add(StrUtil.format("http://{}/{}.live.mp4?sign={}", host, appStream, token));
        list.add(StrUtil.format("https://{}/{}.live.mp4?sign={}", sslHost, appStream, token));
        map.put("FMP4MediaSource", list);
    }





}
