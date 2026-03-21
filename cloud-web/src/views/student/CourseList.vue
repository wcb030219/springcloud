<template>
  <el-card>
    <template #header>可选课程（已上架）</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchCourses">刷新</el-button>
    </div>
    <el-table :data="courses" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="courseNo" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="teacherId" label="教师ID" width="100" />
      <el-table-column prop="capacity" label="容量" width="90" />
      <el-table-column prop="selectedCount" label="已选" width="90" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="selectCourse(row.id)">选课</el-button>
          <el-button size="small" type="warning" @click="dropCourse(row.id)">退课</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const courses = ref([])

async function fetchCourses() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/list')
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function selectCourse(courseId) {
  try {
    await http.post('/course/v1/select', null, { params: { courseId } })
    ElMessage.success('选课成功')
    await fetchCourses()
  } catch (e) {
    ElMessage.error(e?.message || '选课失败')
  }
}

async function dropCourse(courseId) {
  try {
    await http.post('/course/v1/drop', null, { params: { courseId } })
    ElMessage.success('退课成功')
    await fetchCourses()
  } catch (e) {
    ElMessage.error(e?.message || '退课失败')
  }
}

onMounted(fetchCourses)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
}
</style>

