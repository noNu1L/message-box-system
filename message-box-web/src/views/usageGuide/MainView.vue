<template>
  <el-card class="usage-guide-card">
    <template #header>
      <div class="card-header">
        <span><el-icon><Compass /></el-icon> 食用指南</span>
      </div>
    </template>

    <el-collapse v-model="activeNames">
      <el-collapse-item title="配置教程（点击展开/收起）" name="1">
        <div>
          <h3>1. 发送配置</h3>
          <p>系统需要配置一个默认的发件邮箱，用于发送所有邮件。请在 <strong>【收发配置】->【发送配置】</strong> 页面进行设置。</p>
          <ul>
            <li><strong>邮箱服务器(SMTP)</strong>: 您的邮件服务提供商的SMTP服务器地址，例如 <code>smtp.qq.com</code>。</li>
            <li><strong>发件地址</strong>: 您用来发送邮件的完整邮箱地址，例如 <code>example@qq.com</code>。</li>
            <li><strong>邮箱授权码</strong>: 这不是您的邮箱登录密码，而是由邮箱服务商生成的用于第三方客户端登录的专用密码。</li>
          </ul>
          <p>保存后，该邮箱将作为所有消息推送的默认发件人。</p>
        </div>
        <div>
          <h3>2. 接收配置</h3>
          <p>接收配置用于管理将消息推送到哪里。您可以创建多个“接收组”，每个组都由一个唯一的 <strong>通道代码 (ChannelCode)</strong> 标识。</p>
          <ul>
            <li>在 <strong>【收发配置】->【接收配置】</strong> 页面，您可以新增或管理接收组。</li>
            <li>每个接收组可以关联一个或多个收件地址（目前支持邮箱）。</li>
            <li>您可以为每个接收组设置接收方式：
              <ul>
                <li><strong>邮箱接收</strong>: 消息将作为邮件发送到该组配置的收件地址。</li>
                <li><strong>客户端接收</strong>: 消息将被推送到已通过对应ChannelCode连接的桌面客户端。</li>
              </ul>
            </li>
          </ul>
          <p>简而言之，<strong>ChannelCode</strong> 是连接消息来源和最终接收者的桥梁。</p>
        </div>
        <div>
          <h3>3. 客户端使用</h3>
          <p>我们提供了一个桌面客户端程序，可以实时接收消息通知。</p>
          <ul>
            <li>客户端通过您在接收配置中设置的 <strong>ChannelCode</strong> 进行连接。</li>
            <li>连接成功后，任何推送到该ChannelCode的消息，如果接收方式包含“客户端接收”，都会被客户端捕获并弹出通知。</li>
          </ul>
        </div>
      </el-collapse-item>

      <el-collapse-item title="消息推送API（点击展开/收起）" name="2">
        <p>系统提供了多种方式来通过HTTP请求推送消息，您只需调用相应的API并提供正确的 <strong>ChannelCode</strong>、<strong>标题(subject)</strong> 和 <strong>内容(content)</strong>。</p>

        <div>
          <h4>GET 请求</h4>
          <p>通过 URL 参数传递所有信息。</p>
          <pre><code>GET /push/{channelCode}?subject=你的标题&content=你的内容</code></pre>
          <p><strong>示例:</strong> <code>/push/ch_123456?subject=测试&content=这是一条测试消息</code></p>
        </div>

        <div>
          <h4>POST 请求 (JSON)</h4>
          <p>通过 JSON 请求体发送，有两种形式：</p>
          <p><strong>方式一:</strong> ChannelCode 在 Body 中。</p>
          <pre><code>POST /push
Content-Type: application/json

{
  "channelCode": "你的ChannelCode",
  "subject": "你的标题",
  "content": "你的内容"
}</code></pre>
          <p><strong>方式二:</strong> ChannelCode 在 URL 中。</p>
          <pre><code>POST /push/{channelCode}
Content-Type: application/json

{
  "subject": "你的标题",
  "content": "你的内容"
}</code></pre>
        </div>

        <div>
          <h4>POST 请求 (表单)</h4>
          <p>通过 `application/x-www-form-urlencoded` 表单形式提交。</p>
          <pre><code>POST /push/{channelCode}
Content-Type: application/x-www-form-urlencoded

subject=你的标题&content=你的内容</code></pre>
        </div>
      </el-collapse-item>
    </el-collapse>

  </el-card>
</template>

<script setup>
import { ref } from 'vue';
import { Compass } from '@element-plus/icons-vue';

const activeNames = ref(['1']); // 默认展开配置教程
</script>

<style scoped>
.usage-guide-card {
  margin: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 1.2em;
}

.card-header .el-icon {
  margin-right: 8px;
}

h3 {
  margin-top: 15px;
  margin-bottom: 10px;
  border-left: 4px solid #409EFF;
  padding-left: 8px;
}

p, li {
  line-height: 1.6;
  color: #606266;
}

ul {
  padding-left: 20px;
}

pre {
  background-color: #f4f4f5;
  border-radius: 4px;
  padding: 16px;
  color: #333;
  font-family: 'Courier New', Courier, monospace;
  overflow-x: auto;
}

code {
  font-family: inherit;
}
</style>