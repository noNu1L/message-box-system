import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate';
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import index from './router';


const app = createApp(App)
const pinia = createPinia()

// 禁用生产模式提示
app.config.productionTip = false;

app.config.globalProperties.$apiUrl = process.env.VUE_APP_API_URL;
// 使用持久化插件
pinia.use(piniaPluginPersistedState);

app.use(pinia) // 使用pinia
app.use(index) // 使用路由配置
app.use(ElementPlus)
app.mount('#app')

