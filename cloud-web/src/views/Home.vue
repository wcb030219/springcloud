<template>
  <el-card>
    <template #header>当前登录信息</template>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户ID">{{ auth.userId }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ auth.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ auth.realName }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const roleLabel = computed(() => {
  const role = String(auth.role || '')
  if (role === '1') return '管理员'
  if (role === '2') return '教师'
  if (role === '3') return '学生'
  return role || '-'
})
</script>

