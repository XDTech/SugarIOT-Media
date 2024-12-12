package org.sugar.media.beans.gb;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Date:2024/12/12 11:05:14
 * Author：Tobin
 * Description:
 */


@Data
public class DeviceBean {
    private Long id;
    private Long tenantId;// 租户id
    private String name; // 设备名称
    private String deviceId;// 设备id 规则:[租户编码]+0000+7位流水号生成
    private String pwd; //国标设备验证id 为空采用系统密码
}
