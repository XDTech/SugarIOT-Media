package org.sugar.media.beans.hooks.zlm;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Date:2024/11/26 16:53:30
 * Author：Tobin
 * Description:
 */

@Data
public class CommonBean {

    private Integer code;

    private String msg;

    private Long nodeId;

    private Map<String,Object> data=new HashMap<>();
}
