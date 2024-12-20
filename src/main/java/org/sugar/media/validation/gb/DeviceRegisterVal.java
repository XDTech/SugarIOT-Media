package org.sugar.media.validation.gb;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.sugar.media.enums.DeviceTypeEnum;
import org.sugar.media.enums.PlayerTypeEnum;
import org.sugar.media.validation.validator.EnumValidatorInterface;

/**
 * Date:2024/12/19 11:46:23
 * Author：Tobin
 * Description:
 */

@Data
public class DeviceRegisterVal {

    @NotBlank(message = "device name not null")
    private String deviceName; // 设备名称


    @NotBlank(message = "device id not null")
    @Pattern(regexp = "^\\d{7}$", message = "请输入7位流水号")
    private String deviceId;// 设备id 规则:[租户编码]+0000+7位流水号生成

    @NotBlank(message = "device type is required")
    @EnumValidatorInterface(enumClass = DeviceTypeEnum.class, message = "Invalid device type")
    private  String deviceType;

    private String pwd; //国标设备验证id 为空采用系统密码
}
