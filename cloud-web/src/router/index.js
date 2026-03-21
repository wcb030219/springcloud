import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const Login = () => import('../views/Login.vue')
const Home = () => import('../views/Home.vue')
const StudentCourses = () => import('../views/student/CourseList.vue')
const StudentSelected = () => import('../views/student/SelectedCourses.vue')
const StudentDrops = () => import('../views/student/DropRecords.vue')
const StudentTimetable = () => import('../views/student/Timetable.vue')
const StudentGrades = () => import('../views/student/GradeMy.vue')
const StudentEvaluation = () => import('../views/student/EvaluationSubmit.vue')
const AdminCourses = () => import('../views/admin/CourseManage.vue')
const AdminUsers = () => import('../views/admin/UserManage.vue')
const AdminCategories = () => import('../views/admin/CategoryManage.vue')
const AdminStats = () => import('../views/admin/StatsDashboard.vue')
const AdminEvalTemplates = () => import('../views/admin/EvaluationTemplateManage.vue')
const AdminEvalQuestions = () => import('../views/admin/EvaluationQuestionManage.vue')
const AdminEvalReport = () => import('../views/admin/EvaluationReport.vue')
const TeacherGrade = () => import('../views/teacher/GradeSubmit.vue')
const TeacherCourses = () => import('../views/teacher/MyCourses.vue')

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', name: 'Home', component: Home },
  { path: '/student/courses', name: 'StudentCourses', component: StudentCourses, meta: { roles: ['3'] } },
  { path: '/student/selected', name: 'StudentSelected', component: StudentSelected, meta: { roles: ['3'] } },
  { path: '/student/drops', name: 'StudentDrops', component: StudentDrops, meta: { roles: ['3'] } },
  { path: '/student/timetable', name: 'StudentTimetable', component: StudentTimetable, meta: { roles: ['3'] } },
  { path: '/student/grades', name: 'StudentGrades', component: StudentGrades, meta: { roles: ['3'] } },
  { path: '/student/evaluation', name: 'StudentEvaluation', component: StudentEvaluation, meta: { roles: ['3'] } },
  { path: '/admin/courses', name: 'AdminCourses', component: AdminCourses, meta: { roles: ['1'] } },
  { path: '/admin/users', name: 'AdminUsers', component: AdminUsers, meta: { roles: ['1'] } },
  { path: '/admin/categories', name: 'AdminCategories', component: AdminCategories, meta: { roles: ['1'] } },
  { path: '/admin/stats', name: 'AdminStats', component: AdminStats, meta: { roles: ['1'] } },
  { path: '/admin/evaluation/templates', name: 'AdminEvalTemplates', component: AdminEvalTemplates, meta: { roles: ['1'] } },
  { path: '/admin/evaluation/questions', name: 'AdminEvalQuestions', component: AdminEvalQuestions, meta: { roles: ['1'] } },
  { path: '/admin/evaluation/report', name: 'AdminEvalReport', component: AdminEvalReport, meta: { roles: ['1'] } },
  { path: '/teacher/grade', name: 'TeacherGrade', component: TeacherGrade, meta: { roles: ['2'] } },
  { path: '/teacher/courses', name: 'TeacherCourses', component: TeacherCourses, meta: { roles: ['2'] } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!auth.userId && !auth.role) {
    auth.loadFromStorage()
  }

  if (to.path === '/login') return true

  if (!auth.isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const allowedRoles = to.meta?.roles
  if (Array.isArray(allowedRoles) && allowedRoles.length > 0) {
    if (!allowedRoles.includes(String(auth.role))) return { path: '/' }
  }
  return true
})
