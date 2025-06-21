package com.nonu1l.msgboxcore.controller;

import com.nonu1l.commons.utils.Result;
import com.nonu1l.msgboxcore.service.ChannelCodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 通道代码控制器
 *
 * @author nonu1l
 * @date 2025/01/05
 */
@RestController
@RequestMapping("/channelCode")
public class ChannelCodeController {

    private final ChannelCodeService channelCodeService;

    public ChannelCodeController(ChannelCodeService channelCodeService) {
        this.channelCodeService = channelCodeService;
    }

    /**
     * 重新获取通道代码
     *
     * @param request 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/getNewChannelCode")
    public Result<String> getNewChannelCode(HttpServletRequest request) {
        String channelCode = channelCodeService.getNewChannelCode();
        Result<String> data = new Result<>();
        data.setSuccess(true);
        data.setData(channelCode);
        return data;
    }
}
