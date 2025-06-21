package com.nonu1l.msgboxcore.controller;

import com.nonu1l.commons.utils.Result;
import com.nonu1l.msgboxcore.dto.LoginRequest;
import com.nonu1l.msgboxcore.dto.UserDto;
import com.nonu1l.msgboxcore.entity.UserExt;
import com.nonu1l.msgboxcore.service.UserService;
import com.nonu1l.msgboxcore.utils.CryptoUtils;
import com.nonu1l.msgboxcore.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * @author zhonghanbo
 * @date 2024年11月20日 17:38
 */

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;


    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String username = CryptoUtils.decrypt(loginRequest.getUsername());
        String password = CryptoUtils.decrypt(loginRequest.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        UserExt user = (UserExt) authentication.getPrincipal();
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);

        userService.updateLastLoginTime(username);

        return Result.ok(userVo);
    }

    @PostMapping("/update")
    public Result<String> getSetting(@RequestBody UserDto userDto) {
        userService.update(userDto);
        return Result.ok("更新成功");
    }
}
