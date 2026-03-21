<template>
  <el-row :gutter="12">
    <el-col :span="8">
      <el-card>
        <template #header>概览</template>
        <el-button type="primary" :loading="loadingOverview" @click="fetchOverview">刷新</el-button>
        <el-descriptions v-if="overview" :column="1" border style="margin-top: 12px">
          <el-descriptions-item label="课程总数">{{ overview.totalCourses }}</el-descriptions-item>
          <el-descriptions-item label="上架课程">{{ overview.onlineCourses }}</el-descriptions-item>
          <el-descriptions-item label="下架课程">{{ overview.offlineCourses }}</el-descriptions-item>
          <el-descriptions-item label="选课总次数">{{ overview.totalSelected }}</el-descriptions-item>
          <el-descriptions-item label="评教总次数">{{ overview.totalEvaluations }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>

    <el-col :span="16">
      <el-card>
        <template #header>热门课程（按已选人数）</template>
        <div class="actions">
          <el-button type="primary" :loading="loadingHot" @click="fetchHot">刷新</el-button>
        </div>
        <el-table :data="hotCourses" v-loading="loadingHot" stripe>
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          <el-table-column prop="courseName" label="课程名称" min-width="200" />
          <el-table-column prop="teacherId" label="教师ID" width="110" />
          <el-table-column prop="selectedCount" label="已选" width="90" />
          <el-table-column prop="capacity" label="容量" width="90" />
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-card style="margin-top: 12px">
    <template #header>课程评教均分（按课程聚合）</template>
    <div class="actions">
      <el-button type="primary" :loading="loadingEvalAvg" @click="fetchEvalAvg">刷新</el-button>
    </div>
    <el-table :data="evalAvg" v-loading="loadingEvalAvg" stripe>
      <el-table-column prop="courseId" label="课程ID" width="100" />
      <el-table-column prop="courseNo" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="220" />
      <el-table-column prop="avgScore" label="均分" width="120">
        <template #default="{ row }">
          <span>{{ formatScore(row.avgScore) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="count" label="评教人数" width="120" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loadingOverview = ref(false)
const overview = ref(null)

const loadingHot = ref(false)
const hotCourses = ref([])

const loadingEvalAvg = ref(false)
const evalAvg = ref([])

function formatScore(v) {
  if (v === null || v === undefined) return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

async function fetchOverview() {
  loadingOverview.value = true
  try {
    const res = await http.get('/course/v1/admin/stats/overview')
    overview.value = res.data || null
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingOverview.value = false
  }
}

async function fetchHot() {
  loadingHot.value = true
  try {
    const res = await http.get('/course/v1/admin/stats/hotCourses', { params: { top: 10 } })
    hotCourses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingHot.value = false
  }
}

async function fetchEvalAvg() {
  loadingEvalAvg.value = true
  try {
    const res = await http.get('/course/v1/admin/stats/evaluationAvg', { params: { top: 20 } })
    evalAvg.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingEvalAvg.value = false
  }
}

onMounted(() => {
  fetchOverview()
  fetchHot()
  fetchEvalAvg()
})
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>

