package com.nonu1l.msgboxcore.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * SMTP 服务器
 *
 * @author nonu1l
 * @date 2024年11月07日 16:36
 */

@Data
@AllArgsConstructor
@TableName("ex_smtp_server")
public class SmtpServer {
    @TableId
    private String emailServerId;
    private String serverName;
    private String serverAddress;
    private Integer serverPort;
} 