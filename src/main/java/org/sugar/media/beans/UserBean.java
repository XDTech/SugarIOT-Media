package org.sugar.media.beans;

import lombok.Data;

import java.io.Serializable;

/**
 * (MUser)表实体类
 *
 * @author Tobin
 * @since 2024-11-10 10:05:25
 */
@Data
public class UserBean implements Serializable {

    private static final long serialVersionUID = -62648420112215297L;


    private Long id;

    private String avatar;

    private String email;

    private String name;


    private String phone;


    private String username;




}

