<!-- <template>
  <div>
    <el-container>
    <el-aside style="background-color: #001529;min-height:100vh" :width="asideWith">
      <div style="height: 60px;color:white;display: flex;align-items: center;justify-content: center">
        <img src="@/assets/logo.png" width="40px"height="40px">
        <span  v-show="!isCollapse" class="logo-title">Mars</span>
      </div>
      <el-menu router :collapse="isCollapse" :collapse-transition="false" background-color="#001529"  style="border: none;"
               text-color="rgba(255,255,255,0.65)" :default-active="$route.path" class="el-menu-menu">
      <el-menu-item index="/manager/home" >
          <i class="el-icon-house"></i>
          <span slot="title" >System Home</span>
      </el-menu-item>
        <el-menu-item  index="/manager/Person">
          <i class="el-icon-yonghuguanli2"></i>
          <span slot="title">Personal Info</span>
        </el-menu-item>
        <el-menu-item  v-if="loginUser.role==='Admin'" index="/manager/user">
            <i class="el-icon-yonghuguanli1"></i>
            <span slot="title">User Management</span>
        </el-menu-item>
        <el-menu-item index="/graph/stats">
          <i class="el-icon-house"></i>
          <span slot="title">System Home</span>
        </el-menu-item>

        <el-submenu index="/manager">
          <template slot="title">
            <i class="el-icon-menu"></i>
            <span>Information Management</span>
          </template>
          <el-menu-item  index="/manager/medicineCategory">
            Category Info
          </el-menu-item>
          <el-menu-item index="/manager/herb">
            Herb Info
          </el-menu-item>
          <el-menu-item index="/manager/ingredients">
            Ingredient Info
          </el-menu-item>
          <el-menu-item index="/manager/protein">
            Protein Info
          </el-menu-item>
          <el-menu-item index="/manager/Disease">
            Disease Info
          </el-menu-item>
          <el-menu-item index="/manager/Pathway">
            Pathway Info
          </el-menu-item>
        </el-submenu>
        <el-menu-item index="/graph/search">
          <i class="el-icon-search"></i>
          <span slot="title">Graph Search</span>
        </el-menu-item>
        <el-menu-item index="/searchAll">
          <i class="el-icon-s-grid"></i>
          <span slot="title">Select All</span>
        </el-menu-item>
        <el-menu-item index="/manager/llm">
          <i class="el-icon-data-analysis"></i> 
          <span slot="title">Knowledge Extraction</span>
        </el-menu-item>
        <el-menu-item index="/manager/predictTargets">
          <i class="el-icon-connection"></i>
          <span slot="title">Link Prediction</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <i :class="collapseIcon" style="font-size: 26px" @click="handleCollapse"></i>
        <el-breadcrumb separator-class="el-icon-arrow-right" style="margin-left: 20px">
          <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
          <el-breadcrumb-item :to="{path: $route.path}">{{$route.meta.name}}</el-breadcrumb-item>
        </el-breadcrumb>
        <div style="flex: 1;display: flex;align-items: center;justify-content: flex-end">
          <el-dropdown placement="bottom" >
            <div style="display: flex;align-items: center;cursor: default">
              <i class="el-icon-quanping" style="font-size: 24px;margin-right: 10px" @click="handleFull"></i>
            <img :src="loginUser.avatar||'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="" style="width: 40px;height: 40px;border-radius: 50%">
            <span>{{loginUser.name}}</span>
            </div>
                          <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item @click.native="$router.push('/person')">Personal Info</el-dropdown-item>
                            <el-dropdown-item @click.native="$router.push('/password')">Change Password</el-dropdown-item>
                            <el-dropdown-item @click.native="logout">Logout</el-dropdown-item>
                          </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view  @update:user="updateUser"/>
      </el-main>
    </el-container>
    </el-container>
  </div>
</template>
<script>
import axios from "axios";
import request from "@/utils/request";
export default {
  data() {
    return {
      isCollapse: false,
      asideWith: '200px',
      collapseIcon:'el-icon-s-fold',
      user:[],
      loginUser: JSON.parse(localStorage.getItem("honey-user") || '{}'),
      url:'',
      urls:[]
    }
    },
  mounted(){
    request.get('/user/selectAll').then(res=>{
      this.user=res.data
    })
  },
  methods:{
    preview(url){
      window.open(url)
    },
    updateUser(user){
      this.loginUser=JSON.parse(JSON.stringify(user))
    },

    showUrls(){
      console.log(this.urls)
    },
    handleMultipleFileUpload(response, file, fileList) {
      this.urls = fileList.map(v => v.response?.data)
    },
    handleTableFileUpload(row, file, fileList) {
      console.log(row, file, fileList)
      row.avatar = file.response.data
      console.log(row)
      // Trigger update
      request.put('/user/update', row).then(res => {
        if (res.code === '200') {
          this.$message.success('Upload successful')
        } else {
          this.$message.error(res.msg)
        }
      })
    },

    handleFileUpload(response,file,fileList){
    },
      handleCollapse()
      {
        this.isCollapse = !this.isCollapse
        this.collapseIcon=this.isCollapse?'el-icon-s-unfold':'el-icon-s-fold'
        this.asideWith=this.isCollapse?'64px':'200px'
      },
      handleFull(){
        document.documentElement.requestFullscreen()
      },
      logout(){
        localStorage.removeItem("honey-user")
        this.$router.push("/")
      }

    }
}
</script>

