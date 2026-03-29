<template>
  <el-card>
    <template #header>课程列表</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchCourses">刷新</el-button>
    </div>

    <div class="section-title">必修课</div>
    <el-table :data="requiredCourses" v-loading="loadingRequired" stripe>
      <el-table-column prop="courseNo" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="teacherName" label="教师" width="140" />
      <el-table-column prop="classTime" label="上课时间" min-width="140" />
      <el-table-column prop="classLocation" label="上课地点" min-width="140" />
      <el-table-column prop="credit" label="学分" width="90" />
    </el-table>

    <el-divider />

    <div class="section-title">选修课（可选）</div>
    <el-table :data="courses" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="courseNo" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="teacherName" label="教师" width="140" />
      <el-table-column prop="classTime" label="上课时间" min-width="140" />
      <el-table-column prop="classLocation" label="上课地点" min-width="140" />
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
const loadingRequired = ref(false)
const requiredCourses = ref([])

async function fetchRequired() {
  loadingRequired.value = true
  try {
    const res = await http.get('/course/v1/my/required')
    requiredCourses.value = Array.isArray(res.data) ? res.data : []
  } catch {
    requiredCourses.value = []
  } finally {
    loadingRequired.value = false
  }
}

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
  fetchRequired()
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
.section-title {
  font-weight: 600;
  margin: 8px 0;
}
</style>
