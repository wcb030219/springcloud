<template>
  <el-card>
    <template #header>匿名教学评估（按题目作答）</template>

    <el-form :model="form" label-width="90px" class="top-form">
      <el-form-item label="课程ID">
        <el-input v-model.number="form.courseId" placeholder="例如 1" style="width: 220px" />
      </el-form-item>
      <el-form-item label="模板">
        <el-select v-model="form.templateId" placeholder="请选择模板" filterable style="width: 320px" @change="fetchQuestions">
          <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="匿名">
        <el-switch v-model="form.isAnonymous" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loadingTemplates" @click="fetchTemplates">刷新模板</el-button>
        <el-button type="primary" :loading="loadingQuestions" :disabled="!form.templateId" @click="fetchQuestions">刷新题目</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-empty v-if="!loadingQuestions && questions.length === 0" description="暂无题目，请先选择模板" />

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

const loadingTemplates = ref(false)
const loadingQuestions = ref(false)
const submitting = ref(false)

const templates = ref([])
const questions = ref([])
const answerMap = reactive({})

const form = reactive({
  courseId: null,
  templateId: null,
  isAnonymous: 1,
})

async function fetchTemplates() {
  loadingTemplates.value = true
  try {
    const res = await http.get('/evaluation/v1/evaluation/templates')
    templates.value = Array.isArray(res.data) ? res.data : []
    if (!form.templateId && templates.value.length > 0) {
      form.templateId = templates.value[0].id
      await fetchQuestions()
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingTemplates.value = false
  }
}

async function fetchQuestions() {
  if (!form.templateId) return
  loadingQuestions.value = true
  try {
    const res = await http.get('/evaluation/v1/evaluation/questions', { params: { templateId: form.templateId } })
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
    ElMessage.error('课程ID不能为空')
    return
  }
  if (!form.templateId) {
    ElMessage.error('请选择模板')
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
      templateId: form.templateId,
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

onMounted(fetchTemplates)
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
