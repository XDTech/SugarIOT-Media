package org.sugar.media.service.media;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.sugar.media.beans.hooks.zlm.CloseStreamBean;
import org.sugar.media.beans.hooks.zlm.CommonBean;
import org.sugar.media.beans.hooks.zlm.StreamProxyInfoBean;
import org.sugar.media.beans.hooks.zlm.ZlmRemoteConfigBean;
import org.sugar.media.enums.AutoCloseEnum;
import org.sugar.media.enums.SyncEnum;
import org.sugar.media.model.TenantModel;
import org.sugar.media.model.node.NodeModel;
import org.sugar.media.model.stream.StreamPullModel;
import org.sugar.media.service.tenant.TenantService;
import org.sugar.media.utils.BaseUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ZlmApiService {


    @Autowired
    public RestTemplate restTemplate;


    @Value("${server.port}")
    private Integer serverPort;

    @Value("${server.ip}")
    private String serverIp;


    @Resource
    private TenantService tenantService;

    public static String savePathPrefix = "./www";

    // 创建一个新的zlm实例时，应当初始化该方法


    public boolean syncZlmConfig(NodeModel nodeModel, SyncEnum types) {
        // set config
        String host = StrUtil.format("http://{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(host + "/index/api/setServerConfig");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());


            if (types.equals(SyncEnum.hook)) {
                this.createHookConfig(nodeModel, builder);
            }


            if (types.equals(SyncEnum.base)) {
                this.createBaseConfig(nodeModel, builder);
            }
            if (types.equals(SyncEnum.all)) {
                this.createBaseConfig(nodeModel, builder);
                this.createHookConfig(nodeModel, builder);
            }

            StaticLog.info("{}", builder.toUriString());


            //r
            Map<String, String> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);

            StaticLog.info("{}", exchange.getStatusCode());

            Object code = exchange.getBody().get("code");
            if (Convert.toInt(code).equals(0)) {
                return true;
            }

            return false;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }

    }

    private void createHookConfig(NodeModel nodeModel, UriComponentsBuilder builder) {
        Map<String, String> confMap = new HashMap<>();

        String host = StrUtil.format("http://{}:{}", this.serverIp, this.serverPort);

        //    confMap.put("api.secret",nodeModel.getId().toString());
        builder.queryParam("http.sslport", nodeModel.getHttpsPort().toString());
        builder.queryParam("http.port", nodeModel.getHttpPort().toString());
        // 是否启用hook事件，启用后，推拉流都将进行鉴权
        builder.queryParam("hook.enable", "1");

        // 关闭hls 否则会自动录制
        builder.queryParam("protocol.enable_hls", "0");

        // 服务器唯一id，用于触发hook时区别是哪台服务器
        builder.queryParam("general.mediaServerId", nodeModel.getId().toString());

        // 服务器启动报告，可以用于服务器的崩溃重启事件监听
        builder.queryParam("hook.on_server_started", host + "/zlm/server/started");

        builder.queryParam("hook.on_server_exited", host + "/zlm/server/exited");
        //
        builder.queryParam("hook.on_server_keepalive", host + "/zlm/keepalive");

        // 鉴权
        builder.queryParam("hook.on_play", host + "/zlm/on_play");

        // 流未找到
        builder.queryParam("hook.on_stream_not_found", host + "/zlm/stream/nof/found");

        // 无人观看事件
        builder.queryParam("hook.on_stream_none_reader", host + "/zlm/stream/none/reader");

        // 流量统计事件
        builder.queryParam("hook.on_flow_report", host + "/zlm/on/flow/report");


        // mp4 录制回调

        builder.queryParam("hook.on_record_mp4", host + "/zlm/on_record_mp4");

        // 推流鉴权（推到zlm的流）

        builder.queryParam("hook.on_publish", host + "/zlm/on_publish");

        // 流改变事件

        builder.queryParam("hook.on_stream_changed", host + "/zlm/on_stream_changed");


    }

    private void createBaseConfig(NodeModel nodeModel, UriComponentsBuilder builder) {

        builder.queryParam("rtmp.port", nodeModel.getRtmpPort());
        //
        builder.queryParam("rtsp.port", nodeModel.getRtspPort());
        builder.queryParam("hook.timeoutSec", nodeModel.getTimeoutSec());
        builder.queryParam("hook.alive_interval", nodeModel.getAliveInterval());


    }

    private String createZlmHost(NodeModel nodeModel) {
        return StrUtil.format("http://{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
    }

    // 获取服务器api列表(getApiList)
    public List<String> getApiList(NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/getApiList");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            Map<String, Object> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);

            return Convert.toList(String.class, exchange.getBody().get("data"));

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }


    }

    // 获取服务配置
    public ZlmRemoteConfigBean getServerConfig(NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/getServerConfig");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<ZlmRemoteConfigBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ZlmRemoteConfigBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }


    }


    public CommonBean restartServer(NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/restartServer");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            Map<String, Object> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<CommonBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CommonBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }


    }

    public CommonBean addStreamProxy(StreamPullModel pullModel, NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/addStreamProxy");

            Optional<TenantModel> tenant = this.tenantService.getTenant(pullModel.getTenantId());

            if (tenant.isEmpty()) {
                CommonBean commonBean = new CommonBean();
                commonBean.setCode(-1);
                commonBean.setMsg("tenant not found");
                return commonBean;
            }

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            builder.queryParam("vhost", pullModel.getVhost());
            builder.queryParam("app", pullModel.getApp());
            builder.queryParam("stream", pullModel.getStream());
            builder.queryParam("url", pullModel.getUrl());
            builder.queryParam("mp4_save_path", this.getSavePath(tenant.get().getCode()));
            builder.queryParam("hls_save_path", this.getSavePath(tenant.get().getCode()));
            builder.queryParam("timeout_sec", pullModel.getTimeoutSec());
            builder.queryParam("enable_rtsp", BaseUtil.convertBool(pullModel.isEnableRtsp()));
            builder.queryParam("enable_hls", BaseUtil.convertBool(pullModel.isEnableHls()));
            builder.queryParam("enable_rtmp", BaseUtil.convertBool(pullModel.isEnableRtmp()));
            builder.queryParam("enable_mp4", BaseUtil.convertBool(pullModel.isEnableMp4()));
            builder.queryParam("enable_ts", BaseUtil.convertBool(pullModel.isEnableTs()));
            builder.queryParam("enable_fmp4", BaseUtil.convertBool(pullModel.isEnableFmp4()));
            builder.queryParam("enable_audio", BaseUtil.convertBool(pullModel.isEnableAudio()));
            builder.queryParam("add_mute_audio", BaseUtil.convertBool(pullModel.isAddMuteAudio()));
            builder.queryParam("mp4_max_second", pullModel.getMp4MaxSecond());

            if (pullModel.getAutoClose().equals(AutoCloseEnum.yes)) {
                builder.queryParam("auto_close", "1");

            } else if (pullModel.getAutoClose().equals(AutoCloseEnum.no)) {
                builder.queryParam("auto_close", "0");
            }
            StaticLog.info("{}", builder.toUriString());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<CommonBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CommonBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            CommonBean commonBean = new CommonBean();
            commonBean.setCode(-1);
            commonBean.setMsg(e.getMessage());

            e.printStackTrace();
            return commonBean;

        }


    }


    public CommonBean closeStreamProxy(StreamPullModel pullModel, NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/delStreamProxy");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            builder.queryParam("key", pullModel.getStreamKey());

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<CommonBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CommonBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            CommonBean commonBean = new CommonBean();
            commonBean.setCode(-1);
            commonBean.setMsg(e.getMessage());

            e.printStackTrace();
            return commonBean;

        }
    }

    /**
     * 获取拉流代理
     */
    public StreamProxyInfoBean getStreamProxyInfo(String streamkey, NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/getProxyInfo");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            builder.queryParam("key", streamkey);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<StreamProxyInfoBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, StreamProxyInfoBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            StreamProxyInfoBean streamProxyInfoBean = new StreamProxyInfoBean();
            streamProxyInfoBean.setCode(-1);


            e.printStackTrace();
            return streamProxyInfoBean;

        }

    }




    public CloseStreamBean closeSteam(String app, String stream, NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/close_streams");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            builder.queryParam("app", app);
            builder.queryParam("stream", stream);
            builder.queryParam("force", 1);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<CloseStreamBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CloseStreamBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            CloseStreamBean commonBean = new CloseStreamBean();
            commonBean.setCode(-1);


            e.printStackTrace();
            return commonBean;

        }
    }

    public String getSavePath(Integer tenantCode) {

        return StrUtil.format("{}/{}", savePathPrefix, tenantCode);

    }


}
