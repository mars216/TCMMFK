<template>
  <div class="register-background">
    <div style="background-color: white; width: 50%; border-radius: 5px; overflow: hidden">
      <div>
        <img src="@/assets/css/img/4.png" alt="" style="width: 100%; height: 250px">
      </div>
      <div style="display: flex; align-items: center; justify-content: center; margin-bottom: 60px">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="registerRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center; margin-bottom: 20px">Register for TCM Knowledge Graph System</div>
          <el-form-item prop="username" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="Username" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="Password" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="confirmPass" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="Confirm Password" v-model="user.confirmPass"></el-input>
          </el-form-item>
          <el-form-item prop="role" style="margin: 30px 0">
            <el-radio-group v-model="user.role">
              <el-radio label="Student">Student</el-radio>
              <el-radio label="Teacher">Teacher</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%; background-color: #2eab8c; color: white" @click="register">Register</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1">Already have an account? <span style="color: #0f9876; cursor: pointer" @click="$router.push('/')">Login</span></div>
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
    const validatePassword = (rule, confirmPass, callback) => {
      if (confirmPass === '') {
        callback(new Error('Please confirm your password'))
      } else if (confirmPass !== this.user.password) {
        callback(new Error('Passwords do not match'))
      } else {
        callback()
      }
    }
    return {
      user: {
        username: '',
        password: '',
        confirmPass: '',
        role: ''
      },
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' },
        ],
        password: [
          { required: true, message: 'Please enter password', trigger: 'blur' },
        ],
        confirmPass: [
          { validator: validatePassword, trigger: 'blur' }
        ],
        role: [
          { required: true, message: 'Please select role', trigger: 'blur' },
        ]
      }
    }
  },
  methods: {
    register() {
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          request.post('/register', this.user).then(res => {
            if (res.code === '200') {
              this.$router.push('/')
              this.$message.success('Registration successful')
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

<style scoped>
.register-background {
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