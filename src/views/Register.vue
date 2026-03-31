<template>
  <div class="register-background">
    <div style="background-color: white; width: 50%; border-radius: 5px; overflow: hidden">
      <div>
        <img src="@/assets/css/img/4.png" alt="" style="width: 100%; height: 250px">
      </div>
      <div style="display: flex; align-items: center; justify-content: center; margin-bottom: 60px">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="registerRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center; margin-bottom: 20px">欢迎注册中医药知识图谱管理与可视化系统</div>
          <el-form-item prop="username" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="请输入账号" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="请输入密码" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="confirmPass" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="请确认密码" v-model="user.confirmPass"></el-input>
          </el-form-item>
          <el-form-item prop="role" style="margin: 30px 0">
            <el-radio-group v-model="user.role">
              <el-radio label="学生"></el-radio>
              <el-radio label="老师"></el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%; background-color: #2eab8c; color: white" @click="register">注 册</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1">已经有账号了？请 <span style="color: #0f9876; cursor: pointer" @click="$router.push('/')">登录</span></div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>

import request from "@/utils/request";

export default {
  name: "Register",
  data() {
    // 验证码校验
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('请确认密码'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      user: {
        username: '',
        password: '',
        confirmPass: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入账号', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        role:[
          { required: true, message: '请选择角色', trigger: 'blur' },
        ]
      }
    }
  },
  created() {

  },
  methods: {
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          request.post('/register', this.user).then(res => {
            if (res.code === '200') {
              this.$router.push('/')
              this.$message.success('注册成功')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>.register-background {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('@/assets/css/img/tupian_bk.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
</style>

</style>