<template>
  <el-card>
    <template #header>管理员-课程管理</template>

    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="课程名">
        <el-input v-model="query.courseName" placeholder="模糊查询" clearable />
      </el-form-item>
      <el-form-item label="课程编号">
        <el-input v-model="query.courseNo" placeholder="精确查询" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.courseCategory" placeholder="全部" clearable filterable style="width: 180px">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.categoryName" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="fetchCourses">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增课程</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="courses" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="courseNo" label="课程编号" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column prop="teacherName" label="授课教师" width="140" />
      <el-table-column prop="courseCategory" label="分类" width="140" />
      <el-table-column prop="courseType" label="类型" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.courseType === 1">必修</el-tag>
          <el-tag v-else type="warning">选修</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="classTime" label="时间" min-width="140" />
      <el-table-column prop="classLocation" label="地点" min-width="140" />
      <el-table-column prop="capacity" label="容量" width="90" />
      <el-table-column prop="selectedCount" label="已选" width="90" />
      <el-table-column prop="courseStatus" label="状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.courseStatus === 1" type="success">上架</el-tag>
          <el-tag v-else type="info">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">修改</el-button>
          <el-button
            v-if="row.courseStatus === 1"
            size="small"
            type="warning"
            @click="setStatus(row.id, 0)"
          >下架</el-button>
          <el-button
            v-else
            size="small"
            type="success"
            @click="setStatus(row.id, 1)"
          >上架</el-button>
          <el-button size="small" type="danger" @click="removeCourse(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="课程编号">
        <el-input v-model="form.courseNo" placeholder="例如 CS201" />
      </el-form-item>
      <el-form-item label="课程名称">
        <el-input v-model="form.courseName" placeholder="例如 数据结构" />
      </el-form-item>
      <el-form-item label="授课教师">
        <el-select v-model="form.teacherId" placeholder="请选择教师" filterable style="width: 260px">
          <el-option v-for="t in teachers" :key="t.id" :label="`${t.realName || ''} (${t.username || ''})`" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="容量">
        <el-input v-model.number="form.capacity" placeholder="例如 50" />
      </el-form-item>
      <el-form-item label="学分">
        <el-input v-model.number="form.credit" placeholder="例如 3.0" />
      </el-form-item>
      <el-form-item label="学时">
        <el-input v-model.number="form.classHours" placeholder="例如 48" />
      </el-form-item>
      <el-form-item label="类别">
        <el-select v-model="form.courseCategory" placeholder="请选择/输入分类" filterable allow-create default-first-option style="width: 260px">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.categoryName" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.courseType" style="width: 160px">
          <el-option label="必修" :value="1" />
          <el-option label="选修" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间">
        <el-input v-model="form.classTime" placeholder="可选" />
      </el-form-item>
      <el-form-item label="地点">
        <el-input v-model="form.classLocation" placeholder="可选" />
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
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/http'

const loading = ref(false)
const saving = ref(false)
const courses = ref([])
const teachers = ref([])
const categories = ref([])

const query = reactive({
  courseName: '',
  courseNo: '',
  courseCategory: '',
  status: null,
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const isEdit = ref(false)

const form = reactive({
  id: null,
  courseNo: '',
  courseName: '',
  teacherId: null,
  capacity: null,
  credit: null,
  classHours: null,
  courseCategory: '',
  courseType: 2,
  classTime: '',
  classLocation: '',
  description: '',
})

function resetForm() {
  form.id = null
  form.courseNo = ''
  form.courseName = ''
  form.teacherId = null
  form.capacity = null
  form.credit = null
  form.classHours = null
  form.courseCategory = ''
  form.courseType = 2
  form.classTime = ''
  form.classLocation = ''
  form.description = ''
}

async function fetchCourses() {
  loading.value = true
  try {
    const res = await http.get('/course/v1/admin/list', { params: { ...query } })
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchTeachers() {
  try {
    const res = await http.get('/user/v1/admin/user/list', { params: { roleType: 2, status: 1 } })
    teachers.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    teachers.value = []
  }
}

async function fetchCategories() {
  try {
    const res = await http.get('/course/v1/admin/category/list', { params: { status: 1 } })
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    categories.value = []
  }
}

function resetQuery() {
  query.courseName = ''
  query.courseNo = ''
  query.courseCategory = ''
  query.status = null
  fetchCourses()
}

function openCreate() {
  isEdit.value = false
  dialogTitle.value = '新增课程'
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '修改课程'
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.courseNo || !form.courseName) {
    ElMessage.error('课程编号和课程名称不能为空')
    return
  }
  if (!form.teacherId) {
    ElMessage.error('教师ID不能为空')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await http.post('/course/v1/admin/update', { ...form })
      ElMessage.success('修改成功')
    } else {
      await http.post('/course/v1/admin/save', { ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchCourses()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function setStatus(id, status) {
  try {
    await ElMessageBox.confirm(status === 1 ? '确认上架该课程？' : '确认下架该课程？', '提示', { type: 'warning' })
    await http.post('/course/v1/admin/status', null, { params: { id, status } })
    ElMessage.success(status === 1 ? '已上架' : '已下架')
    await fetchCourses()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  }
}

async function removeCourse(id) {
  try {
    await ElMessageBox.confirm('确认删除该课程？', '提示', { type: 'warning' })
    await http.post('/course/v1/admin/remove', null, { params: { id } })
    ElMessage.success('已删除')
    await fetchCourses()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchCourses)
onMounted(fetchTeachers)
onMounted(fetchCategories)
</script>

<style scoped>
.query-form {
  margin-bottom: 12px;
}
</style>
