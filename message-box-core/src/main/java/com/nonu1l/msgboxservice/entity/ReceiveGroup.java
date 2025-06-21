package com.nonu1l.msgboxservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 通用接收组
 *
 * @author nonu1l
 * @date 2024年11月07日 16:36
 */

@Data
@TableName("ex_receive_group")
public class ReceiveGroup {
    @TableId
    private String groupId;
    private String userId;
    private String channelCode;
    private String receiveMode;
    private String name;
    private String description;
    private String receiveEmails;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int status;
} 