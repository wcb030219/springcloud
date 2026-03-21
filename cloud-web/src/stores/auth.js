import { defineStore } from 'pinia'
import axios from 'axios'

const STORAGE_KEY = 'cloud-web-auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    userId: '',
    role: '',
    username: '',
    realName: '',
    token: '',
  }),
  actions: {
    loadFromStorage() {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (!raw) return
      try {
        const data = JSON.parse(raw)
        this.userId = String(data.userId || '')
        this.role = String(data.role || '')
        this.username = String(data.username || '')
        this.realName = String(data.realName || '')
        this.token = String(data.token || '')
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    },
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          userId: this.userId,
          role: this.role,
          username: this.username,
          realName: this.realName,
          token: this.token,
        }),
      )
    },
    isLoggedIn() {
      return Boolean(this.userId) && Boolean(this.role) && Boolean(this.token)
    },
    async login(username, password) {
      const res = await axios.post('/user/v1/login', { username, password })
      const payload = res?.data
      if (!payload || typeof payload.code !== 'number') {
        throw new Error('登录失败')
      }
      if (payload.code !== 200) {
        throw new Error(payload.message || '登录失败')
      }
      const data = payload.data || {}
      this.userId = String(data.userId || '')
      this.role = String(data.role || '')
      this.username = String(data.username || '')
      this.realName = String(data.realName || '')
      this.token = String(data.token || '')
      this.persist()
      return payload
    },
    logout() {
      this.userId = ''
      this.role = ''
      this.username = ''
      this.realName = ''
      this.token = ''
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
