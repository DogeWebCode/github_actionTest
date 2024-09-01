import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import vue3GoogleLogin from 'vue3-google-login'
import 'element-plus/dist/index.css'
import router from './router'

const app = createApp(App)

app.use(ElementPlus)

app.use(router)

app.use(vue3GoogleLogin, {
  clientId: '402282197504-i03vqslflc0nbv4ttobahhm36c20qlma.apps.googleusercontent.com'
})

app.mount('#app')
