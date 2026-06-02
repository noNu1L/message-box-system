# 消息盒子 (Message Box System)

灵活的消息推送与管理平台，通过统一 HTTP API 接收消息，根据通道配置分发给邮件或桌面客户端。

![img.png](https://github.com/noNu1L/message-box-system/blob/master/document/images/v0.1.0.png?raw=true)

---

## 环境要求

- JDK 17
- Maven
- Node.js 18+
- Rust (桌面客户端构建)
- MySQL 8.0+

---

## 快速开始

```bash
# 克隆
git clone <your-repository-url>
cd message-box-system

# 数据库
# 在 MySQL 创建数据库后，导入 document/初始化SQL.sql
# 修改 message-box-core/src/main/resources/application.yaml 中的数据库连接信息

# 启动后端 (端口 8675)
cd message-box-core
mvn spring-boot:run

# 启动 Web 管理后台 (端口 8080)
cd ../message-box-web
npm install
npm run serve

# 启动桌面客户端
cd ../message-box-client
npm install
npm run tauri-dev
```

### 使用步骤

1. 打开 Web 管理后台，配置 SMTP 发件服务和接收组
2. 在桌面客户端中输入 `ChannelCode` 连接 WebSocket
3. 调用 HTTP API 推送消息：`GET/POST /msg/{ChannelCode}?title=&content=`

---

## Acknowledgement

This project is supported by [JetBrains](https://www.jetbrains.com/) with a free Open Source license.

[![JetBrains](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/?from=message-box-system)
