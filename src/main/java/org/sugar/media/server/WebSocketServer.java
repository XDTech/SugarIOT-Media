package org.sugar.media.server;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.sugar.media.beans.SocketMsgBean;
import org.sugar.media.model.UserModel;
import org.sugar.media.security.UserSecurity;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static jakarta.websocket.CloseReason.CloseCodes.CLOSED_ABNORMALLY;

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
    /**
     * 存放所有在线的客户端
     */
    private static final Map<String, Session> onlineSessionClientMap = new ConcurrentHashMap<>();

    private static UserSecurity userSecurity;

    private static final Map<String, BlockingQueue<String>> sessionMessageQueues = new ConcurrentHashMap<>();

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
            session.close(new CloseReason(CLOSED_ABNORMALLY, "fail"));
            return;
        }

        StaticLog.info("连接建立中 ==> session_id = {}， sid = {}", session.getId(), token);
        //加入 Map中。将页面的uid和session绑定或者session.getId()与session

        //在线数加1

        onlineSessionClientMap.put(Convert.toStr(user.getId()), session);

        this.token = token;
        this.uid = Convert.toStr(user.getId());
        this.session = session;
//        sendToOne(uid, "连接成功");
        StaticLog.info("连接建立成功，当前在线数为：{} ==> 开始监听新连接：session_id = {}， sid = {},。", onlineSessionClientMap.size(), session.getId(), uid);
    }

    /**
     * 连接关闭调用的方法。由前端<code>socket.close()</code>触发
     *
     * @param token
     * @param session
     */
    @OnClose
    public void onClose(@PathParam("token") String token, Session session) throws IOException {
        StaticLog.info("{}触发关闭方法,{}", token, uid);

        //onlineSessionIdClientMap.remove(session.getId());
        session.close();
        // 从 Map中移除
        if (StrUtil.isBlank(uid)) return;

        //在线数减1
        if (onlineSessionClientMap.containsKey(uid)) {
            onlineSessionClientMap.get(Convert.toStr(uid)).close();
        }

        onlineSessionClientMap.remove(uid);


        StaticLog.info("连接关闭成功，当前在线数为：{} ==> 关闭该连接信息：session_id = {}， sid = {},。", onlineSessionClientMap.size(), session.getId(), uid);
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
        // sendToOne(phone, message);
    }

    /**
     * 发生错误调用的方法
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
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

    public static void sendSystemMsg(SocketMsgBean message) {
        onlineSessionClientMap.forEach((onlineSid, toSession) -> {
            String jsonMessage = JSONUtil.toJsonStr(message);

            // 获取或创建队列
            sessionMessageQueues.computeIfAbsent(onlineSid, sid -> {
                BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                startQueueProcessor(onlineSid, toSession, queue);
                return queue;
            }).offer(jsonMessage);
        });
    }

    // 启动队列处理线程
    private static void startQueueProcessor(String onlineSid, Session toSession, BlockingQueue<String> queue) {
        new Thread(() -> {
            while (true) {
                try {
                    String message = queue.take(); // 从队列取出消息
                    toSession.getAsyncRemote().sendText(message, result -> {
                        if (!result.isOK()) {
                            System.err.println("Failed to send message to session " + onlineSid);
                            result.getException().printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
