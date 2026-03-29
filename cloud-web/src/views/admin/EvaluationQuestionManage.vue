<template>
  <el-card>
    <template #header>管理员-评估题库管理</template>

    <div class="actions">
      <el-select v-model="templateId" placeholder="请选择模板" filterable style="width: 320px" @change="fetchQuestions">
        <el-option v-for="t in templates" :key="t.id" :label="t.templateName" :value="t.id" />
      </el-select>
      <el-button type="primary" :loading="loadingTemplates" @click="fetchTemplates">刷新模板</el-button>
      <el-button type="success" :disabled="!templateId" @click="openCreate">新增题目</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="questionOrder" label="顺序" width="90" />
      <el-table-column prop="questionType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.questionType === 1">单选</el-tag>
          <el-tag v-else-if="row.questionType === 2" type="warning">多选</el-tag>
          <el-tag v-else type="info">文本</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionContent" label="题目内容" min-width="320" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" plain @click="removeQuestion(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="题目内容">
        <el-input v-model="form.questionContent" type="textarea" :rows="3" placeholder="必填" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.questionType" style="width: 160px">
          <el-option label="单选" :value="1" />
          <el-option label="多选" :value="2" />
          <el-option label="文本" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="顺序">
        <el-input v-model.number="form.questionOrder" style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 160px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/http'

const loadingTemplates = ref(false)
const templates = ref([])
const templateId = ref(null)

const loading = ref(false)
const saving = ref(false)
const rows = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')
const isEdit = ref(false)

const form = reactive({
  id: null,
  templateId: null,
  questionContent: '',
  questionType: 1,
  questionOrder: 1,
  status: 1,
})

function resetForm() {
  form.id = null
  form.templateId = templateId.value
  form.questionContent = ''
  form.questionType = 1
  form.questionOrder = 1
  form.status = 1
}

async function fetchTemplates() {
  loadingTemplates.value = true
  try {
    const res = await http.get('/evaluation/v1/admin/evaluation/template/list', { params: { status: 1 } })
    const list = Array.isArray(res.data) ? res.data : []
    const map = new Map()
    list.forEach((t) => {
      if (t && t.id != null) map.set(t.id, t)
    })
    templates.value = Array.from(map.values())
    if (!templateId.value && templates.value.length > 0) {
      templateId.value = templates.value[0].id
      await fetchQuestions()
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loadingTemplates.value = false
  }
}

async function fetchQuestions() {
  if (!templateId.value) return
  loading.value = true
  try {
    const res = await http.get('/evaluation/v1/admin/evaluation/question/list', { params: { templateId: templateId.value } })
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增题目'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑题目'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.questionContent) {
    ElMessage.error('题目内容不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await http.post('/evaluation/v1/admin/evaluation/question/update', { ...form, templateId: templateId.value })
      ElMessage.success('修改成功')
    } else {
      await http.post('/evaluation/v1/admin/evaluation/question/save', { ...form, templateId: templateId.value })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchQuestions()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggle(row) {
  try {
    const next = row.status === 1 ? 0 : 1
    await http.post('/evaluation/v1/admin/evaluation/question/status', null, { params: { id: row.id, status: next } })
    ElMessage.success(next === 1 ? '已启用' : '已禁用')
    await fetchQuestions()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function removeQuestion(row) {
  try {
    await ElMessageBox.confirm('确认删除该题目？', '提示', { type: 'warning' })
    await http.post('/evaluation/v1/admin/evaluation/question/delete', null, { params: { id: row.id } })
    ElMessage.success('已删除')
    await fetchQuestions()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchTemplates)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
