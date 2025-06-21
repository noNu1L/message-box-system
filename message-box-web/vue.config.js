const { defineConfig } = require('@vue/cli-service')
const { WEB_SERVICE_URL,SEND_SERVICE_URL } = require('./src/config/index');
// import serverConfig from '@/config/index'
module.exports = defineConfig({
  transpileDependencies: true
})

module.exports = {
  devServer: {
    proxy: {
      '/web': {
        target: WEB_SERVICE_URL,
        // 允许跨域
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          '^/web': ''
        }
      },
      '/send': {
        target: SEND_SERVICE_URL,
        // 允许跨域
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          '^/send': ''
        }
      }
    }
  }
}
