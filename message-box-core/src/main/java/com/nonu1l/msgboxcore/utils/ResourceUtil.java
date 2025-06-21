package com.nonu1l.msgboxcore.utils;

import com.nonu1l.msgboxcore.entity.UserExt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * 资源工具类
 *
 * @author zhonghanbo
 * @date 2024年11月26日 17:17
 */
@Component
public class ResourceUtil {

    /**
     * 获取会话用户
     *
     * @return {@link UserExt }
     */
    public static UserExt getSessionUser() {
        UserExt user = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            user = (UserExt) authentication.getPrincipal();
        } catch (Exception e) {
            throw new RuntimeException("无法获取当前用户信息");
        }
        return user;

    }

    /**
     * 获取请求 URL
     *
     * @return {@link String }
     */
    public static String getRequestURL(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        return request.getRequestURL().toString();
    }


    /**
     * 获取请求 URI
     *
     * @return {@link String }
     */
    public static String getRequestURI(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        return request.getRequestURI();
    }

}
