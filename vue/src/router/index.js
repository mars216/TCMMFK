import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/Manager.vue'
// Fix the issue of repeated menu clicks causing errors in vue-router versions above 3.0 for navigation bar or bottom tabBar
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/manager/home'   // ⭐Key: redirect to home by default
  },
  {
    path: '/manager',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    redirect: '/manager/home',  // ⭐Redirect to home by default when entering /manager
    children: [
      { path: 'home', name: 'Home', meta: { name: 'System Home' }, component: () => import('../views/Manager/Home') },
      { path: 'user', name: 'User', meta: { name: 'User Management' }, component: () => import('../views/Manager/User.vue') },
      { path: 'person', name: 'Person', meta: { name: 'Personal Info' }, component: () => import('../views/Manager/Person.vue') },
      { path: 'herb', name: 'Herb', component: () => import('@/views/Manager/Herb.vue') },
      {
        path: '/herb/detail/:id',
        name: 'HerbDetail',
        component: () => import('@/views/Manager/Detail/HerbDetail.vue')
      },
      { path: 'password', name: 'Password', meta: { name: 'Change Password' }, component: () => import('../views/Manager/Password.vue') },
      { path: 'medicineCategory', name: 'MedicineCategory', component: () => import('@/views/Manager/MedicineCategory.vue'), meta: { name: 'Category Info' } },
      {
        path: '/medicineCategory/detail/:id',
        name: 'MedicineCategoryDetail',
        component: () => import('@/views/Manager/Detail/MedicineCategoryDetail.vue'),
        meta: { name: 'Category Details' }
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
        name: 'Auth', meta: { name: 'No Permission' },
        component: () => import('../views/Auth.vue')
      },
      {
        path: '/ingredient/detail/:id',
        name: 'IngredientDetail',
        component: () => import('@/views/Manager/Detail/IngredientDetail.vue')  // Ensure the path is correct
      },
      {
        path: '/protein/detail/:id',
        name: 'ProteinDetail',
        component: () => import('@/views/Manager/Detail/ProteinDetail.vue')  // Ensure the path is correct
      },
      {
        path: '/disease/detail/:id',
        name: 'DiseaseDetail',
        component: () => import('@/views/Manager/Detail/DiseaseDetail.vue')  // Ensure the path is correct
      },
      {
        path: '/pathway/detail/:id',
        name: 'PathwayDetail',
        component: () => import('@/views/Manager/Detail/PathwayDetail.vue')  // Ensure the path is correct
      }
    ]
  },
  {
    path: '/element',
    name: 'element', meta: { name: 'Components' },
    component: () => import('../views/Element.vue')
  },
  {
    path: '/register',
    name: 'register', meta: { name: 'Register' },
    component: () => import('../views/Register.vue')
  },
  {
    path: '*',
    name: '404', meta: { name: 'Page Not Found' },
    component: () => import('../views/Manager/404.vue')
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})
// router.beforeEach((to, from, next) => {
//   // to is the route being navigated to
//   // from is the current route
//   // next is a function to resolve the hook
//   let adminPaths = ['/user']
//   let user = JSON.parse(localStorage.getItem('honey-user') || '{}')
//
//   if (user.role !== 'Admin' && adminPaths.includes(to.path)) {
//     // If the current user is not an admin but the target route is only accessible to admins, redirect to the 403 page
//     next('/403')
//   } else {
//     next()
//   }
// })
// router.beforeEach((to, from, next) => {
//   // 1. Define whitelist for interception rules
//   const whiteList = ['/login', '/register', '/', '/403', '/manager/llm',]; // Public pages (accessible without login)
//   const adminPaths = ['/manager/user']; // Pages only accessible to admins (note: your actual /user route is /manager/user)
//   const user = JSON.parse(localStorage.getItem('honey-user') || '{}'); // Get user info from local storage
//   const isLoggedIn = !!user.username; // Determine if logged in (presence of username, could also check token)

//   // 2. First step: block unauthenticated users (core fix to bypass login)
//   if (!isLoggedIn && !whiteList.includes(to.path)) {
//     // Not logged in + accessing non-public page → redirect to login page and record target path (to return after login)
//     next({ path: '/login', query: { redirect: to.fullPath } });
//     return; // Terminate further logic to avoid duplicate navigation
//   }

//   // 3. Second step: permission control for logged-in users (original logic with improved path matching)
//   if (isLoggedIn && user.role !== 'Admin' && adminPaths.includes(to.path)) {
//     // Logged in but not admin + accessing admin-only page → redirect to 403 no-permission page
//     next('/403');
//     return;
//   }

//   // 4. All conditions satisfied (logged in / public page + has permission) → allow navigation
//   next();
// });
// router/index.js or route configuration file
router.beforeEach((to, from, next) => {
  // Fully public, no interception
  next();
});

export default router