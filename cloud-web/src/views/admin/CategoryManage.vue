<template>
  <el-card>
    <template #header>管理员-课程分类管理</template>

    <div class="actions">
      <el-button type="primary" :loading="loading" @click="fetchCategories">刷新</el-button>
      <el-button type="success" @click="openCreate">新增分类</el-button>
    </div>

    <el-table :data="categories" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="categoryName" label="分类名称" min-width="220" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" min-width="180" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="分类名称">
        <el-input v-model="form.categoryName" placeholder="必填" />
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
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const saving = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const isEdit = ref(false)

const form = reactive({
  id: null,
  categoryName: '',
  status: 1,
})

function resetForm() {
  form.id = null
  form.categoryName = ''
  form.status = 1
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/admin/category/list')
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增分类'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.categoryName) {
    ElMessage.error('分类名称不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await http.post('/course/v1/admin/category/update', { ...form })
      ElMessage.success('修改成功')
    } else {
      await http.post('/course/v1/admin/category/save', { ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchCategories()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggle(row) {
  try {
    const next = row.status === 1 ? 0 : 1
    await http.post('/course/v1/admin/category/status', null, { params: { id: row.id, status: next } })
    ElMessage.success(next === 1 ? '已启用' : '已禁用')
    await fetchCategories()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchCategories)
</script>

<style scoped>
.actions {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
</style>

