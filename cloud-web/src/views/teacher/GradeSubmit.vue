<template>
  <el-card>
    <template #header>教师-录入/修改成绩</template>
    <el-form :model="form" label-width="90px" class="grade-form">
      <el-form-item label="学生ID">
        <el-input v-model="form.studentId" placeholder="例如 4" />
      </el-form-item>
      <el-form-item label="课程ID">
        <el-input v-model="form.courseId" placeholder="例如 1" />
      </el-form-item>
      <el-form-item label="成绩">
        <el-input v-model.number="form.gradeScore" placeholder="例如 92.5" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remarks" placeholder="可选" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../../api/http'

const saving = ref(false)
const form = reactive({
  studentId: '',
  courseId: '',
  gradeScore: null,
  remarks: '',
})

async function submit() {
  if (!form.studentId || !form.courseId || form.gradeScore === null || form.gradeScore === undefined) {
    ElMessage.error('学生ID、课程ID和成绩不能为空')
    return
  }
  saving.value = true
  try {
    await http.post('/evaluation/v1/grade/submit', {
      studentId: form.studentId,
      courseId: form.courseId,
      gradeScore: form.gradeScore,
      remarks: form.remarks,
    })
    ElMessage.success('提交成功')
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.grade-form {
  max-width: 520px;
}
</style>
