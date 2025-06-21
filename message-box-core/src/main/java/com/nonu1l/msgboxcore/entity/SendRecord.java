package com.nonu1l.msgboxcore.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;


/**
 * 发送记录
 *
 * @author nonu1l
 * @date 2024年11月07日 16:36
 */

@Data
@TableName("ex_send_record")
public class SendRecord {
    @TableId(value = "id")
    private String id;
    private String userId;
    private String sendEmail;
    private String receiveEmails;
    private String receiveMode;
    private String receiveMachineId;
    private String subject;
    private String channelCode;
    private String groupName;
    private String failMsg;
    private Timestamp createdAt;
    private Timestamp updatedAt;
} 