package org.sugar.media.service;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.sugar.media.beans.hooks.zlm.ZlmRemoteConfigBean;
import org.sugar.media.enums.SyncEnum;
import org.sugar.media.model.node.NodeModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZlmApiService {


    @Autowired
    public RestTemplate restTemplate;


    @Value("${server.port}")
    private Integer serverPort;

    @Value("${server.ip}")
    private String serverIp;


    // 创建一个新的zlm实例时，应当初始化该方法



    public boolean syncZlmConfig(NodeModel nodeModel, SyncEnum types) {
        // set config
        String host = StrUtil.format("http://{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(host + "/index/api/setServerConfig");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());


            if(types.equals(SyncEnum.hook)){
                this.createHookConfig(nodeModel, builder);
            }


            if(types.equals(SyncEnum.base)){
               this.createBaseConfig(nodeModel,builder);
            }
            if(types.equals(SyncEnum.all)){
                this.createBaseConfig(nodeModel, builder);
                this.createHookConfig(nodeModel,builder);
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

        // 服务器唯一id，用于触发hook时区别是哪台服务器
        builder.queryParam("general.mediaServerId", nodeModel.getId().toString());

        // 服务器启动报告，可以用于服务器的崩溃重启事件监听
        builder.queryParam("hook.on_server_started", host + "/zlm/server/started");
        //
        builder.queryParam("hook.on_server_keepalive", host + "/zlm/keepalive");


    }

    private void createBaseConfig(NodeModel nodeModel, UriComponentsBuilder builder) {
        Map<String, String> confMap = new HashMap<>();

        String host = StrUtil.format("http://{}:{}", this.serverIp, this.serverPort);

        //    confMap.put("api.secret",nodeModel.getId().toString());
        builder.queryParam("http.sslport", nodeModel.getHttpsPort().toString());
        builder.queryParam("http.port", nodeModel.getHttpPort().toString());
        // 是否启用hook事件，启用后，推拉流都将进行鉴权
        builder.queryParam("hook.enable", "1");

        // 服务器唯一id，用于触发hook时区别是哪台服务器
        builder.queryParam("general.mediaServerId", nodeModel.getId().toString());

        // 服务器启动报告，可以用于服务器的崩溃重启事件监听
        builder.queryParam("hook.on_server_started", host + "/zlm/server/started");
        //
        builder.queryParam("hook.on_server_keepalive", host + "/zlm/keepalive");


    }

    private String createZlmHost(NodeModel nodeModel){
      return StrUtil.format("http://{}:{}", nodeModel.getIp(), nodeModel.getHttpPort());
    }
    // 获取服务器api列表(getApiList)
    public List<String> getApiList(NodeModel nodeModel) {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/getApiList");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            Map<String, Object> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(reqMap, headers);
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

            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.createZlmHost(nodeModel) + "/index/api/getServerConfig");

            // add param
            builder.queryParam("secret", nodeModel.getSecret());
            Map<String, Object> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(reqMap, headers);
            ResponseEntity<ZlmRemoteConfigBean> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, ZlmRemoteConfigBean.class);

            return exchange.getBody();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }


    }


}
