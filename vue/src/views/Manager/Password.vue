<template>
  <div>
    <el-card style="width: 50%">
      <el-form ref="formRef" :model="user" :rules="rules" label-width="80px" style="padding-right: 20px">
        <el-form-item label="Old Password" prop="password">
          <el-input show-password v-model="user.password" placeholder="Old Password"></el-input>
        </el-form-item>
        <el-form-item label="New Password" prop="newPassword">
          <el-input show-password v-model="user.newPassword" placeholder="New Password"></el-input>
        </el-form-item>
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input show-password v-model="user.confirmPassword" placeholder="Confirm Password"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">Confirm</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Password",
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Please confirm your password'))
      } else if (value !== this.user.newPassword) {
        callback(new Error('Password confirmation does not match'))
      } else {
        callback()
      }
    }

    return {
      user: JSON.parse(localStorage.getItem('honey-user') || '{}'),
      rules: {
        password: [
          { required: true, message: 'Please enter your old password', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: 'Please enter your new password', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  created() {

  },
  methods: {
    update() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.user.password = this.user.newPassword
          this.$request.put('/user/update', this.user).then(res => {
            if (res.code === '200') {
              this.$message.success('Saved successfully')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>
.el-form-item__label {
  font-weight: bold;
}
</style>