import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/Manager.vue'
// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}




Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/manager/home'   // ⭐关键：默认跳首页
  },
  {
    path: '/manager',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/manager/home',  // ⭐进入 manager 默认也是首页
    children: [
      { path: 'home', name: 'Home', meta: { name: '系统首页' }, component: () => import('../views/Manager/Home') },
      { path: 'user', name: 'User', meta: { name: '用户管理' }, component: () => import('../views/Manager/User.vue') },
      { path: 'person', name: 'Person', meta: { name: '个人信息' }, component: () => import('../views/Manager/Person.vue') },
      { path: 'herb', name: 'Herb', component: () => import('@/views/Manager/Herb.vue') },
      {
        path: '/herb/detail/:id',
        name: 'HerbDetail',
        component: () => import('@/views/Manager/Detail/HerbDetail.vue')
      },
      { path: 'password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../views/Manager/Password.vue') },
      { path: 'medicineCategory', name: 'MedicineCategory', component: () => import('@/views/Manager/MedicineCategory.vue'), meta: { name: '类目信息' } },
      {
        path: '/medicineCategory/detail/:id',
        name: 'MedicineCategoryDetail',
        component: () => import('@/views/Manager/Detail/MedicineCategoryDetail.vue'),
        meta: { name: '类目详情' }
      },
      { path: '/graph/stats', name: 'GraphStats', component: () => import('@/views/Manager/Stats/GraphStats.vue') },
      { path: '/graph/search', name: 'GraphSearch', component: () => import('@/views/Manager/Graph/Search_graph.vue') },
      { path: 'ingredients', name: 'Ingredients', component: () => import('@/views/Manager/Ingredients.vue') },
      { path: 'protein', name: 'Protein', component: () => import('@/views/Manager/Protein.vue') },
      { path: 'disease', name: 'Disease', component: () => import('@/views/Manager/Disease.vue') },
      { path: 'pathway', name: 'Pathway', component: () => import('@/views/Manager/Pathway.vue') },
      { path: 'llm', name: 'LLM', component: () => import('@/views/Manager/flask/test1.vue') },
      { path: 'predictTargets', name: 'PredictTargets', component: () => import('@/views/Manager/flask/PredictTargets.vue') },
      {
        path: '/searchAll',
        name: 'SearchAll',
        component: () => import('@/views/Manager/SearchAll/SearchAll.vue')
      },
      {
        path: '/403',
        name: 'Auth', meta: { name: '无权限' },
        component: () => import('../views/Auth.vue')
      },
      {
        path: '/ingredient/detail/:id',
        name: 'IngredientDetail',
        component: () => import('@/views/Manager/Detail/IngredientDetail.vue')  // 确保路径正确
      },
      {
        path: '/protein/detail/:id',
        name: 'ProteinDetail',
        component: () => import('@/views/Manager/Detail/ProteinDetail.vue')  // 确保路径正确
      },
      {
        path: '/disease/detail/:id',
        name: 'DiseaseDetail',
        component: () => import('@/views/Manager/Detail/DiseaseDetail.vue')  // 确保路径正确
      },
      {
        path: '/pathway/detail/:id',
        name: 'PathwayDetail',
        component: () => import('@/views/Manager/Detail/PathwayDetail.vue')  // 确保路径正确
      }
    ]
  },
  {
    path: '/element',
    name: 'element', meta: { name: '组件' },
    component: () => import('../views/Element.vue')
  },
  {
    path: '/register',
    name: 'register', meta: { name: '注册' },
    component: () => import('../views/Register.vue')
  },
  {
    path: '*',
    name: '404', meta: { name: '无法访问' },
    component: () => import('../views/Manager/404.vue')
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
// router.beforeEach((to, from, next) => {
//   // to 是到达的路由信息
//   // from 是开源的路由信息
//   // next 是帮助我们跳转路由的函数
//   let adminPaths = ['/user']
//   let user = JSON.parse(localStorage.getItem('honey-user') || '{}')
//
//   if (user.role !== '管理员' && adminPaths.includes(to.path)) {
//     // 如果当前登录的用户不是管理员，然后当前的到达的路径是管理员才有权限访问的路径，那这个时候我就让用户去到一个没有权限的页面，不让他访问实际的页面
//     next('/403')
//   } else {
//     next()
//   }
// })
router.beforeEach((to, from, next) => {
  // 1. 定义需要拦截的规则
  const whiteList = ['/login', '/register', '/', '/403', '/manager/llm']; // 公开页面（无需登录就能访问）
  const adminPaths = ['/manager/user']; // 仅管理员能访问的页面（注意：路径要完整，你的 /user 实际是 /manager/user）
  const user = JSON.parse(localStorage.getItem('honey-user') || '{}'); // 获取本地存储的用户信息
  const isLoggedIn = !!user.username; // 判断是否已登录（有用户名即视为已登录，也可判断 token）

  // 2. 第一步：拦截未登录用户（核心！解决绕过登录的问题）
  if (!isLoggedIn && !whiteList.includes(to.path)) {
    // 未登录 + 访问的不是公开页面 → 强制跳登录页，并记录“目标路径”（登录后可跳回）
    next({ path: '/login', query: { redirect: to.fullPath } });
    return; // 终止后续逻辑，避免重复跳转
  }

  // 3. 第二步：已登录用户的权限控制（你原有的逻辑，优化路径匹配）
  if (isLoggedIn && user.role !== '管理员' && adminPaths.includes(to.path)) {
    // 已登录但非管理员 + 访问管理员页面 → 跳 403 无权限页
    next('/403');
    return;
  }

  // 4. 所有条件都满足（已登录/是公开页 + 有权限）→ 允许跳转
  next();
});

export default router
