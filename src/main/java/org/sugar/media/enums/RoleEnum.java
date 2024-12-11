package org.sugar.media.enums;

/**
 * Date:2024/12/11 23:04:45
 * Author：Tobin
 * Description: 平台管理员（PLATFORM_ADMIN）
 * <p>
 * 查询所有租户的数据。
 * 不限制 tenant_id。
 * 租户管理员和普通用户（TENANT_ADMIN, USER）
 * <p>
 * 查询限定租户的数据。
 * 限制 tenant_id 为当前用户所属的租户。
 */
public enum RoleEnum {

    platform_admin, tenant_admin, tenant_user

}
