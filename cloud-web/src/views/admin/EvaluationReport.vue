<template>
  <el-row :gutter="12">
    <el-col :span="14">
      <el-card>
        <template #header>课程评教均分榜</template>
        <div class="actions">
          <el-button type="primary" :loading="loading" @click="fetchAvg">刷新</el-button>
        </div>
        <el-table :data="rows" v-loading="loading" stripe @row-click="openStats">
          <el-table-column prop="courseId" label="课程ID" width="100" />
          <el-table-column prop="courseName" label="课程名称" min-width="220" />
          <el-table-column prop="avgScore" label="均分" width="120">
            <template #default="{ row }">{{ formatScore(row.avgScore) }}</template>
          </el-table-column>
          <el-table-column prop="count" label="评教人数" width="120" />
        </el-table>
        <div class="hint">点击某行查看该课程分布统计</div>
      </el-card>
    </el-col>

    <el-col :span="10">
      <el-card>
        <template #header>说明</template>
        <div class="desc">
          <div>可视化：用分段统计 + 进度条展示评分分布。</div>
          <div>分段：0-1.5、1.5-2.5、2.5-3.5、3.5-4.5、4.5-5。</div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="dialogVisible" title="评教分布统计" width="520px">
    <el-descriptions v-if="stats" :column="2" border>
      <el-descriptions-item label="课程">{{ stats.courseName || stats.courseId }}</el-descriptions-item>
      <el-descriptions-item label="评教人数">{{ stats.count }}</el-descriptions-item>
      <el-descriptions-item label="均分">{{ formatScore(stats.avgScore) }}</el-descriptions-item>
      <el-descriptions-item label="最低/最高">{{ formatScore(stats.minScore) }} / {{ formatScore(stats.maxScore) }}</el-descriptions-item>
    </el-descriptions>

    <div v-if="distRows.length" style="margin-top: 12px">
      <div v-for="d in distRows" :key="d.range" class="dist-row">
        <div class="dist-label">{{ d.range }}</div>
        <el-progress :percentage="d.percent" :stroke-width="12" />
        <div class="dist-count">{{ d.count }}</div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const rows = ref([])
const courseMap = ref(new Map())

const dialogVisible = ref(false)
const stats = ref(null)
const distRows = ref([])

function formatScore(v) {
  if (v === null || v === undefined) return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

async function fetchCourses() {
  try {
    const res = await http.get('/course/v1/admin/list')
    const list = Array.isArray(res.data) ? res.data : []
    const m = new Map()
    list.forEach((c) => {
      if (c && c.id != null) m.set(Number(c.id), c)
    })
    courseMap.value = m
  } catch {
    courseMap.value = new Map()
  }
}

async function fetchAvg() {
  loading.value = true
  try {
    await fetchCourses()
    const res = await http.get('/evaluation/v1/admin/report/courseAvg', { params: { top: 50 } })
    const list = Array.isArray(res.data) ? res.data : []
    rows.value = list.map((r) => {
      const courseId = Number(r.courseId)
      const c = courseMap.value.get(courseId)
      return {
        ...r,
        courseId,
        courseName: c ? c.courseName : '',
      }
    })
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function buildDistRows(distribution, total) {
  const keys = Object.keys(distribution || {})
  return keys.map((k) => {
    const count = Number(distribution[k] || 0)
    const percent = total === 0 ? 0 : Math.round((count / total) * 100)
    return { range: k, count, percent }
  })
}

async function openStats(row) {
  try {
    const courseId = row.courseId
    const res = await http.get('/evaluation/v1/admin/report/courseStats', { params: { courseId } })
    const data = res.data || null
    if (!data) return
    const c = courseMap.value.get(Number(courseId))
    stats.value = { ...data, courseName: c ? c.courseName : '' }
    distRows.value = buildDistRows(stats.value.distribution, Number(stats.value.count || 0))
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  }
}

onMounted(fetchAvg)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}
.desc {
  color: #374151;
  line-height: 1.8;
}
.dist-row {
  display: grid;
  grid-template-columns: 120px 1fr 60px;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.dist-label {
  color: #374151;
}
.dist-count {
  text-align: right;
  color: #6b7280;
}
</style>

