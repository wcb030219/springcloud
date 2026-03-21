<template>
  <el-card>
    <template #header>退课记录</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchDrops">刷新</el-button>
    </div>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="course.id" label="课程ID" width="90" />
      <el-table-column prop="course.courseNo" label="课程编号" width="120" />
      <el-table-column prop="course.courseName" label="课程名称" min-width="180" />
      <el-table-column prop="course.classTime" label="上课时间" min-width="140" />
      <el-table-column prop="course.classLocation" label="上课地点" min-width="140" />
      <el-table-column prop="dropTime" label="退课时间" min-width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const rows = ref([])

async function fetchDrops() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/my/drops')
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchDrops)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
}
</style>

