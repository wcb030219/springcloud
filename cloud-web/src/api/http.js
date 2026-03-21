import axios from 'axios'
import { useAuthStore } from '../stores/auth'

export const http = axios.create({
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers = {
      ...(config.headers || {}),
      Authorization: `Bearer ${auth.token}`,
    }
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response?.data
    if (payload && typeof payload.code === 'number') {
      if (payload.code === 200) return payload
      return Promise.reject(new Error(payload.message || '请求失败'))
    }
    return payload
  },
  (error) => Promise.reject(error),
)

