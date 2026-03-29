<template>
  <el-card>
    <template #header>管理员-评估模板管理</template>

    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchList">刷新</el-button>
      <el-button type="success" @click="openCreate">新增模板</el-button>
    </div>

    <el-form :inline="true" :model="bindForm" class="bind-form">
      <el-form-item label="课程">
        <el-select v-model="bindForm.courseId" placeholder="请选择课程" filterable style="width: 320px">
          <el-option v-for="c in courseOptions" :key="c.id" :label="`${c.courseNo || ''} ${c.courseName || ''}`" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="模板">
        <el-select v-model="bindForm.templateId" placeholder="请选择模板" filterable style="width: 260px">
          <el-option v-for="t in rows" :key="t.id" :label="t.templateName" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="binding" :disabled="!bindForm.courseId || !bindForm.templateId" @click="bindTemplate">绑定</el-button>
        <el-button :loading="loadingCourses" @click="fetchCourses">刷新课程</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="rows" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="templateName" label="模板名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" :underline="false" @click.stop="openBindings(row)">{{ row.templateName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="templateType" label="类型" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.templateType === 1">通用</el-tag>
          <el-tag v-else type="warning">专业</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" plain @click="removeTemplate(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="模板名称">
        <el-input v-model="form.templateName" placeholder="必填" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.templateType" style="width: 160px">
          <el-option label="通用" :value="1" />
          <el-option label="专业" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 160px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>

  <el-dialog v-model="bindingsVisible" :title="bindingsTitle" width="760px">
    <div class="actions">
      <el-button type="primary" :loading="bindingsLoading" :disabled="!boundTemplate" @click="fetchBindings">刷新</el-button>
    </div>
    <el-table :data="bindingCourses" v-loading="bindingsLoading" stripe>
      <el-table-column prop="id" label="课程ID" width="110" />
      <el-table-column prop="courseNo" label="编号" width="140" />
      <el-table-column prop="courseName" label="课程名称" min-width="220" />
      <el-table-column prop="classTime" label="时间" min-width="160" />
      <el-table-column prop="classLocation" label="地点" min-width="160" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain :loading="unbindLoadingId === row.id" @click="unbindCourse(row)">解绑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const saving = ref(false)
const loadingCourses = ref(false)
const binding = ref(false)
const rows = ref([])
const courseOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增模板')
const isEdit = ref(false)

const bindingsVisible = ref(false)
const bindingsLoading = ref(false)
const bindingCourses = ref([])
const boundTemplate = ref(null)
const unbindLoadingId = ref(null)

const bindingsTitle = computed(() => {
  if (!boundTemplate.value) return '已绑定课程'
  return `已绑定课程 - ${boundTemplate.value.templateName || ''}`
})

const bindForm = reactive({
  courseId: null,
  templateId: null,
})

const form = reactive({
  id: null,
  templateName: '',
  templateType: 1,
  status: 1,
  description: '',
})

function resetForm() {
  form.id = null
  form.templateName = ''
  form.templateType = 1
  form.status = 1
  form.description = ''
}

async function fetchList() {
  loading.value = true
  try {
    const res = await http.get('/evaluation/v1/admin/evaluation/template/list')
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchCourses() {
  loadingCourses.value = true
  try {
    const res = await http.get('/course/v1/admin/list', { params: { status: 1 } })
    courseOptions.value = Array.isArray(res.data) ? res.data : []
  } catch {
    courseOptions.value = []
  } finally {
    loadingCourses.value = false
  }
}

async function bindTemplate() {
  binding.value = true
  try {
    await http.post('/evaluation/v1/admin/evaluation/courseTemplate/bind', null, {
      params: { courseId: bindForm.courseId, templateId: bindForm.templateId },
    })
    ElMessage.success('绑定成功')
  } catch (e) {
    ElMessage.error(e?.message || '绑定失败')
  } finally {
    binding.value = false
  }
}

async function openBindings(row) {
  boundTemplate.value = row || null
  bindingCourses.value = []
  bindingsVisible.value = true
  await fetchBindings()
}

async function fetchBindings() {
  if (!boundTemplate.value?.id) return
  bindingsLoading.value = true
  try {
    const res = await http.get('/evaluation/v1/admin/evaluation/template/bindings', { params: { templateId: boundTemplate.value.id } })
    bindingCourses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    bindingsLoading.value = false
  }
}

async function unbindCourse(course) {
  if (!course?.id || !boundTemplate.value?.id) return
  try {
    await ElMessageBox.confirm('确认解绑该课程的模板？', '提示', { type: 'warning' })
    unbindLoadingId.value = course.id
    await http.post('/evaluation/v1/admin/evaluation/courseTemplate/unbind', null, {
      params: { courseId: course.id, templateId: boundTemplate.value.id },
    })
    ElMessage.success('解绑成功')
    await fetchBindings()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  } finally {
    unbindLoadingId.value = null
  }
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增模板'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑模板'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.templateName) {
    ElMessage.error('模板名称不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await http.post('/evaluation/v1/admin/evaluation/template/update', { ...form })
      ElMessage.success('修改成功')
    } else {
      await http.post('/evaluation/v1/admin/evaluation/template/save', { ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggle(row) {
  try {
    const next = row.status === 1 ? 0 : 1
    await http.post('/evaluation/v1/admin/evaluation/template/status', null, { params: { id: row.id, status: next } })
    ElMessage.success(next === 1 ? '已启用' : '已禁用')
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function removeTemplate(row) {
  try {
    await ElMessageBox.confirm('确认删除该模板（将同时删除该模板下的题目）？', '提示', { type: 'warning' })
    await http.post('/evaluation/v1/admin/evaluation/template/delete', null, { params: { id: row.id } })
    ElMessage.success('已删除')
    await fetchList()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchList)
onMounted(fetchCourses)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.bind-form {
  margin-bottom: 12px;
}
</style>
