<template>
  <el-card>
    <template #header>我的已选课程</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchSelected">刷新</el-button>
    </div>
    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="course.id" label="课程ID" width="90" />
      <el-table-column prop="course.courseNo" label="课程编号" width="120" />
      <el-table-column prop="course.courseName" label="课程名称" min-width="180" />
      <el-table-column prop="course.classTime" label="上课时间" min-width="140" />
      <el-table-column prop="course.classLocation" label="上课地点" min-width="140" />
      <el-table-column prop="selectionTime" label="选课时间" min-width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="warning" @click="drop(row.courseId)">退课</el-button>
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
const rows = ref([])

async function fetchSelected() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/my/selected')
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function drop(courseId) {
  try {
    await http.post('/course/v1/drop', null, { params: { courseId } })
    ElMessage.success('退课成功')
    await fetchSelected()
  } catch (e) {
    ElMessage.error(e?.message || '退课失败')
  }
}

onMounted(fetchSelected)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
}
</style>

