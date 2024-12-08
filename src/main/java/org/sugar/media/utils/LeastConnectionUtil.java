package org.sugar.media.utils;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date:2024/12/06 13:18:35
 * Author：Tobin
 * Description: 负载均衡---最小连接数法
 */
public class LeastConnectionUtil {

    private static List<String> serverList = new ArrayList<>();

    //记录每个服务器的连接数
    private static Map<String, Integer> connectionsMap = new ConcurrentHashMap<>();

    public static String leastConnections() {
        //获取服务器数量
        int serverCount = serverList.size();
        //如果没有可用的服务器返回null
        if (serverCount == 0) {
            return null;
        }
        //默认选择第一个服务器
        String selectedServerAddress = serverList.get(0);
        //获取第一个服务器的连接数
        int minConnections = connectionsMap.getOrDefault(selectedServerAddress, 0);
        //遍历服务器列表，寻找连接数最少的服务器
        for (int i = 1; i < serverCount; i++) {
            String serverAddress = serverList.get(i);
            int connections = connectionsMap.getOrDefault(serverAddress, 0);
            if (connections < minConnections) {
                selectedServerAddress = serverAddress;
                minConnections = connections;
            }
        }
        //返回连接数最少的服务器地址
        addConnection(selectedServerAddress);
        return selectedServerAddress;
    }


    // 增加连接数
    public static void addConnection(String server) {
        Console.log("当前选择的服务器：{}，连接数{}", server, connectionsMap.getOrDefault(server, 0) + 1);
        connectionsMap.put(server, connectionsMap.getOrDefault(server, 0) + 1);
    }

    public static void addServerList(String server) {
        if (!serverList.contains(server)) {
            serverList.add(server);
        }
    }

    public static void removeServerList(String server) {
        serverList.remove(server);
    }

}
