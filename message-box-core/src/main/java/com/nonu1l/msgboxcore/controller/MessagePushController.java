package com.nonu1l.msgboxcore.controller;

import com.nonu1l.commons.utils.HttpUtils;
import com.nonu1l.commons.utils.MapUtils;
import com.nonu1l.commons.utils.Result;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.msgboxcore.dto.SendByGroupOriginalMsgDto;
import com.nonu1l.msgboxcore.service.MessagePushService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * 消息推送 Controller
 *
 * @author nonu1l
 * @date 2024年10月25日 9:55
 */

@RestController
@RequestMapping(value = "/push")
public class MessagePushController {

    private final MessagePushService messagePushService;
    private static final Logger log = LoggerFactory.getLogger(MessagePushController.class);

    public MessagePushController(MessagePushService messagePushService) {
        this.messagePushService = messagePushService;
    }

    private Result<String> processSend(String channelCode, String subject, String content) {
        if (ValidateUtils.isEmpty(channelCode)) {
            return Result.fail("channelCode不能为空");
        }
        if (ValidateUtils.isEmpty(subject)) {
            return Result.fail("subject不能为空");
        }
        if (ValidateUtils.isEmpty(content)) {
            return Result.fail("content不能为空");
        }
        SendByGroupOriginalMsgDto mailSendDto = new SendByGroupOriginalMsgDto(channelCode, subject, content);
        return messagePushService.sendByGroup(mailSendDto);
    }


    /**
     * Get 发送消息
     * url :/push/channelCode?subject=xxx&content=xxx
     *
     * @param request 请求
     * @return {@link Result }<{@link String }>
     */
    @GetMapping("/{channelCode}")
    public Result<String> sendGroupOfGet2(HttpServletRequest request,
                                          @PathVariable("channelCode") String channelCode) {
        Map<String, Object> paramMap = HttpUtils.getParamMap(request);
        String channelCodeByContent = MapUtils.getStringValueIgnoreCase(paramMap, "channelCode");
        String subject = MapUtils.getStringValueIgnoreCase(paramMap, "subject");
        String content = MapUtils.getStringValueIgnoreCase(paramMap, "content");

        if (ValidateUtils.isNotEmpty(channelCodeByContent) && !channelCode.equals(channelCodeByContent)) {
            return Result.fail("请求正文的channelCode与路径channelCode不一致，请移除正文channelCode或路径channelCode");
        }
        return processSend(channelCode, subject, content);
    }

    /**
     * Post 发送消息 1
     * url :/push
     * postData :{"channelCode":"xxx","subject":"xxx","content":"xxx"}
     *
     * @param requestBody 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping
    public Result<String> sendGroupOfPost(@RequestBody Map<String, Object> requestBody) {
        String channelCode = MapUtils.getStringValueIgnoreCase(requestBody, "channelCode");
        String subject = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String content = MapUtils.getStringValueIgnoreCase(requestBody, "content");
        return processSend(channelCode, subject, content);
    }


    /**
     * Post 发送消息 2
     * url :/push/channelCode
     * postData :{"subject":"xxx","content":"xxx"}
     *
     * @param requestBody 请求
     * @param channelCode channelCode
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/{channelCode}")
    public Result<String> sendGroupOfPost(@RequestBody Map<String, Object> requestBody,
                                          @PathVariable("channelCode") String channelCode) {
        String subject = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String content = MapUtils.getStringValueIgnoreCase(requestBody, "content");
        return processSend(channelCode, subject, content);
    }


    /**
     * 发送 post2 组 (表单方式)
     *
     * @param requestBody 请求正文
     * @param channelCode     通道代码
     * @return {@link Result }<{@link String }>
     */
    @PostMapping(value = "/{channelCode}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result<String> sendGroupOfPost2(@RequestParam MultiValueMap<String, String> requestBody,
                                           @PathVariable("channelCode") String channelCode) {
        String subject = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String content = MapUtils.getStringValueIgnoreCase(requestBody, "content");

        // 安全地截取[]
        if (subject != null && subject.startsWith("[") && subject.endsWith("]")) {
            subject = subject.substring(1, subject.length() - 1);
        }
        if (content != null && content.startsWith("[") && content.endsWith("]")) {
            content = content.substring(1, content.length() - 1);
        }
        return processSend(channelCode, subject, content);
    }


    /**
     * 测试 send config
     *
     * @param request 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/testSendConfig")
    public Result<String> testSendConfig(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        String email = MapUtils.getStringValueIgnoreCase(requestBody, "email");
        String subject = MapUtils.getStringValueIgnoreCase(requestBody, "subject");
        String content = MapUtils.getStringValueIgnoreCase(requestBody, "content");
        try {
            String userId = MapUtils.getStringValueIgnoreCase(requestBody, "userId");
            return messagePushService.testSendConfig(userId, email, subject, content);
        } catch (Exception e) {
            log.error("测试邮件发送配置时出错", e);
            return Result.fail("发送失败：" + e.getMessage());
        }
    }


} 