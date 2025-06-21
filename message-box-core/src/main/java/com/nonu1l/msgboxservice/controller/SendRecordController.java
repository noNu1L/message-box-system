package com.nonu1l.msgboxservice.controller;


import com.nonu1l.commons.utils.Result;
import com.nonu1l.msgboxservice.dto.SendRecordViewDto;
import com.nonu1l.msgboxservice.service.SendRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;


/**
 * 发送记录控制器
 *
 * @author nonu1l
 * @date 2025/01/05
 */
@RestController
@RequestMapping("/sendRecord")
public class SendRecordController {

    @Autowired
    private SendRecordService sendRecordService ;
    /**
     * 重新获取通道代码
     *
     * @param request 请求
     * @return {@link Result }<{@link String }>
     */
    @PostMapping("/pull")
    public Result<HashMap<String, List<SendRecordViewDto>>> getSendRecord(HttpServletRequest request) {
        Result<HashMap<String, List<SendRecordViewDto>>> sendRecord = sendRecordService.getSendRecord();
        return sendRecord;
    }
}
