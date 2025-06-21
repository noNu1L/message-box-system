package com.nonu1l.msgboxservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * @author zhonghanbo
 * @date 2025年01月05日 15:36
 */

@Data
@TableName("ex_user")
public class UserExt implements UserDetails {

    @TableId
    private String userId;
    private String nickname;
    private String email;
    private String emailServerId;
    private String emailPassword;
    private Integer emailStatus;
    private Integer status;
    private Integer isAdmin;
    private Timestamp lastLoginTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String username;
    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
