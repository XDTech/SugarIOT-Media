package org.sugar.media.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sugar.media.enums.SocketMsgEnum;

import java.util.Date;

/**
 * Date:2024/11/26 15:25:15
 * Author：Tobin
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocketMsgBean {


    private SocketMsgEnum types;// 消息类型

    private Date time; //发送消息时间

    private String msg; // 消息内容
}
