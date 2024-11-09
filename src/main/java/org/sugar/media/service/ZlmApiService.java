package org.sugar.media.service;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.StaticLog;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.sugar.media.config.RestTemplateConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZlmApiService {


    @Autowired
    public RestTemplate restTemplate;

    // 获取服务器api列表(getApiList)
    public List<String> getApiList(String host, String secret) {

        try {
            HttpHeaders headers = new HttpHeaders();

            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(host + "/index/api/getApiList");

            // add param
            builder.queryParam("secret", secret);
            Map<String, Object> reqMap = new HashMap<>();
            HttpEntity<?> entity = new HttpEntity<>(reqMap, headers);
            ResponseEntity<Map> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Map.class);

            return Convert.toList(String.class, exchange.getBody().get("data"));

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }


    }


}
