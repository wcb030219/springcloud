<template>
  <el-card>
    <template #header>匿名教学评估（按题目作答）</template>

    <el-form :model="form" label-width="90px" class="top-form">
      <el-form-item label="课程">
        <el-select v-model="form.courseId" placeholder="请选择课程" filterable style="width: 420px" @change="onCourseChange">
          <el-option
            v-for="c in myCourses"
            :key="c.id"
            :label="`${c.courseNo || ''} ${c.courseName || ''}（${c.classTime || '未填写时间'} / ${c.classLocation || '未填写地点'}）`"
            :value="c.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="模板">
        <el-input :model-value="templateName" disabled style="width: 320px" />
      </el-form-item>
      <el-form-item label="匿名">
        <el-switch v-model="form.isAnonymous" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loadingCourses" @click="fetchMyCourses">刷新课程</el-button>
        <el-button type="primary" :loading="loadingQuestions" :disabled="!form.courseId" @click="fetchQuestions">刷新题目</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-empty v-if="!loadingQuestions && questions.length === 0" description="暂无题目，请先选择课程" />

    <el-form v-else label-width="0px">
      <div v-for="q in questions" :key="q.id" class="q-item">
        <div class="q-title">{{ q.questionOrder }}. {{ q.questionContent }}</div>
        <div class="q-body">
          <el-rate
            v-if="q.questionType !== 3"
            v-model="answerMap[q.id].answerScore"
            :max="5"
            show-score
            score-template="{value}"
          />
          <el-input
            v-else
            v-model="answerMap[q.id].answerContent"
            type="textarea"
            :rows="3"
            placeholder="请输入你的建议（可选）"
          />
        </div>
      </div>

      <el-button type="primary" :loading="submitting" @click="submit">提交评教</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loadingCourses = ref(false)
const loadingQuestions = ref(false)
const submitting = ref(false)

const myCourses = ref([])
const questions = ref([])
const answerMap = reactive({})
const templateName = ref('')

const form = reactive({
  courseId: null,
  isAnonymous: 1,
})

async function fetchMyCourses() {
  loadingCourses.value = true
  try {
    const [selectedRes, requiredRes] = await Promise.all([
      http.get('/course/v1/my/selected'),
      http.get('/course/v1/my/required'),
    ])
    const selected = Array.isArray(selectedRes.data) ? selectedRes.data.map((x) => x.course).filter(Boolean) : []
    const required = Array.isArray(requiredRes.data) ? requiredRes.data : []
    const map = new Map()
    ;[...required, ...selected].forEach((c) => {
      if (c && c.id != null) map.set(c.id, c)
    })
    myCourses.value = Array.from(map.values())
    if (!form.courseId && myCourses.value.length > 0) {
      form.courseId = myCourses.value[0].id
      await onCourseChange()
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingCourses.value = false
  }
}

async function onCourseChange() {
  questions.value = []
  templateName.value = ''
  if (!form.courseId) return
  await fetchQuestions()
}

async function fetchQuestions() {
  if (!form.courseId) return
  loadingQuestions.value = true
  try {
    const tRes = await http.get('/evaluation/v1/evaluation/courseTemplate', { params: { courseId: form.courseId } })
    const t = tRes?.data || null
    templateName.value = t?.templateName || '未配置模板'
    const res = await http.get('/evaluation/v1/evaluation/questions', { params: { courseId: form.courseId } })
    questions.value = Array.isArray(res.data) ? res.data : []
    questions.value.forEach((q) => {
      if (!answerMap[q.id]) {
        answerMap[q.id] = { questionId: q.id, answerScore: 0, answerContent: '' }
      }
    })
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingQuestions.value = false
  }
}

function buildAnswers() {
  return questions.value.map((q) => {
    const a = answerMap[q.id] || { questionId: q.id, answerScore: 0, answerContent: '' }
    if (q.questionType === 3) {
      return { questionId: q.id, answerContent: a.answerContent || '', answerScore: null }
    }
    const score = a.answerScore == null ? 0 : Number(a.answerScore)
    return { questionId: q.id, answerContent: null, answerScore: Number.isNaN(score) ? 0 : Math.round(score) }
  })
}

async function submit() {
  if (!form.courseId) {
    ElMessage.error('请选择课程')
    return
  }
  if (questions.value.length === 0) {
    ElMessage.error('题目为空')
    return
  }
  submitting.value = true
  try {
    await http.post('/evaluation/v1/evaluation/submitAnswers', {
      courseId: form.courseId,
      isAnonymous: form.isAnonymous,
      answers: buildAnswers(),
    })
    ElMessage.success('提交成功')
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchMyCourses)
</script>

<style scoped>
.top-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.q-item {
  padding: 10px 0;
  border-bottom: 1px dashed #e5e7eb;
}
.q-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}
.q-body {
  padding-left: 6px;
}
</style>
