import axios from "axios";
import router from "@/router";

const request = axios.create({
    baseURL: '/api/',
    timeout: 100000
})

request.interceptors.request.use(
    config => {
        config.headers['content-type'] = 'application/json;charset=utf-8';
        //config.headers['token']=user?.token;
        let user = JSON.parse(localStorage.getItem("honey-user") || '{}')
        config.headers['token'] = user.token  // Set request header
        return config;
    }, error => {
        console.log('request error:' + error)
        return Promise.reject(error)
    }
);

request.interceptors.response.use(
    response => {
        let res = response.data
        if (typeof res == 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            router.push('/login')
        }
        return res
    }, error => {
        console.log('request error:' + error)
        return Promise.reject(error)
    }
);

export default request