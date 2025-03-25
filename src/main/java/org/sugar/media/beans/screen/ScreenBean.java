package org.sugar.media.beans.screen;

import lombok.Data;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;

/**
 * Date:2025/01/04 14:33:28
 * Author：Tobin
 * Description: 返回前端 生成tree
 */

@Data
public class ScreenBean {

    private Long id;

    private AppEnum types;

    private String nodeType = "0"; // 0表示节点，1表示正常的流

    private String name;

    private Long parentId = 0L;

    private String app;

    private String stream;

    private StatusEnum status;

    private String deviceCode;

    private String channelCode;


}
