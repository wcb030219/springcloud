<template>
  <el-card>
    <template #header>个人课表（按时间地点聚合）</template>
    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchTimetable">刷新</el-button>
    </div>

    <el-empty v-if="!loading && items.length === 0" description="暂无已选课程" />

    <el-collapse v-else>
      <el-collapse-item v-for="(item, idx) in items" :key="idx" :name="idx">
        <template #title>
          <span>{{ item.classTime || '未填写上课时间' }}</span>
          <span style="margin-left: 12px; color: #6b7280">{{ item.classLocation || '未填写地点' }}</span>
          <span style="margin-left: 12px; color: #6b7280">({{ (item.courses || []).length }} 门)</span>
        </template>

        <el-table :data="item.courses || []" stripe>
          <el-table-column prop="id" label="课程ID" width="90" />
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          <el-table-column prop="courseName" label="课程名称" min-width="200" />
          <el-table-column prop="teacherId" label="教师ID" width="100" />
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const items = ref([])

async function fetchTimetable() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/my/timetable')
    items.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchTimetable)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
}
</style>

