<template>
  <router-view v-if="isLoginPage" />
  <el-container v-else class="app-container">
    <el-header class="app-header">
      <div class="app-title">学生选课与教学评估系统</div>
      <div class="app-user">
        <span class="app-user-name">{{ displayName }}</span>
        <el-button size="small" @click="handleLogout">退出</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu :default-active="route.path" router>
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/courses">学生-课程</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/selected">学生-已选课程</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/drops">学生-退课记录</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/timetable">学生-个人课表</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/grades">学生-成绩</el-menu-item>
          <el-menu-item v-if="role === '3'" index="/student/evaluation">学生-评教</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/courses">管理员-课程管理</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/users">管理员-用户管理</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/categories">管理员-课程分类</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/stats">管理员-数据统计</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/evaluation/templates">管理员-评估模板</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/evaluation/questions">管理员-评估题库</el-menu-item>
          <el-menu-item v-if="role === '1'" index="/admin/evaluation/report">管理员-评估报表</el-menu-item>
          <el-menu-item v-if="role === '2'" index="/teacher/courses">教师-我的课程</el-menu-item>
          <el-menu-item v-if="role === '2'" index="/teacher/grade">教师-录入成绩</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

if (!auth.userId && !auth.role) auth.loadFromStorage()

const isLoginPage = computed(() => route.path === '/login')
const role = computed(() => String(auth.role || ''))
const displayName = computed(() => auth.realName || auth.username || `用户${auth.userId || ''}`)

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  height: 100vh;
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}
.app-title {
  font-weight: 600;
}
.app-user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.app-user-name {
  color: #374151;
}
.app-aside {
  border-right: 1px solid #e5e7eb;
}
.app-main {
  background: #f8fafc;
}
</style>