<style>
.logo-title{
  margin-left: 5px;
  font-size: 20px;
  transition: all .3s;
}
.el-menu--inline  {
  background-color: #000c17!important;
}
.el-menu--inline .el-menu-item{
  background-color: #000c17!important;
  padding-left: 49px!important;
  min-width: 0px!important;
}
.el-menu--inline  .el-menu-item.is-active{
  padding-left: 45px!important;
}
.el-submenu__title:hover,.el-menu-item:hover,.el-submenu__title:hover>i ,.el-menu-item:hover>i,.el-menu-item .el-tooltip:hover>i{
  color:#fff!important;
}
.el-menu-item.is-active {
  color: white!important;
  background-color: #409EFF!important;
  border-radius: 5px!important;
  width: calc(100% - 8px);
  margin-left: 4px!important;
}

.el-menu-item.is-active i,.el-menu-item.is-active .el-tooltip{
  margin-left: -4px;
}
.el-menu-item{
  height: 40px!important;
  line-height: 40px !important;
}
.el-submenu__title{
  height: 40px!important;
  line-height: 40px !important;
}
.el-aside{
  transition: width .3s;
  box-shadow: 2px 0 6px rgba(0,21,41,35);
}
.el-header{
  box-shadow: 2px 0 6px rgba(0,21,41,35);
  display: flex;
  align-items: center;
}
</style> -->

<template>
  <div>
    <el-container>
      <!-- Top navigation bar -->
      <el-header class="header">
        <!-- Logo -->
        <div class="logo-container">
          <div class="text-logo">TCMKP</div>
          <span class="logo-title">Platform</span>
        </div>

        <!-- Top menu -->
        <el-menu
          router
          class="top-menu"
          mode="horizontal"
          :default-active="$route.path"
          background-color="#ffffff"
          text-color="#333"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/manager/home">
            <i class="el-icon-house"></i>
            <span>Home</span>
          </el-menu-item>

          <el-menu-item index="/graph/stats">
            <i class="el-icon-s-claim"></i>
            <span>Herb Overview Statistics</span>
          </el-menu-item>

          <el-submenu index="/manager">
            <template slot="title">
              <i class="el-icon-menu"></i>
              <span>Data Details</span>
            </template>
            <el-menu-item index="/manager/medicineCategory">Categories</el-menu-item>
            <el-menu-item index="/manager/herb">Herbs</el-menu-item>
            <el-menu-item index="/manager/ingredients">Ingredients</el-menu-item>
            <el-menu-item index="/manager/protein">Proteins</el-menu-item>
            <el-menu-item index="/manager/Disease">Diseases</el-menu-item>
            <el-menu-item index="/manager/Pathway">Pathways</el-menu-item>
          </el-submenu>

          <el-menu-item index="/graph/search">
            <i class="el-icon-search"></i>
            <span>Graph Search</span>
          </el-menu-item>

          <el-menu-item index="/searchAll">
            <i class="el-icon-s-grid"></i>
            <span>Multi-node Query</span>
          </el-menu-item>

          <el-menu-item index="/manager/llm">
            <i class="el-icon-data-analysis"></i>
            <span>Knowledge Extraction</span>
          </el-menu-item>

          <el-menu-item index="/manager/predictTargets">
            <i class="el-icon-connection"></i>
            <span>Target Prediction</span>
          </el-menu-item>
        </el-menu>
      </el-header>

      <!-- Main content area -->
      <el-main>
        <router-view @update:user="updateUser" />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  data() {
    return {
      loginUser: JSON.parse(localStorage.getItem("honey-user") || '{}'),
      user: [],
    };
  },
  mounted() {
    request.get('/user/selectAll').then(res => {
      this.user = res.data;
    });
  },
  methods: {
    updateUser(user) {
      this.loginUser = JSON.parse(JSON.stringify(user));
    },
  },
};
</script>

<style>
/* Top navigation bar overall */
.header {
  display: flex;
  align-items: center;
  height: 90px;
  padding: 0 20px;
  background-color: #ffffff;
}

/* Logo */
.logo-container {
  display: flex;
  align-items: center;
  margin-right: 50px;
}

.text-logo {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 10px;
}

.logo-title {
  font-size: 26px;
  font-weight: bold;
  color: #333;
}

/* Top menu fills remaining width */
.top-menu {
  flex: 1;
  display: flex !important;
  font-size: 22px;
}

/* Each menu item equal width, fills header height, horizontally centered */
.el-menu--horizontal > .el-menu-item,
.el-menu--horizontal > .el-submenu {
  flex: 1;
  display: flex !important;
  align-items: center;
  justify-content: center;
  height: 90px !important;
  padding: 0 !important;
  text-align: center;
}

/* Icon size */
.el-menu-item i,
.el-submenu__title i {
  font-size: 28px;
  margin-right: 8px;
}

/* Text style */
.el-menu-item span,
.el-submenu__title span {
  display: inline-block;
  line-height: normal;
}

/* Submenu arrow size and position */
.el-submenu__icon {
  font-size: 20px !important;
  margin-left: 5px;
}

/* Hover highlight */
.el-menu-item:hover,
.el-submenu__title:hover {
  color: #409EFF !important;
}
</style>