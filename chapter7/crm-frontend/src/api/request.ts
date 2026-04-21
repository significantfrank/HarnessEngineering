import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({
  baseURL: '',
  timeout: 10000,
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== '200') {
      message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    message.error(error.message || '网络错误')
    return Promise.reject(error)
  },
)

export default request
