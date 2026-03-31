<template>
  <div class="login-background">
    <div style=" background-color: white; width: 50%; border-radius: 5px; overflow: hidden">
      <div >
        <img src="@/assets/css/img/4.png" alt="" style="width: 100%;height: 250px">
      </div>
      <div style=" display: flex; align-items: center; justify-content: center;margin-bottom: 60px">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="loginRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center; margin-bottom: 20px">Welcome to TCM Knowledge Graph Management and Visualization System</div>
          <el-form-item prop="username" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="Please enter username" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password" style="margin: 30px 0">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="Please enter password" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="code" style="margin: 30px 0">
            <div style="display: flex">
              <el-input placeholder="Please enter verification code" prefix-icon="el-icon-circle-check" size="medium" style="flex: 1" v-model="user.code"></el-input>
              <div style="flex: 1; height: 36px">
                <valid-code @update:value="getCode" />
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button style="width: 100%;background-color: #2eab8c;color: white" @click="login">Login</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1">Don't have an account? <span style="color: #0f9876; cursor: pointer" @click="$router.push('/register')">Register</span></div>
            <div style="flex: 1; text-align: right"><span style="color: #0f9876; cursor: pointer" @click="handleForgetPassDialogVis">Forgot password?</span></div>
          </div>
        </el-form>
      </div>
    </div>
      <el-dialog title="Forgot Password" :visible.sync="forgetPassDialogVis" width="30%">
        <el-form :model="forgetUserForm" label-width="80px" style="padding-right: 20px">
          <el-form-item label="Username">
            <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="Please enter username"></el-input>
          </el-form-item>
          <el-form-item label="Phone">
            <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="Please enter phone number"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="forgetPassDialogVis = false">Cancel</el-button>
          <el-button type="primary" @click="resetPassword">Confirm</el-button>
        </div>
      </el-dialog>

  </div>
</template>

<script>
import ValidCode from "@/components/vaildCode.vue";
import request from "@/utils/request";

export default {
  name: "Login",
  components: {
    ValidCode
  },
  data() {

    // Captcha validation
    const validateCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Please enter verification code'))
      } else if (value !== this.code) {
        callback(new Error('Incorrect verification code'))
      } else {
        callback()
      }
    }

    return {
      forgetPassDialogVis:false,
      forgetUserForm:{},
      response: '',
      code: '',  // Captcha code from component
      user: {
        code: '',   // User input captcha
        username: '',
        password: ''
      },
      rules: {
        username: [
          {required: true, message: 'Please enter username', trigger: 'blur'},
        ],
        password: [
          {required: true, message: 'Please enter password', trigger: 'blur'},
        ],
        code: [
          {validator: validateCode, trigger: 'blur'}
        ],
      }
    }
  },

  created() {

  },

  methods: {
    getCode(code) {
      this.code = code
    },
    resetPassword() {
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('Password reset successful')
          this.forgetPassDialogVis = false
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    handleForgetPassDialogVis() {
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    login() {
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          this.$request.post('/login', this.user).then(res => {
            if (res.code === '200') {
              this.$router.push('/manager/home')
              this.$message.success('Login successful')
              localStorage.setItem("honey-user", JSON.stringify(res.data))
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

<style scoped>.login-background {
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