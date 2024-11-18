package org.sugar.media.beans.node;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sugar.media.enums.MediaServerEnum;

/**
 * Date:2024/11/18 09:51:36
 * Author：Tobin
 * Description:
 */

@Data
public class NodeBean {


    private Long id;


    private boolean online;

    private String types;// 状态



    private String name;

    private String ip;


    private Integer httpPort;


    private Integer httpsPort;

    private String secret;
}
