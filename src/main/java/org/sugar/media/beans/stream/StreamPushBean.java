package org.sugar.media.beans.stream;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.sugar.media.enums.AppEnum;
import org.sugar.media.enums.StatusEnum;

import java.util.Date;

/**
 * Date:2025/01/02 09:10:09
 * Author：Tobin
 * Description:
 */
@Data
public class StreamPushBean {

    private Long id;

    private Long tenantId;// 租户id

    private Date createdAt;

    private Date updatedAt;


    private String name;


    private String app;

    private String stream;

    private String params;


    private AppEnum types;


    private Long nodeId;

    private Long relevanceId; // 关联的 通道id


    private StatusEnum status;

    private Integer totalReaderCount;
    private String bytesSpeed;
    private String originTypeStr;
    private String schema;
    private Date pushAt;

    private Long aliveSecond;

    private String secret;
}
