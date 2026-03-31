<template>
  <div>
    <el-row>
      <el-col :span="1">
        <div  style="width: 100%;height: 300px; background-color: #42b983" class="grid-content bg-purple-dark"></div>
      </el-col>
      <el-col :span="12">
        <div  style="width: 100%;height: 300px; background-color:red" class="grid-content bg-purple-dark"></div>
      </el-col>
    </el-row>

    <el-row>
      <el-button round>圆角按钮</el-button>
      <el-button type="primary" round>主要按钮</el-button>
      <el-button type="success" round>成功按钮</el-button>
      <el-button type="info" round>信息按钮</el-button>
      <el-button type="warning" round>警告按钮</el-button>
      <el-button type="danger" round>危险按钮</el-button>
      <el-button type="primary" icon="el-icon-edit" circle></el-button>
      <el-button type="primary" :loading="true" round>加载中</el-button>
      <el-button circle icon="el-icon-edit"  type="primary"></el-button>
      <el-button circle icon="el-icon-delete"  type="danger"></el-button>
    </el-row>
    <el-row style="margin-top: 20px" >
      <el-input style="width: 200px" show-password suffix-icon="el-icon-search" v-model="value" ></el-input>
      <el-input style="width: 200px" clearable suffix-icon="el-icon-search" v-model="clear" placeholder="请输入" ></el-input>
      <el-col :span="12">
        <div class="sub-title">输入后匹配输入建议</div>
        <el-autocomplete
            class="inline-input"
            v-model="state2"
            :fetch-suggestions="querySearch"
            placeholder="请输入内容"
            :trigger-on-focus="false"
            @select="handleSelect"
        ></el-autocomplete>
      </el-col>

      <el-select v-model="select1" placeholder="请选择" @change="changeFood" filterable>
        <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
        </el-option>
      </el-select>
      <el-radio-group v-model="select2" @change="changeRadio">
        <el-radio label="男"></el-radio>
        <el-radio label="女"></el-radio>
      </el-radio-group>
    </el-row>
    <el-row> <span class="demonstration">默认</span>
      <el-date-picker
          v-model="value1"
          type="date"
          placeholder="选择日期"
          value-format="yyyy-MM-dd"
          @change="Datereturn">
      </el-date-picker></el-row>
      <span class="demonstration">默认</span>
      <el-date-picker
          v-model="value2"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          @change="Datereturn">
      </el-date-picker>
  </div>

</template>
<script>
export default {
  name:'element',
  data() {
    return {
      restaurants: [ { "value": "三全鲜食（北新泾店）", "address": "长宁区新渔路144号" },{ "value": "Hot honey 首尔炸鸡（仙霞路）", "address": "上海市长宁区淞虹路661号" }],
      value: '',
      value2:'',
      clear:'',
      select1:'',
      select2:'',
      value1:'',
      options: [{
        value: '选项1',
        label: '黄金糕'
      }, {
        value: '选项2',
        label: '双皮奶'
      }, {
        value: '选项3',
        label: '蚵仔煎'
      }, {
        value: '选项4',
        label: '龙须面'
      }, {
        value: '选项5',
        label: '北京烤鸭'
      }],
      checklist:[],
    }
  },
  methods:{
    querySearch(queryString, cb) {
      var restaurants = this.restaurants;
      var results = queryString ? restaurants.filter(v=>v.value.includes(queryString)) : restaurants;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    changeFood(){
      console.log(this.select1)
    },
    handleSelect(){

    },
    changeRadio(){
      alert(this.select2)
    },
    Datereturn(){
      console.log(this.value2)
    }
  }
}
</script>