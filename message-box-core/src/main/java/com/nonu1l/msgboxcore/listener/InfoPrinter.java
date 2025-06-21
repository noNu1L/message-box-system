package com.nonu1l.msgboxcore.listener;

/**
 * 启动信息打印
 * @author zhonghanbo
 * @date 2025年06月21日 23:13
 */

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InfoPrinter {

    @Value("${ex.datasource.ip}")
    private String ip;

    @Value("${ex.datasource.port}")
    private int port;

    @Value("${ex.datasource.name}")
    private String name;

    @Value("${ex.datasource.username}")
    private String username;

    @PostConstruct
    public void printInfo() {
        log.info("=============================================");
        log.info("连接的数据库信息：");
        log.info("IP: {}", ip);
        log.info("端口: {}", port);
        log.info("库名: {}", name);
        log.info("用户名: {}", username);
        log.info("=============================================");

    }
}
