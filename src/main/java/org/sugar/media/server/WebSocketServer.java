package org.sugar.media.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.sugar.media.model.UserModel;
import org.sugar.media.security.UserSecurity;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date:2024/11/20 14:19:49
 * Author：Tobin
 * Description:
 */

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    /**
     * 静态变量，用来记录当前在线连接数，线程安全的类。
     */
    private static final AtomicInteger onlineSessionClientCount = new AtomicInteger(0);
    /**
     * 存放所有在线的客户端
     */
    private static final Map<String, Session> onlineSessionClientMap = new ConcurrentHashMap<>();

    private static UserSecurity userSecurity;

    @Autowired
    public void setDeviceListenerService(UserSecurity userSecurity) {
        WebSocketServer.userSecurity = userSecurity;
    }

    /**
     * 连接uid和连接会话
     */
    private String uid;
    private String token;
    private Session session;

    /**
     * 连接建立成功调用的方法。由前端<code>new WebSocket</code>触发
     *
     * @param token   token信息
     * @param session 与某个客户端的连接会话，需要通过它来给客户端发送消息
     */
    @OnOpen
    public void onOpen(@PathParam("token") String token, Session session) throws IOException {
        /**
         * session.getId()：当前session会话会自动生成一个id，从0开始累加的。
         */
        UserModel user = this.userSecurity.getUser(token);
        StaticLog.info("{}", user);

        // 鉴权失败
        if (ObjectUtil.isEmpty(user)) {
            session.close();
            return;
        }

        StaticLog.info("连接建立中 ==> session_id = {}， sid = {}", session.getId(), token);
        //加入 Map中。将页面的uid和session绑定或者session.getId()与session
        //onlineSessionIdClientMap.put(session.getId(), session);
        onlineSessionClientMap.put(token, session);
        //在线数加1
        onlineSessionClientCount.incrementAndGet();
        this.token = token;
        this.session = session;
//        sendToOne(uid, "连接成功");
        StaticLog.info("连接建立成功，当前在线数为：{} ==> 开始监听新连接：session_id = {}， sid = {},。", onlineSessionClientCount, session.getId(), uid);
    }

    /**
     * 连接关闭调用的方法。由前端<code>socket.close()</code>触发
     *
     * @param token
     * @param session
     */
    @OnClose
    public void onClose(@PathParam("token") String token, Session session) {
        //onlineSessionIdClientMap.remove(session.getId());
        // 从 Map中移除
        onlineSessionClientMap.remove(uid);

        //在线数减1
        onlineSessionClientCount.decrementAndGet();
        StaticLog.info("连接关闭成功，当前在线数为：{} ==> 关闭该连接信息：session_id = {}， sid = {},。", onlineSessionClientCount, session.getId(), uid);
    }

    /**
     * 收到客户端消息后调用的方法。由前端<code>socket.send</code>触发
     * * 当服务端执行toSession.getAsyncRemote().sendText(xxx)后，前端的socket.onmessage得到监听。
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        /**
         * html界面传递来得数据格式，可以自定义.
         * {"sid":"user-1","message":"hello websocket"}
         */
        StaticLog.info("接收到消息-----------------" + message);
        //A发送消息给B，服务端收到A的消息后，从A的消息体中拿到B的uid及携带的手机号。查找B是否在线，如果B在线，则使用B的session发消息给B自己
        String phone = "";
        String toSid = "";
        //A给B发送消息，A要知道B的信息，发送消息的时候把B的信息携带过来
        StaticLog.info("服务端收到客户端消息 ==> fromSid = {}, toSid = {}, message = {}", uid, toSid, message);
        sendToOne(phone, message);
    }

    /**
     * 发生错误调用的方法
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        StaticLog.error("WebSocket发生错误，错误信息为：" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 群发消息
     *
     * @param message 消息
     */
    public void sendToAll(String message) {
        // 遍历在线map集合
        onlineSessionClientMap.forEach((onlineSid, toSession) -> {
            // 排除掉自己
            if (!uid.equalsIgnoreCase(onlineSid)) {
                StaticLog.info("服务端给客户端群发消息 ==> sid = {}, toSid = {}, message = {}", uid, onlineSid, message);
                toSession.getAsyncRemote().sendText(message);
            }
        });
    }

    /**
     * 指定发送消息
     *
     * @param toUid
     * @param message
     */
    private void sendToOne(String toUid, String message) {
        /*
         * 判断发送者是否在线
         */
        Session toSession = onlineSessionClientMap.get(toUid);
        if (toSession == null) {
            StaticLog.error("服务端给客户端发送消息 ==> toSid = {} 不存在, message = {}", toUid, message);
            return;
        }
        // 异步发送
        StaticLog.info("服务端给客户端发送消息 ==> toSid = {}, message = {}", toUid, message);
        toSession.getAsyncRemote().sendText(message);
    }

}