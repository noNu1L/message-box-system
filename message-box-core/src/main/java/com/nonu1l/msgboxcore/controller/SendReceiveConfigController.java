package com.nonu1l.msgboxcore.controller;

import com.nonu1l.commons.utils.HttpUtils;
import com.nonu1l.commons.utils.MapUtils;
import com.nonu1l.commons.utils.Result;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.msgboxcore.dto.SendReceiveConfigDto;
import com.nonu1l.msgboxcore.entity.UserExt;
import com.nonu1l.msgboxcore.service.MessagePushService;
import com.nonu1l.msgboxcore.service.SendReceiveConfigService;
import com.nonu1l.msgboxcore.utils.ResourceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 收发设置
 *
 * @author zhonghanbo
 * @date 2024年11月18日 0:36
 */

@RestController
@RequestMapping("/sendReceiveConfig")
public class SendReceiveConfigController {

    private final SendReceiveConfigService sendReceiveConfigService;
    private final MessagePushService messagePushService;

    public SendReceiveConfigController(SendReceiveConfigService sendReceiveConfigService, MessagePushService messagePushService) {
        this.sendReceiveConfigService = sendReceiveConfigService;
        this.messagePushService = messagePushService;
    }


    /**
     * 获取电子邮件服务器
     *
     * @return {@link Result }<{@link SendReceiveConfigDto.SendConfig }>
     */
    @PostMapping("/getSmtpServer")
    public Result<List<HashMap<String, String>>> getSmtpServer() {
        List<HashMap<String, String>> emailServerList = sendReceiveConfigService.getSmtpServer();
        return Result.ok(emailServerList);
    }

    /**
     * 获取发送配置
     *
     * @param httpRequest HTTP 请求
     * @return {@link Result }<{@link SendReceiveConfigDto.SendConfig }>
     */
    @PostMapping("/getSendConfig")
    public Result<SendReceiveConfigDto.SendConfig> getSendConfig(HttpServletRequest httpRequest) {
        SendReceiveConfigDto.SendConfig sendConfig = sendReceiveConfigService.getSendConfig();
        return Result.ok(sendConfig);
    }


    /**
     * 获取接收配置
     *
     * @param httpRequest HTTP 请求
     * @return {@link Result }<{@link List }<{@link SendReceiveConfigDto.ReceiveConfig }>>
     */
    @PostMapping("/getReceiveConfig")
    public Result<List<SendReceiveConfigDto.ReceiveConfig>> getReceiveConfig(HttpServletRequest httpRequest) {
        List<SendReceiveConfigDto.ReceiveConfig> receiveConfig = sendReceiveConfigService.getReceiveConfig();
        return Result.ok(receiveConfig);
    }


    /**
     * 更新发送配置
     *
     * @param sendConfig  发送配置
     * @param httpRequest HTTP 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/updateSendConfig")
    public Result<String> updateSendConfig(@Valid @RequestBody SendReceiveConfigDto.SendConfig sendConfig, HttpServletRequest httpRequest) {
        sendReceiveConfigService.updateSendConfig(sendConfig);
        return Result.ok("保存成功");
    }


    /**
     * 更新接收配置
     *
     * @param receiveConfig 接收配置
     * @param httpRequest   HTTP 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/updateReceiveConfig")
    public Result<String> updateReceiveConfig(@Valid @RequestBody SendReceiveConfigDto.ReceiveConfig receiveConfig, HttpServletRequest httpRequest) {
        sendReceiveConfigService.updateReceiveConfig(receiveConfig);
        return Result.ok("保存成功");
    }


    /**
     * 删除接收配置
     *
     * @param receiveConfig 接收配置
     * @param httpRequest   HTTP 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/deleteReceiveConfig")
    public Result<String> deleteReceiveConfig(@RequestBody SendReceiveConfigDto.ReceiveConfig receiveConfig, HttpServletRequest httpRequest) {
        sendReceiveConfigService.deleteReceiveConfig(receiveConfig);
        return Result.ok("删除成功");
    }


    /**
     * 测试 send config
     *
     * @param request 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/testSendConfig")
    public Result<String> testSendConfig(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Map<String, Object> paramMap = HttpUtils.getParamMap(request);
        String email = MapUtils.getStringValueIgnoreCase(requestBody, "email");
        String subject = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String content = MapUtils.getStringValueIgnoreCase(requestBody, "content");
        UserExt sessionUser = ResourceUtil.getSessionUser();
        String userId = sessionUser.getUserId();

        if (ValidateUtils.isEmpty(email) || ValidateUtils.isEmpty(subject) || ValidateUtils.isEmpty(content)) {
            return Result.fail("参数不能为空");
        }
        Result<String> stringResult = messagePushService.testSendConfig(userId, email, subject, content);
        if (stringResult.isSuccess()) {
            return Result.ok("邮件投递成功，请前往发送记录进行查询投递情况");
        } else {
            return Result.fail("邮件投递失败");
        }
    }


} 