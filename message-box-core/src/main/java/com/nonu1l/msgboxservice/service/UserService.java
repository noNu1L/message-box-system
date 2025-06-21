package com.nonu1l.msgboxservice.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.exdataext.service.BaseService;
import com.nonu1l.msgboxservice.dto.UserDto;
import com.nonu1l.msgboxservice.entity.UserExt;

import com.nonu1l.msgboxservice.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * 用户 Service
 *
 * @author nonu1l
 * @date 2024年11月09日 10:41
 */

@Service
public class UserService extends BaseService<UserMapper, UserExt> implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        LambdaQueryWrapper<UserExt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserExt::getUsername, username);
        UserExt user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new UsernameNotFoundException("UserExt not found: " + username);
        }
        return user;
    }

    /**
     * 更新
     *
     * @param userDto 用户 DTO
     */
    public void update(UserDto userDto) {
        // 从安全上下文中获取当前登录的用户名
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        UserExt currentUser = (UserExt) this.loadUserByUsername(currentUsername);

        if (currentUser == null) {
            throw new RuntimeException("用户不存在或未登录");
        }

        boolean isModified = false;

        // 1. 处理密码修改
        if (ValidateUtils.isNotEmpty(userDto.getNewPassword())) {
            if (ValidateUtils.isEmpty(userDto.getOldPassword())) {
                throw new RuntimeException("请输入旧密码");
            }
            // 验证旧密码
            if (!passwordEncoder.matches(userDto.getOldPassword(), currentUser.getPassword())) {
                throw new RuntimeException("旧密码不正确");
            }
            // 加密并设置新密码
            currentUser.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
            isModified = true;
        }

        // 2. 处理昵称修改
        if (ValidateUtils.isNotEmpty(userDto.getNickname())) {
            currentUser.setNickname(userDto.getNickname());
            isModified = true;
        }

        // 如果有修改，则更新到数据库
        if (isModified) {
            userMapper.updateById(currentUser);
        }
    }


    /**
     * 更新上次登录时间
     *
     * @param username 用户 ID
     */
    public void updateLastLoginTime(String username) {
        LambdaQueryWrapper<UserExt> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(UserExt::getUsername, username);
        UserExt user = getOne(userLambdaQueryWrapper);
        user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
        updateById(user);
    }
}
