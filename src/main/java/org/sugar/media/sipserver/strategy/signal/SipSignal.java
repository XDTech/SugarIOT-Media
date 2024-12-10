package org.sugar.media.sipserver.strategy.signal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date:2024/12/10 10:44:27
 * Author：Tobin
 * Description:
 */
// 定义一个用于标识 SIP 信令处理器的注解
@Target(ElementType.TYPE) // 仅用于类级别
@Retention(RetentionPolicy.RUNTIME) // 在运行时可访问
public @interface SipSignal {
    String value(); // 用于指定处理的信令类型，例如 "REGISTER", "BYE" 等
}