<template>
  <el-row :gutter="12">
    <el-col :span="10">
      <el-card>
        <template #header>我的授课课程</template>
        <div class="actions">
          <el-button type="primary" :loading="loadingCourses" @click="fetchCourses">刷新</el-button>
        </div>
        <el-table :data="courses" v-loading="loadingCourses" stripe highlight-current-row @current-change="handleSelectCourse">
          <el-table-column prop="id" label="课程ID" width="90" />
          <el-table-column prop="courseNo" label="编号" width="120" />
          <el-table-column prop="courseName" label="课程名称" min-width="160" />
          <el-table-column prop="classTime" label="时间" min-width="140" />
          <el-table-column prop="classLocation" label="地点" min-width="140" />
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="14">
      <el-card>
        <template #header>
          <div class="header-right">
            <span>课程详情</span>
            <span style="color:#6b7280">{{ selectedCourseLabel }}</span>
          </div>
        </template>

        <el-tabs v-model="tab">
          <el-tab-pane label="学生名单" name="students">
            <div class="actions">
              <el-button type="primary" :disabled="!selectedCourseId" :loading="loadingStudents" @click="fetchStudents">刷新</el-button>
            </div>
            <el-table :data="students" v-loading="loadingStudents" stripe>
              <el-table-column prop="id" label="学生ID" width="110" />
              <el-table-column prop="studentNo" label="学号" width="140" />
              <el-table-column prop="realName" label="姓名" min-width="160" />
              <el-table-column prop="username" label="账号" min-width="160" />
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="成绩管理" name="grades">
            <div class="actions">
              <el-button type="primary" :disabled="!selectedCourseId" :loading="loadingGrades" @click="fetchGrades">刷新</el-button>
              <el-button :disabled="!selectedCourseId" @click="downloadExport">导出CSV</el-button>
              <el-upload :auto-upload="false" :show-file-list="false" accept=".csv" :on-change="handleFileChange">
                <el-button :disabled="!selectedCourseId" type="success">导入CSV</el-button>
              </el-upload>
            </div>
            <el-table :data="grades" v-loading="loadingGrades" stripe>
              <el-table-column prop="studentId" label="学生ID" width="110" />
              <el-table-column prop="gradeScore" label="成绩" width="120" />
              <el-table-column prop="remarks" label="备注" min-width="200" />
              <el-table-column prop="updateTime" label="更新时间" min-width="180" />
            </el-table>
            <div class="hint">导入CSV格式：studentId,gradeScore,remarks（首行可带表头）</div>
          </el-tab-pane>

          <el-tab-pane label="评教统计" name="evaluation">
            <div class="actions">
              <el-button type="primary" :disabled="!selectedCourseId" :loading="loadingEval" @click="fetchEvaluation">刷新</el-button>
            </div>
            <el-descriptions v-if="stats" :column="2" border>
              <el-descriptions-item label="评教人数">{{ stats.count }}</el-descriptions-item>
              <el-descriptions-item label="平均分">{{ formatScore(stats.avgScore) }}</el-descriptions-item>
              <el-descriptions-item label="最低分">{{ formatScore(stats.minScore) }}</el-descriptions-item>
              <el-descriptions-item label="最高分">{{ formatScore(stats.maxScore) }}</el-descriptions-item>
            </el-descriptions>

            <el-table v-if="distributionRows.length" :data="distributionRows" stripe style="margin-top: 12px">
              <el-table-column prop="range" label="分段" width="160" />
              <el-table-column prop="count" label="人数" width="120" />
            </el-table>

            <el-divider />
            <div class="actions">
              <el-button :disabled="!selectedCourseId" :loading="loadingEvalList" @click="fetchEvaluationList">查看评教详情</el-button>
            </div>
            <el-table :data="evaluationList" v-loading="loadingEvalList" stripe>
              <el-table-column prop="evaluationScore" label="评分" width="100" />
              <el-table-column prop="evaluationContent" label="内容" min-width="220" />
              <el-table-column prop="evaluationTime" label="时间" min-width="180" />
              <el-table-column prop="studentName" label="学生" width="120" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const tab = ref('students')

const loadingCourses = ref(false)
const courses = ref([])
const selectedCourseId = ref(null)
const selectedCourse = ref(null)

const loadingStudents = ref(false)
const students = ref([])

const loadingGrades = ref(false)
const grades = ref([])

const loadingEval = ref(false)
const stats = ref(null)
const distributionRows = ref([])
const loadingEvalList = ref(false)
const evaluationList = ref([])

const selectedCourseLabel = computed(() => {
  if (!selectedCourse.value) return '（未选择）'
  const c = selectedCourse.value
  return `（${c.courseNo || ''} ${c.courseName || ''}）`
})

function formatScore(v) {
  if (v === null || v === undefined) return '-'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  return n.toFixed(2)
}

async function fetchCourses() {
  loadingCourses.value = true
  try {
    const res = await http.get('/course/v1/teacher/courses')
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingCourses.value = false
  }
}

function handleSelectCourse(row) {
  if (!row) return
  selectedCourse.value = row
  selectedCourseId.value = row.id
  students.value = []
  grades.value = []
  stats.value = null
  distributionRows.value = []
  evaluationList.value = []
  fetchStudents()
}

async function fetchStudents() {
  if (!selectedCourseId.value) return
  loadingStudents.value = true
  try {
    const res = await http.get('/course/v1/teacher/course/students', { params: { courseId: selectedCourseId.value } })
    students.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingStudents.value = false
  }
}

async function fetchGrades() {
  if (!selectedCourseId.value) return
  loadingGrades.value = true
  try {
    const res = await http.get('/evaluation/v1/grade/list', { params: { courseId: selectedCourseId.value } })
    grades.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingGrades.value = false
  }
}

async function downloadExport() {
  if (!selectedCourseId.value) return
  try {
    const resp = await http.get('/evaluation/v1/grade/export', {
      params: { courseId: selectedCourseId.value },
      responseType: 'blob',
    })
    const blob = resp instanceof Blob ? resp : new Blob([resp], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `grades-course-${selectedCourseId.value}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

async function handleFileChange(uploadFile) {
  if (!selectedCourseId.value) return
  const file = uploadFile?.raw
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)
  try {
    await http.post(`/evaluation/v1/grade/import?courseId=${selectedCourseId.value}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    ElMessage.success('导入完成')
    await fetchGrades()
  } catch (e) {
    ElMessage.error(e?.message || '导入失败')
  }
}

async function fetchEvaluation() {
  if (!selectedCourseId.value) return
  loadingEval.value = true
  try {
    const res = await http.get('/evaluation/v1/evaluation/stats', { params: { courseId: selectedCourseId.value } })
    stats.value = res.data || null
    const dist = stats.value?.distribution || {}
    distributionRows.value = Object.keys(dist).map((k) => ({ range: k, count: dist[k] }))
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingEval.value = false
  }
}

async function fetchEvaluationList() {
  if (!selectedCourseId.value) return
  loadingEvalList.value = true
  try {
    const res = await http.get('/evaluation/v1/evaluation/list', { params: { courseId: selectedCourseId.value } })
    evaluationList.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingEvalList.value = false
  }
}

onMounted(fetchCourses)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
.header-right {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
.hint {
  margin-top: 8px;
  color: #6b7280;
  font-size: 12px;
}
</style>

