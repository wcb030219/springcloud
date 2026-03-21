<template>
  <el-card>
    <template #header>管理员-用户管理（教师/学生）</template>

    <el-tabs v-model="roleTab" @tab-change="handleRoleChange">
      <el-tab-pane label="教师" name="2" />
      <el-tab-pane label="学生" name="3" />
    </el-tabs>

    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="用户名/姓名/学号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="fetchUsers">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" width="160" />
      <el-table-column prop="realName" label="姓名" min-width="140" />
      <el-table-column prop="studentNo" label="学号/工号" width="160" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="info">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" @click="resetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="必填" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.realName" placeholder="必填" />
      </el-form-item>
      <el-form-item label="学号/工号">
        <el-input v-model="form.studentNo" placeholder="可选" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="form.phone" placeholder="可选" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="可选" />
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
    <div class="hint">新增用户默认密码：030219</div>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/http'

const roleTab = ref('2')
const loading = ref(false)
const saving = ref(false)
const users = ref([])

const query = reactive({
  keyword: '',
  status: null,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)

const form = reactive({
  id: null,
  username: '',
  realName: '',
  studentNo: '',
  phone: '',
  email: '',
  status: 1,
})

function resetForm() {
  form.id = null
  form.username = ''
  form.realName = ''
  form.studentNo = ''
  form.phone = ''
  form.email = ''
  form.status = 1
}

function buildParams() {
  return {
    roleType: Number(roleTab.value),
    status: query.status,
    keyword: query.keyword,
  }
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await http.get('/user/v1/admin/user/list', { params: buildParams() })
    users.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = null
  fetchUsers()
}

function handleRoleChange() {
  resetQuery()
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = roleTab.value === '2' ? '新增教师' : '新增学生'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.username || !form.realName) {
    ElMessage.error('用户名和姓名不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await http.post('/user/v1/admin/user/update', { ...form, roleType: Number(roleTab.value) })
      ElMessage.success('修改成功')
    } else {
      await http.post('/user/v1/admin/user/save', { ...form, roleType: Number(roleTab.value) })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchUsers()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  try {
    const nextStatus = row.status === 1 ? 0 : 1
    await http.post('/user/v1/admin/user/status', null, { params: { id: row.id, status: nextStatus } })
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await fetchUsers()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function resetPassword(row) {
  try {
    await ElMessageBox.confirm('确认重置密码为 030219 ？', '提示', { type: 'warning' })
    await http.post('/user/v1/admin/user/resetPassword', null, { params: { id: row.id } })
    ElMessage.success('已重置密码')
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.query-form {
  margin-bottom: 12px;
}
.hint {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
}
</style>

