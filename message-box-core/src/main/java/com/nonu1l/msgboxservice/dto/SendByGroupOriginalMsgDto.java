package com.nonu1l.msgboxservice.dto;

import lombok.Data;


/**
 * 邮件发送Dto
 * @author nonu1l
 * @date 2024年11月07日 14:37
 */

@Data
public class SendByGroupOriginalMsgDto {

    // key
    private String channelCode;

    private String subject;

    private String content;


    public SendByGroupOriginalMsgDto(String channelCode, String subject, String content) {
        this.channelCode = channelCode;
        this.subject = subject;
        this.content = content;
    }
}
