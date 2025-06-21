package com.nonu1l.msgboxcore.dto;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 用户 Dto
 * @author zhonghanbo
 * @date 2024年11月20日 17:39
 */

@Data
public class UserDto {
    private String userId;
    private String nickname;

    // --- for password update ---
    private String oldPassword;
    private String newPassword;
    // --- end ---

    private String email;
    private Integer status;
    private String emailServerId;
    private String emailPassword;
    private Integer isAdmin;
    private Timestamp lastLoginTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
