<template>
  <el-card>
    <template #header>我的成绩</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchGrades">刷新</el-button>
    </div>
    <el-table :data="grades" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="course.courseName" label="课程" min-width="180" />
      <el-table-column prop="course.credit" label="学分" width="90" />
      <el-table-column prop="gradeScore" label="成绩" width="110" />
      <el-table-column prop="remarks" label="备注" min-width="200" />
      <el-table-column prop="createTime" label="录入时间" min-width="180" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const grades = ref([])

async function fetchGrades() {
  loading.value = true
  try {
    const res = await http.get('/evaluation/v1/grade/my')
    grades.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchGrades)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
}
</style>
