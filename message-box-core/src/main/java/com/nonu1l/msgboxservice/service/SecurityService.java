package com.nonu1l.msgboxservice.service;

import com.nonu1l.msgboxservice.entity.UserExt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 安全服务
 *
 * @author zhonghanbo
 * @date 2024年12月30日 20:55
 */

@Service
public class SecurityService {

    private final UserService userService;

    public SecurityService(UserService userService) {
        this.userService = userService;
    }

    public void refreshSession(UserExt updatedUser) {
        // 加载最新的用户详情
        UserDetails userDetails = userService.loadUserByUsername(updatedUser.getUsername());

        // 创建一个新的认证令牌
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(userDetails, updatedUser.getPassword(), userDetails.getAuthorities());

        // 获取当前会话
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        HttpSession session = request.getSession();
        // 将新的认证信息存储到会话中
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(newAuth);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // 设置新的认证信息到当前安全上下文中
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

}
