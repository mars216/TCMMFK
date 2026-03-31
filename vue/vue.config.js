// const { defineConfig } = require('@vue/cli-service')
// module.exports = defineConfig({
//   transpileDependencies: true,
//   devServer:{
//     port:7000
//   },//修改端口
//   chainWebpack:config => {
//     config.plugin('html').tap(
//         args => {
//           args[0].title='xiaobai';
//           return args;
//         }
//     )
//   }//修改他的title
// })
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,

  devServer: {
    port: 7000,
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },

  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].title = 'TCKMD'
      return args
    })
  }
})
